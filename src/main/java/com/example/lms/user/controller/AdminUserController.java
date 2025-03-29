package com.example.lms.user.controller;

import com.example.lms.security.dto.RoleDTO;
import com.example.lms.user.dto.UserDTO;
import com.example.lms.user.dto.UserListDTO;
import com.example.lms.user.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Management", description = "API endpoints for managing users by admin")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation",
                content = @Content(schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
})
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<UserListDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Set<String> roles) {
        
        return ResponseEntity.ok(adminUserService.getAllUsers(page, size, sortBy, sortDirection, search, active, roles));
    }

    @Operation (summary = "Get user by ID", description = "Retrieve a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activate a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.deactivateUser(id));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles", description = "Update roles for a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User roles updated",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> updateUserRoles(@PathVariable Long id, @RequestBody Set<String> roleNames) {
        return ResponseEntity.ok(adminUserService.updateUserRoles(id, roleNames));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Retrieve all available roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved",
                    content = @Content(schema = @Schema(implementation = RoleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Roles not found")
    })
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(adminUserService.getAllRoles());
    }
}