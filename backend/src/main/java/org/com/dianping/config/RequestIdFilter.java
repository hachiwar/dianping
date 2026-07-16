package org.com.dianping.config;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.com.dianping.observability.PlatformMetrics;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component @Order(1)
public class RequestIdFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);
    private final PlatformMetrics metrics;
    public RequestIdFilter(PlatformMetrics metrics) { this.metrics = metrics; }
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request; HttpServletResponse httpResponse = (HttpServletResponse) response;
        String id = httpRequest.getHeader("X-Request-Id"); if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        long start = System.nanoTime(); MDC.put("requestId", id); httpRequest.setAttribute("requestId", id); httpResponse.setHeader("X-Request-Id", id);
        try { chain.doFilter(request, response); } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            metrics.request(httpResponse.getStatus());
            log.info("requestId={} method={} path={} status={} durationMs={}", id, httpRequest.getMethod(), httpRequest.getRequestURI(), httpResponse.getStatus(), elapsedMs);
            MDC.remove("requestId");
        }
    }
}
