package org.com.dianping.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

class RedisLockServiceTest {
    @SuppressWarnings("unchecked")
    @Test void acquiresWithLeaseAndReleasesOnlyThroughOwnerScript() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(eq("lock:test"), anyString(), eq(Duration.ofSeconds(5)))).thenReturn(true);
        RedisLockService locks = new RedisLockService(redis);

        String owner = locks.tryAcquire("lock:test", Duration.ofSeconds(5)).orElseThrow();
        locks.release("lock:test", owner);

        assertTrue(!owner.isBlank());
        verify(redis).execute(any(RedisScript.class), eq(List.of("lock:test")), eq(owner));
    }
}
