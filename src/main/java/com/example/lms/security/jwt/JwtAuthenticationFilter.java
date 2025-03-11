package com.example.lms.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Simplified JWT Authentication Filter
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh-token",
        "/api/auth/forgot-password",
        "/api/auth/reset-password"
);

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
                try {
                    if (shouldSkipFilter(request)) {
                        filterChain.doFilter(request, response);
                        return;
                    }

            String jwt = getJwtFromRequest(request);
            log.debug("JWT in request: {}", jwt != null ? "Present" : "Absent");

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Authentication auth = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Set authentication in Security Context");
            }else if (StringUtils.hasText(jwt)) {
                log.debug("Invalid JWT token");
            }
        } catch (Exception ex) {
            log.error("Could not set authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Determine if this filter should be skipped for the given request
     *
     * @param request HTTP request
     * @return true if filter should be skipped, false otherwise
     */
    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip filter for public endpoints
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        // Skip filter for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        return false;
    }
}