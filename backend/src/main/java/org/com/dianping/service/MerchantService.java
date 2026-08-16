package org.com.dianping.service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.com.dianping.entity.Merchant;
import org.com.dianping.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.com.dianping.observability.PlatformMetrics;

import jakarta.transaction.Transactional;
import net.sourceforge.pinyin4j.PinyinHelper;

@Service
public class MerchantService {
    private static final Logger log = LoggerFactory.getLogger(MerchantService.class);

    private final MerchantRepository merchantRepository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final DatabaseFallbackLimiter fallbackLimiter;
    private final PlatformMetrics metrics;

    public MerchantService(MerchantRepository merchantRepository, StringRedisTemplate redis, ObjectMapper objectMapper, DatabaseFallbackLimiter fallbackLimiter, PlatformMetrics metrics) {
        this.merchantRepository = merchantRepository;
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.fallbackLimiter = fallbackLimiter;
        this.metrics = metrics;
    }

    @Transactional
    public List<Merchant> getMerchants(String keyword, Float minRating,
                                       String priceRange, Float avgPrice,
                                       String sortType) {
        Float minPrice = null;
        Float maxPrice = null;

        // 处理价格区间
        if (priceRange != null && !priceRange.isEmpty()) {
            String[] parts = priceRange.split("-");
            if (parts.length == 2) {
                minPrice = Float.parseFloat(parts[0]);
                maxPrice = Float.parseFloat(parts[1]);
            } else if (priceRange.endsWith("-")) {
                minPrice = Float.parseFloat(priceRange.substring(0, priceRange.length() - 1));
            } else if (priceRange.startsWith("-")) {
                maxPrice = Float.parseFloat(priceRange.substring(1));
            } else {
                minPrice = Float.parseFloat(priceRange);
            }
        }
        if (avgPrice != null) {
            maxPrice = avgPrice;
        }

        // 如果 keyword 是汉字，转换为拼音
        String keywordPinyin = null;
        if (keyword != null && keyword.matches("[\\u4e00-\\u9fa5]+")) {
            keywordPinyin = convertToPinyin(keyword);
        }

        List<Merchant> results = merchantRepository.searchMerchantsWithPinyin(
                keyword, minRating, minPrice, maxPrice);

        // 如果 keywordPinyin 不为空，追加拼音匹配结果
        if (keywordPinyin != null) {
            List<Merchant> pinyinResults = merchantRepository.searchMerchantsWithPinyin(
                    keywordPinyin, minRating, minPrice, maxPrice);
            results.addAll(pinyinResults);
        }

        // 去重
        results = new ArrayList<>(results.stream().distinct().toList());

        // 应用排序
        if (sortType != null) {
            switch (sortType) {
                case "rating":
                    results.sort((a, b) -> Float.compare(b.getRating(), a.getRating()));
                    break;
                case "price_asc":
                    results.sort((a, b) -> Float.compare(a.getAvgPrice(), b.getAvgPrice()));
                    break;
                case "price_desc":
                    results.sort((a, b) -> Float.compare(b.getAvgPrice(), a.getAvgPrice()));
                    break;
            }
        }
        return results;
    }

    private String convertToPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
            if (pinyinArray != null) {
                // 去掉音调标记
                pinyin.append(pinyinArray[0].replaceAll("\\d", ""));
            } else {
                pinyin.append(c);
            }
        }
        return pinyin.toString();
    }

    public List<Merchant> searchMerchantsByKeyword(String keyword) {
        return merchantRepository.findByKeyword(keyword);
    }

    public Optional<Merchant> getMerchantById(Long id) {
        String key = "merchant:detail:" + id;
        try {
            String cached = redis.opsForValue().get(key);
            if ("__null__".equals(cached)) { metrics.cache("merchant", "hit"); return Optional.empty(); }
            if (cached != null) { metrics.cache("merchant", "hit"); return Optional.of(objectMapper.readValue(cached, Merchant.class)); }
            Optional<Merchant> merchant = merchantRepository.findById(id);
            metrics.cache("merchant", "miss");
            redis.opsForValue().set(key, merchant.map(this::json).orElse("__null__"), Duration.ofMinutes(merchant.isPresent() ? 10 + ThreadLocalRandom.current().nextInt(5) : 1));
            return merchant;
        } catch (Exception ex) {
            metrics.cache("merchant", "fallback"); log.warn("merchant cache unavailable, falling back to database: {}", ex.toString());
            return fallbackLimiter.execute(() -> merchantRepository.findById(id));
        }
    }

    private String json(Merchant merchant) { try { return objectMapper.writeValueAsString(merchant); } catch (Exception e) { throw new IllegalStateException(e); } }

    public List<Merchant> searchMerchantsWithPinyin(String keyword) {
        return merchantRepository.searchMerchantsWithPinyin(keyword, null, null, null);
    }

    public String getMerchantCategory(Long businessId) {
        Merchant merchant = merchantRepository.findById(businessId)
            .orElseThrow(() -> new RuntimeException("商户不存在"));
        return merchant.getCategory();
    }
}
