package org.com.dianping.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Duration;
import java.util.Optional;
import org.com.dianping.entity.Merchant;
import org.com.dianping.observability.PlatformMetrics;
import org.com.dianping.repository.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.fasterxml.jackson.databind.ObjectMapper;

class MerchantServiceTest {
    @SuppressWarnings("unchecked")
    @Test void cachesExistingMerchantWithJitteredExpiry() {
        MerchantRepository repository = mock(MerchantRepository.class); StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class); PlatformMetrics metrics = mock(PlatformMetrics.class);
        Merchant merchant = new Merchant(); merchant.setId(7L); merchant.setMerchantName("merchant");
        when(redis.opsForValue()).thenReturn(values); when(repository.findById(7L)).thenReturn(Optional.of(merchant));
        assertEquals(merchant, new MerchantService(repository, redis, new ObjectMapper(), new DatabaseFallbackLimiter(), metrics).getMerchantById(7L).orElseThrow());
        var ttl = org.mockito.ArgumentCaptor.forClass(Duration.class);
        verify(values).set(eq("merchant:detail:7"), anyString(), ttl.capture());
        assertTrue(ttl.getValue().toMinutes() >= 10 && ttl.getValue().toMinutes() <= 14);
        verify(metrics).cache("merchant", "miss");
    }
    @Test void limitsDatabaseFallbackWhenRedisFails() {
        MerchantRepository repository = mock(MerchantRepository.class); StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class); PlatformMetrics metrics = mock(PlatformMetrics.class);
        when(redis.opsForValue()).thenReturn(values); when(values.get(anyString())).thenThrow(new RuntimeException("redis down")); when(repository.findById(1L)).thenReturn(Optional.empty());
        assertTrue(new MerchantService(repository, redis, new ObjectMapper(), new DatabaseFallbackLimiter(), metrics).getMerchantById(1L).isEmpty());
        verify(metrics).cache("merchant", "fallback");
    }
}
