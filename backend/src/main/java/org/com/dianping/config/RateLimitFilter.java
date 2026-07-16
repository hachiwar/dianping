package org.com.dianping.config;

import java.io.IOException;
import java.time.Duration;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.com.dianping.handler.ErrorResponseWriter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateLimitFilter implements Filter {
    private final StringRedisTemplate redis;
    public RateLimitFilter(StringRedisTemplate redis) { this.redis = redis; }
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request; String path = req.getRequestURI();
        if (!path.startsWith("/api/")) { chain.doFilter(request, response); return; }
        int limit = path.startsWith("/api/orders") ? 20 : 120;
        String identity = req.getUserPrincipal() == null ? req.getRemoteAddr() : req.getUserPrincipal().getName();
        String key = "rate:" + path + ":" + identity + ":" + System.currentTimeMillis() / 60000;
        try {
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1) redis.expire(key, Duration.ofMinutes(1));
            if (count != null && count > limit) { ErrorResponseWriter.write(req, (HttpServletResponse) response, 429, "Too many requests; retry shortly"); return; }
        } catch (RuntimeException ignored) { }
        chain.doFilter(request, response);
    }
}
