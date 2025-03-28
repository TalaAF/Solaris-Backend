package com.example.lms.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.mock.web.MockHttpServletRequest;


import com.example.lms.security.token.model.UserToken;
import com.example.lms.security.token.service.TokenStoreService;
import com.example.lms.user.repository.UserRepository;

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

    @Autowired
    private TokenStoreService tokenStoreService;

    @Autowired
    private UserRepository userRepository;
    @PostConstruct
    public void init() {
        // Initialize the JWT secret key from the configured secret string
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecretStr.getBytes());
        log.info("JWT Provider initialized with expiration: {} ms", jwtExpirationMs);
    }

    /**
     * Generate a JWT token for an authenticated user with roles and permissions
     * 
     * @param authentication The authenticated user
     * @return JWT token string
     */
    public String generateToken(Authentication authentication ,HttpServletRequest request) {
        String username;
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    
        // Check the type of principal
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            username = oauth2User.getAttribute("email");
            
            // Add additional claims if needed
            claims.put("name", oauth2User.getAttribute("name"));
            claims.put("picture", oauth2User.getAttribute("picture"));
        } else {
            username = authentication.getName();
        }
    
        // Process roles and permissions
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
    
        // Get your application's user entity from the repository
        com.example.lms.user.model.User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        
                 UserToken userToken = tokenStoreService.createToken(
        user,
        request.getHeader("User-Agent"),
        request.getRemoteAddr()
    );
        // Now use your application's User type
        claims.put("userId", user.getId());
        claims.put("tokenVersion", user.getTokenVersion());
        claims.put("issuedAt", new Date().getTime());
    
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
    
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(jwtIssuer)
                .setId(UUID.randomUUID().toString())
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

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    public Long getTokenVersionFromToken(String token) {
        Claims claims = parseToken(token);
        Long tokenVersion = claims.get("tokenVersion", Long.class);
        return tokenVersion != null ? tokenVersion : 0L;
    }
    /**
     * Validate a token's signature, expiration, and claims
     */
    public boolean validateToken(String token) {
        try {
            // Parse and validate token
            Claims claims = parseToken(token);

            // Check if token type is "access"
            String tokenId = claims.get("tokenId", String.class);
            if (tokenId == null) {
                log.error("Token ID is missing in the token claims");
                return false;
            }

            // Token is valid
            return tokenStoreService.validateToken(tokenId);
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
     * 
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
     * 
     * @param token The JWT token
     * @return Subject (username)
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Parse token and get claims
     * 
     * @param token The JWT token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Generate a token with custom expiration time
     * 
     * @param authentication The authenticated user
     * @param expirationMs   Custom expiration time in milliseconds
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
    // Add this method to JwtTokenProvider
public String generateToken(Authentication authentication) {
    // Create a mock HttpServletRequest
    HttpServletRequest mockRequest = new MockHttpServletRequest();
    return generateToken(authentication, mockRequest);
}
}