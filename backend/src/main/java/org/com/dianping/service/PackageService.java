package org.com.dianping.service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.com.dianping.observability.PlatformMetrics;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.dianping.entity.PackageGroup;
import org.com.dianping.repository.PackageGroupRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PackageService {
    private static final Logger log = LoggerFactory.getLogger(PackageService.class);
    private final PackageGroupRepository packages;
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final DatabaseFallbackLimiter fallbackLimiter;
    private final PlatformMetrics metrics;
    public PackageService(PackageGroupRepository packages, StringRedisTemplate redis, ObjectMapper json, DatabaseFallbackLimiter fallbackLimiter, PlatformMetrics metrics) { this.packages = packages; this.redis = redis; this.json = json; this.fallbackLimiter = fallbackLimiter; this.metrics = metrics; }
    public Optional<PackageGroup> findById(Long id) {
        String key = "package:detail:" + id;
        try {
            String cached = redis.opsForValue().get(key);
            if ("__null__".equals(cached)) { metrics.cache("package", "hit"); return Optional.empty(); }
            if (cached != null) { metrics.cache("package", "hit"); return Optional.of(json.readValue(cached, PackageGroup.class)); }
            Optional<PackageGroup> result = packages.findById(id);
            metrics.cache("package", "miss");
            redis.opsForValue().set(key, result.map(this::serialize).orElse("__null__"), Duration.ofMinutes(result.isPresent() ? 10 + ThreadLocalRandom.current().nextInt(5) : 1));
            return result;
        } catch (Exception ex) { metrics.cache("package", "fallback"); log.warn("package cache unavailable, falling back to database: {}", ex.toString()); return fallbackLimiter.execute(() -> packages.findById(id)); }
    }
    public PackageGroup save(PackageGroup value) {
        PackageGroup saved = packages.save(value);
        try { redis.delete("package:detail:" + saved.getId()); } catch (RuntimeException ex) { log.warn("package cache invalidation failed for id={}", saved.getId(), ex); }
        return saved;
    }
    private String serialize(PackageGroup value) { try { return json.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
}
