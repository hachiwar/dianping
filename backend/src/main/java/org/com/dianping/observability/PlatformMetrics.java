package org.com.dianping.observability;

import org.com.dianping.event.DeadLetterService;
import org.com.dianping.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class PlatformMetrics {
    private final MeterRegistry registry;
    public PlatformMetrics(MeterRegistry registry, OutboxEventRepository outboxEvents, DeadLetterService deadLetters) {
        this.registry = registry;
        Gauge.builder("dianping.outbox.pending", outboxEvents, repository -> repository.count()).register(registry);
        Gauge.builder("dianping.rabbit.dead_letter.pending", deadLetters, service -> Math.max(0, service.pending())).register(registry);
    }
    public void cache(String name, String result) { registry.counter("dianping.cache.requests", "cache", name, "result", result).increment(); }
    public void request(int status) {
        registry.counter("dianping.http.requests", "outcome", status >= 500 ? "error" : "success").increment();
        if (status >= 400) registry.counter("dianping.http.errors", "status", String.valueOf(status)).increment();
    }
}
