package com.example.lms.security.oauth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/oauth2")
public class OAuth2TestController {

    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> oauthSuccess(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("token", token);
        response.put("message", "OAuth authentication successful");
        
        return ResponseEntity.ok(response);
    }
}
