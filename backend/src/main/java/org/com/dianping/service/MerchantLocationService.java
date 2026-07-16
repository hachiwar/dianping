package org.com.dianping.service;

import java.util.List;
import org.com.dianping.DTO.NearbyMerchantResponse;
import org.com.dianping.entity.Merchant;
import org.com.dianping.repository.MerchantRepository;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Duration;

@Service
public class MerchantLocationService {
    private final MerchantRepository merchants;
    private final StringRedisTemplate redis;
    public MerchantLocationService(MerchantRepository merchants, StringRedisTemplate redis) { this.merchants = merchants; this.redis = redis; }
    public void index(Merchant merchant) {
        if (merchant.getLongitude() == null || merchant.getLatitude() == null) return;
        try { redis.opsForGeo().add(key(merchant.getCategory()), new Point(merchant.getLongitude(), merchant.getLatitude()), merchant.getId().toString()); redis.expire(key(merchant.getCategory()), Duration.ofHours(25)); } catch (RuntimeException ignored) { }
    }
    @EventListener(ApplicationReadyEvent.class)
    public void rebuildIndex() { merchants.findAll().forEach(this::index); }
    @Scheduled(fixedDelay = 43_200_000)
    public void refreshIndex() { rebuildIndex(); }
    public List<NearbyMerchantResponse> nearby(String category, double longitude, double latitude, double radiusMeters, int page, int size) {
        if (page < 0 || size < 1 || size > 100 || radiusMeters <= 0) throw new IllegalArgumentException("附近商家查询参数无效");
        try {
            GeoResults<GeoLocation<String>> found = redis.opsForGeo().search(key(category), GeoReference.fromCoordinate(new Point(longitude, latitude)), new Distance(radiusMeters, Metrics.METERS), RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().sortAscending().limit((page + 1) * size));
            if (found == null) return List.of();
            return found.getContent().stream().skip((long) page * size).map(result -> merchants.findById(Long.valueOf(result.getContent().getName())).map(merchant -> new NearbyMerchantResponse(merchant, result.getDistance().getValue())).orElse(null)).filter(java.util.Objects::nonNull).toList();
        } catch (RuntimeException ignored) { return List.of(); }
    }
    private String key(String category) { return "merchant:geo:" + category; }
}
