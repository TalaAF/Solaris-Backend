package com.example.lms.user.controller;

import com.example.lms.user.dto.UserCreateRequest;
import com.example.lms.user.dto.UserDTO;
import com.example.lms.user.dto.UserUpdateRequest;
import com.example.lms.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
 
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @GetMapping("/email/{email}")

    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }
    
    @PostMapping
    
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return new ResponseEntity<>(userService.createUser(userCreateRequest), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UserUpdateRequest userUpdateRequest,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateRequest, currentUser));
    }
    
    @PatchMapping("/{id}/deactivate")
   
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/activate")
   
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }
}