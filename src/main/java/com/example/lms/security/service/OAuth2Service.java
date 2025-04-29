package com.example.lms.security.service;

import com.example.lms.security.dto.AuthResponse;
import com.example.lms.security.jwt.JwtTokenProvider;
import com.example.lms.security.model.Role;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;  // Add this import
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse processOAuth2Success(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            throw new IllegalStateException("Not an OAuth2 authentication");
        }
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String pictureUrl = oauth2User.getAttribute("picture");
        
        // Check if user exists or create a new one
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createNewUserFromOAuth2(email, name, pictureUrl));
        
        // Create authorities collection from user roles
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
        
        // Create our own authentication token with proper authorities
        Authentication customAuth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, authorities);
        
        // Generate JWT token
        String jwt = tokenProvider.generateToken(customAuth, request);
        
        // Create auth response
        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setName(user.getFullName());
        userInfo.setProfileImage(user.getProfilePicture());
        userInfo.setRoles(user.getRoles());
        
        response.setUser(userInfo);
        
        return response;
    }
    
    @Transactional
    private User createNewUserFromOAuth2(String email, String name, String pictureUrl) {
        User user = User.builder()
                .email(email)
                .fullName(name)
                .profilePicture(pictureUrl)
                .password(null) // OAuth2 users don't need passwords
                .isActive(true)
                .build();
        
        // Assign default STUDENT role
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }
}