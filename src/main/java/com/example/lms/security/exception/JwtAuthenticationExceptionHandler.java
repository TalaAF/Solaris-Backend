package com.example.lms.security.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter to handle JWT authentication exceptions and return appropriate HTTP responses.
 * This runs before the JwtAuthenticationFilter to catch JWT-related exceptions.
 */
@Component
@Slf4j
public class JwtAuthenticationExceptionHandler extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired");
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT claims string is empty");
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        }
    }

    /**
     * Send an error response with a JSON body
     *
     * @param response   HTTP response object
     * @param statusCode HTTP status code
     * @param message    Error message
     * @throws IOException If an I/O error occurs
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", message);
        errorDetails.put("status", "error");
        errorDetails.put("code", String.valueOf(statusCode));

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
}