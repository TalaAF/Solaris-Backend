package com.example.lms.security.oauth;

import com.example.lms.user.model.User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User extends DefaultOAuth2User {
    private final User user;

    public CustomOAuth2User(OAuth2User oAuth2User, User user) {
        super(
            oAuth2User.getAuthorities(),
            oAuth2User.getAttributes(),
            "sub" // This is the attribute that uniquely identifies the user
        );
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }
}