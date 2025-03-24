package com.example.lms.security.oauth;

import com.example.lms.security.model.Role;
import com.example.lms.security.repository.RoleRepository;
import com.example.lms.user.model.User;
import com.example.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // Extract OAuth2 attributes based on provider
        OAuth2UserInfo userInfo = extractUserInfo(userRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        
        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;
        
        if (userOptional.isPresent()) {
            // Update existing user
            user = userOptional.get();
            user = updateExistingUser(user, userInfo);
        } else {
            // Create new user
            user = registerNewUser(userRequest, userInfo);
        }
        
        return new CustomOAuth2User(oAuth2User, user);
    }

    private OAuth2UserInfo extractUserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Login with " + registrationId + " is not supported.");
        }
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setFullName(userInfo.getName());
        user.setProfilePicture(userInfo.getImageUrl());
        user.setActive(true);
        
        // Set default password (can be used for non-OAuth login)
        user.setPassword("{noop}oauth2user");  // You might want to use a more secure approach
        
        // Assign default role (STUDENT)
        Role defaultRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        // Update relevant fields if necessary
        if (userInfo.getName() != null && !user.getFullName().equals(userInfo.getName())) {
            user.setFullName(userInfo.getName());
        }
        
        if (userInfo.getImageUrl() != null && !userInfo.getImageUrl().equals(user.getProfilePicture())) {
            user.setProfilePicture(userInfo.getImageUrl());
        }
        
        return userRepository.save(user);
    }
}