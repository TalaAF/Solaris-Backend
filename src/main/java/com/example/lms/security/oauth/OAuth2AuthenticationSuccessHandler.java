package com.example.lms.security.oauth;

import com.example.lms.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        // Generate JWT token
        String token = tokenProvider.generateToken(authentication);
        
         // Get the redirect URL (frontend application URL)
         String targetUrl = determineTargetUrl(request, response, authentication);

        // For backend testing, redirect to a simple success page with token
        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
        
        // Redirect to our own endpoint instead of a frontend
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        // For backend testing, we'll use our own endpoint
        return "/oauth2/success";
    }
}
