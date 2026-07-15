package org.com.dianping.config;

import java.io.IOException;
import java.time.Duration;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
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
        String key = "rate:" + path + ":" + (req.getHeader("UserId") == null ? req.getRemoteAddr() : req.getHeader("UserId")) + ":" + System.currentTimeMillis() / 60000;
        try { Long count = redis.opsForValue().increment(key); if (count != null && count == 1) redis.expire(key, Duration.ofMinutes(1)); if (count != null && count > limit) { ((HttpServletResponse) response).sendError(429, "请求过于频繁，请稍后重试"); return; } } catch (RuntimeException ignored) { }
        chain.doFilter(request, response);
    }
}
