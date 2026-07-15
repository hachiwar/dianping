package org.com.dianping.config;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component @Order(1)
public class RequestIdFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String id = ((HttpServletRequest) request).getHeader("X-Request-Id"); if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        MDC.put("requestId", id); ((HttpServletRequest) request).setAttribute("requestId", id); ((HttpServletResponse) response).setHeader("X-Request-Id", id);
        try { chain.doFilter(request, response); } finally { MDC.remove("requestId"); }
    }
}
