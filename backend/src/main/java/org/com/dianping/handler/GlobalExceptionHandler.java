package org.com.dianping.handler;

import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException e, HttpServletRequest request) { return response(HttpStatus.BAD_REQUEST, e, request); }
    @ExceptionHandler(SecurityException.class)
    ResponseEntity<ErrorResponse> handleForbidden(SecurityException e, HttpServletRequest request) { return response(HttpStatus.FORBIDDEN, e, request); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException e, HttpServletRequest request) { return response(HttpStatus.FORBIDDEN, e, request); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) { return response(HttpStatus.INTERNAL_SERVER_ERROR, e, request); }
    private ResponseEntity<ErrorResponse> response(HttpStatus status, Exception e, HttpServletRequest request) { return ResponseEntity.status(status).body(new ErrorResponse(status.value(), e.getMessage(), String.valueOf(request.getAttribute("requestId")), Instant.now())); }
    record ErrorResponse(int code, String message, String requestId, Instant timestamp) { }
}
