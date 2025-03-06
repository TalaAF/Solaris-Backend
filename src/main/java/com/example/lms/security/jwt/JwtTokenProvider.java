package com.example.lms.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simplified JWT Token Provider that uses a secure key generated at startup
 */
@Component
@Slf4j
public class JwtTokenProvider {

    // Generate a secure key at startup - this key will be regenerated each time the app restarts
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expirationTime = 86400000; // 24 hours

    /**
     * Generate a JWT token for an authenticated user
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> auth.startsWith("ROLE_"))
        .collect(Collectors.toList());

        List<String> permissions = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .filter(auth -> !auth.startsWith("ROLE_"))
        .collect(Collectors.toList());

        claims.put("roles", roles);
        claims.put("permissions", permissions);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        log.info("Generating token for user: {}", userDetails.getUsername());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)  // Just use the key directly
                .compact();
    }

    /**
     * Extract user details from a token
     */
    public UserDetails getUserDetailsFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        
        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
       
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        
        // Add permissions
        if (permissions != null) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }
    
        return new User(username, "", authorities);
    }

    /**
     * Validate a token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get authentication from token
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = getUserDetailsFromJWT(token);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}