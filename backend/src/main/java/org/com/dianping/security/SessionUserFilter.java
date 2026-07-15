package org.com.dianping.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.com.dianping.DTO.UserResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionUserFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Object value = request.getSession(false) == null ? null : request.getSession(false).getAttribute("USER_SESSION");
        if (value instanceof UserResponse user) {
            String header = request.getHeader("UserId");
            if (header != null && !header.equals(String.valueOf(user.id()))) { response.sendError(403, "用户身份不匹配"); return; }
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.id(), null, AuthorityUtils.createAuthorityList("ROLE_USER")));
        }
        chain.doFilter(request, response);
    }
}
