package com.example.lms.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.security.service.RefreshTokenService;
import com.example.lms.security.token.service.TokenStoreService;
import com.example.lms.user.repository.UserRepository;

import com.example.lms.user.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Simplified JWT Authentication Filter
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TokenStoreService tokenStoreService;

    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh-token",
        "/api/auth/forgot-password",
        "/api/auth/reset-password"
);

    

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
                String path = request.getRequestURI();
    log.debug("Processing request to path: {}", path);
                 // Skip filter for public paths and OPTIONS requests
    if (shouldSkipFilter(request)) {
        log.debug("Skipping JWT filter for path: {}", path);
        filterChain.doFilter(request, response);
        return;
    }
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // Basic validation (signature, expiration)
                if (tokenProvider.validateToken(jwt)) {
                    log.debug("JWT is valid: {}", jwt);
                    // Extract user info from token
                  
                    String tokenId = tokenProvider.getTokenIdFromToken(jwt);
                
                if (tokenId != null) {
                    log.debug("TokenId extracted from JWT: {}", tokenId);
                    // Check if the token is still valid in the token store
                    boolean isValidToken = tokenStoreService.validateToken(tokenId);
                    
                    if (isValidToken) {
                        log.debug("Token is valid in token store: {}", tokenId);
                        // Set authentication in security context
                        Authentication auth = tokenProvider.getAuthentication(jwt);
                        log.debug("Authorities set in SecurityContext: {}", auth.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Set authentication in Security Context for path: {}", request.getRequestURI());
                    } else {
                        log.debug("Token has been revoked or is invalid: {}", tokenId);
                    }
                } else {
                    log.debug("TokenId not found in JWT");
                }
            } else {
                log.debug("JWT validation failed");
            }
        } else {
                log.debug("No JWT found in request headers");
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