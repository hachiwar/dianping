package org.com.dianping.service;

import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFallbackLimiter {
    private final Semaphore permits = new Semaphore(50);
    public <T> T execute(Supplier<T> operation) {
        if (!permits.tryAcquire()) throw new IllegalStateException("Service temporarily busy; retry shortly");
        try { return operation.get(); } finally { permits.release(); }
    }
}
