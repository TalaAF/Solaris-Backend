package com.example.lms.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simplified JWT Token Provider that uses a secure key generated at startup
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret:secretKeyToBeChangedInProduction}")
    private String jwtSecretStr;
    
    @Value("${app.jwt.expiration-ms:3600000}") // Default: 1 hour
    private long jwtExpirationMs;
    
    @Value("${app.jwt.issuer:lms-application}")
    private String jwtIssuer;

    private SecretKey jwtSecret; 

    @PostConstruct
    public void init() {
        // Initialize the JWT secret key from the configured secret string
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecretStr.getBytes());
        log.info("JWT Provider initialized with expiration: {} ms", jwtExpirationMs);
    }
    /**
     * Generate a JWT token for an authenticated user with roles and permissions
     * @param authentication The authenticated user
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        
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
        claims.put("type", "access");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        log.info("Generating token for user: {}", userDetails.getUsername());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(jwtIssuer)
                .setId(UUID.randomUUID().toString())  // Unique token ID
                .signWith(jwtSecret)
                .compact();
    }

    /**
     * Extract user details from a token
     */
    public UserDetails getUserDetailsFromJWT(String token) {
        Claims claims = parseToken(token);

        String username = claims.getSubject();
        
        // Get roles and permissions from claims
        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);

        // Build authorities list from roles and permissions
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
     * Validate a token's signature, expiration, and claims
     */
    public boolean validateToken(String token) {
        try {
            // Parse and validate token
            Claims claims = parseToken(token);
            
            // Check if token type is "access"
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.error("Invalid token type: {}", tokenType);
                return false;
            }
            
            // Token is valid
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Malformed JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("JWT validation error: {}", ex.getMessage());
        }
        
        return false;
    }
    

    /**
     * Get authentication from token
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = getUserDetailsFromJWT(token);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * Get expiration date from token
     * @param token The JWT token
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return parseToken(token).getExpiration();
    }
    /**
     * Check if a token is expired
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Get token subject (username)
     * @param token The JWT token
     * @return Subject (username)
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Parse token and get claims
     * @param token The JWT token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

     /**
     * Generate a token with custom expiration time
     * @param authentication The authenticated user
     * @param expirationMs Custom expiration time in milliseconds
     * @return JWT token string
     */
    public String generateTokenWithCustomExpiration(Authentication authentication, long expirationMs) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
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
        claims.put("type", "access");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        log.info("Generating custom expiration JWT token for user: {}", userDetails.getUsername());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(jwtIssuer)
                .setId(UUID.randomUUID().toString())
                .signWith(jwtSecret)
                .compact();
    }
}