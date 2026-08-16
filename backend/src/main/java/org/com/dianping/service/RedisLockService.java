package org.com.dianping.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisLockService {
    private static final Logger log = LoggerFactory.getLogger(RedisLockService.class);
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);
    private final StringRedisTemplate redis;

    public RedisLockService(StringRedisTemplate redis) { this.redis = redis; }

    public Optional<String> tryAcquire(String key, Duration lease) {
        String owner = UUID.randomUUID().toString();
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, owner, lease))
                ? Optional.of(owner) : Optional.empty();
    }

    public void release(String key, String owner) {
        try { redis.execute(RELEASE, Collections.singletonList(key), owner); }
        catch (RuntimeException ex) { log.warn("distributed lock release failed key={}", key, ex); }
    }
}
