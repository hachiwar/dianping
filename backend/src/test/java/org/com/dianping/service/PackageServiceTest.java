package org.com.dianping.service;

import static org.mockito.Mockito.*;
import org.com.dianping.entity.PackageGroup;
import org.com.dianping.repository.PackageGroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.dianping.observability.PlatformMetrics;

class PackageServiceTest {
    @Test void saveInvalidatesPackageCache() {
        PackageGroupRepository repository = mock(PackageGroupRepository.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        PackageGroup value = new PackageGroup(); value.setId(7L);
        when(repository.save(value)).thenReturn(value);

        new PackageService(repository, redis, new ObjectMapper(), new DatabaseFallbackLimiter(), mock(PlatformMetrics.class)).save(value);

        verify(redis).delete("package:detail:7");
    }
}
