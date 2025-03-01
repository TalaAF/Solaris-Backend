package com.example.lms.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logging filter to help debug JWT authentication issues
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class JwtLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        log.debug("Request to path: {}", path);
        
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("JWT token present in request to: {}", path);
        } else if (path.contains("/auth/") || path.contains("/debug/")) {
            log.debug("No JWT token found, but path is public: {}", path);
        } else {
            log.debug("No JWT token found in request to protected path: {}", path);
        }
        
        filterChain.doFilter(request, response);
        
        log.debug("Response status for {} : {}", path, response.getStatus());
    }
}