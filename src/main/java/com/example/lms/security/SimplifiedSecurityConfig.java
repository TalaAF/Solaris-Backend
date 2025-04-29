package com.example.lms.security;

import com.example.lms.security.exception.JwtAuthenticationExceptionHandler;
import com.example.lms.security.filter.DynamicPermissionFilter;
import com.example.lms.security.jwt.JwtAuthenticationFilter;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.security.oauth.CustomOAuth2UserService;
import com.example.lms.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.example.lms.security.service.RefreshTokenService;
import com.example.lms.security.token.service.TokenStoreService;
import com.example.lms.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import com.example.lms.security.oauth.OAuth2AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Simplified Security Configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SimplifiedSecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final DynamicPermissionFilter dynamicPermissionFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationExceptionHandler jwtAuthenticationExceptionHandler;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final TokenStoreService tokenStoreService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        
        return new JwtAuthenticationFilter(tokenProvider, userRepository, tokenStoreService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);// Increased strength (default is 10)
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

     @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", authException.getMessage());
            error.put("path", request.getRequestURI());
            
            String json = new ObjectMapper().writeValueAsString(error);
            response.getWriter().write(json);
        };
    }

     @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://yourdomain.com")); // Frontend URLs
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Focus on /api endpoints
        return source;
    }

    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS and disable CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            
            // Set session management to stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Set unauthorized requests exception handler
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(authenticationEntryPoint()))
            
            // Set permissions on endpoints
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - add specifically needed frontend endpoints
                .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/code/*", 
                     "/api/public/**", "/api/health/**", "/api/swagger-ui.html",
                     "/api/swagger-ui/**", "/api/v3/api-docs", "/api/v3/api-docs/**").permitAll()
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // All other endpoints need authentication
                .anyRequest().authenticated()
            )
            
            // JWT and permission filters
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            //.addFilterAfter(dynamicPermissionFilter, JwtAuthenticationFilter.class)
            
            // For H2 console, if needed in development
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            
            // OAuth2 configuration - improved configuration with proper endpoints
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> 
                    endpoint.baseUri("/oauth2/authorization"))  // Changed from "/oauth2/authorize" to "/oauth2/authorization"
                .redirectionEndpoint(endpoint -> 
                    endpoint.baseUri("/login/oauth2/code/*"))
                .userInfoEndpoint(endpoint -> 
                    endpoint.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    // Custom failure handler
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Authentication failed: " + 
                        exception.getMessage().replace("\"", "'") + "\"}");
                })
            );
        
        return http.build();
    }

    
}