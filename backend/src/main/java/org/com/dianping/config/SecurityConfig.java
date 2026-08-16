package org.com.dianping.config;

import org.com.dianping.security.SessionUserFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.com.dianping.handler.ErrorResponseWriter;

@Configuration @EnableWebSecurity
public class SecurityConfig {
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, SessionUserFilter sessionUserFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(a -> a.requestMatchers("/api/orders/**", "/api/coupons/**", "/api/reviews/**", "/api/invitation-records", "/api/reward-coupons", "/api/operations/**", "/api/search/**").authenticated().anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, error) -> ErrorResponseWriter.write(request, response, 401, "Login required")).accessDeniedHandler((request, response, error) -> ErrorResponseWriter.write(request, response, 403, "Access denied")))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                .addFilterBefore(sessionUserFilter, UsernamePasswordAuthenticationFilter.class).build();
    }
}
