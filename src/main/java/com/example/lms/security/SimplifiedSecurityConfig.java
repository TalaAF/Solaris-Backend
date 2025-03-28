package com.example.lms.security;

import com.example.lms.security.exception.JwtAuthenticationExceptionHandler;
import com.example.lms.security.filter.DynamicPermissionFilter;
import com.example.lms.security.jwt.JwtAuthenticationFilter;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.security.oauth.CustomOAuth2UserService;
import com.example.lms.security.oauth.OAuth2AuthenticationSuccessHandler;
import com.example.lms.security.service.RefreshTokenService;
import com.example.lms.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, userRepository, refreshTokenService);
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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://yourdomain.com")); // Add your frontend URLs
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
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
                // Public endpoints
                .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
                .requestMatchers("/oauth2/success").permitAll() 
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/health/**").permitAll()
                .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // All other endpoints need authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter and dynamic permission filter
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(dynamicPermissionFilter, JwtAuthenticationFilter.class)
            
            // For H2 console, if needed in development (disable in production)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            
            // OAuth2 configuration
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> 
                    endpoint.baseUri("/oauth2/authorize"))
                .redirectionEndpoint(endpoint -> 
                    endpoint.baseUri("/login/oauth2/code/*"))
                .userInfoEndpoint(endpoint -> 
                    endpoint.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
            );
        
        return http.build();
    }

    
}