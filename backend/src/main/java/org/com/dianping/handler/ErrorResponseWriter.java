package org.com.dianping.handler;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class ErrorResponseWriter {
    private ErrorResponseWriter() { }
    public static void write(HttpServletRequest request, HttpServletResponse response, int status, String message) throws IOException {
        Object attribute = request.getAttribute("requestId"); String requestId = attribute == null ? UUID.randomUUID().toString() : attribute.toString();
        response.setStatus(status); response.setContentType("application/json;charset=UTF-8");
        response.setHeader("X-Request-Id", requestId);
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\",\"requestId\":\"" + requestId + "\",\"timestamp\":\"" + Instant.now() + "\"}");
    }
}
