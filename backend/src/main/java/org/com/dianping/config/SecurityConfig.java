package org.com.dianping.config;

import org.com.dianping.security.SessionUserFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableWebSecurity
public class SecurityConfig {
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, SessionUserFilter sessionUserFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(a -> a.requestMatchers("/api/orders/**", "/api/coupons/**", "/api/reviews/**", "/api/invitation-*").authenticated().anyRequest().permitAll())
                .addFilterBefore(sessionUserFilter, UsernamePasswordAuthenticationFilter.class).build();
    }
}
