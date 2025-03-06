package com.example.lms.security.filter;

import com.example.lms.security.service.SecurityEndpointService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class DynamicPermissionFilter extends OncePerRequestFilter {

    private final SecurityEndpointService securityEndpointService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // Skip filter for authentication paths and public resources
        String path = request.getRequestURI();
        if (path.contains("/api/auth/") || 
            path.contains("/h2-console/") ||
            path.contains("/public/")) {
            chain.doFilter(request, response);
            return;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
                !(authentication instanceof AnonymousAuthenticationToken)) {
            
            String method = request.getMethod();
            
            // Special case for admin endpoints
            if (path.startsWith("/api/admin/")) {
                if (!securityEndpointService.hasAdminRole(authentication)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Access denied: Admin role required");
                    return;
                }
            } 
            // Check dynamic permission for all other endpoints
            else if (!securityEndpointService.checkPermission(authentication, method, path)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied: Insufficient permissions");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}