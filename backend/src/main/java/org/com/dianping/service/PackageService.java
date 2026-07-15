package org.com.dianping.service;

import java.time.Duration;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.dianping.entity.PackageGroup;
import org.com.dianping.repository.PackageGroupRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PackageService {
    private final PackageGroupRepository packages;
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    public PackageService(PackageGroupRepository packages, StringRedisTemplate redis, ObjectMapper json) { this.packages = packages; this.redis = redis; this.json = json; }
    public Optional<PackageGroup> findById(Long id) {
        String key = "package:detail:" + id;
        try {
            String cached = redis.opsForValue().get(key);
            if ("__null__".equals(cached)) return Optional.empty();
            if (cached != null) return Optional.of(json.readValue(cached, PackageGroup.class));
            Optional<PackageGroup> result = packages.findById(id);
            redis.opsForValue().set(key, result.map(this::serialize).orElse("__null__"), Duration.ofMinutes(result.isPresent() ? 10 : 1));
            return result;
        } catch (Exception ignored) { return packages.findById(id); }
    }
    public PackageGroup save(PackageGroup value) {
        PackageGroup saved = packages.save(value);
        try { redis.delete("package:detail:" + saved.getId()); } catch (RuntimeException ignored) { }
        return saved;
    }
    private String serialize(PackageGroup value) { try { return json.writeValueAsString(value); } catch (Exception e) { throw new IllegalStateException(e); } }
}
