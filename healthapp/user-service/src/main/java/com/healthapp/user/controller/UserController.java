package com.healthapp.auth.controller;

import com.healthapp.auth.dto.response.UserResponse;
import com.healthapp.auth.entity.User;
import com.healthapp.auth.repository.UserRepository;
import com.healthapp.auth.security.CustomUserPrincipal;
import com.healthapp.auth.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
@Slf4j
public class UserController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication authentication) {
        log.info("Getting current user profile");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserResponse userResponse = userService.mapToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateUserProfile(
            @RequestBody Map<String, Object> updateData,
            Authentication authentication) {
        
        log.info("Updating user profile");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update allowed fields
        if (updateData.containsKey("firstName")) {
            user.setFirstName((String) updateData.get("firstName"));
        }
        if (updateData.containsKey("lastName")) {
            user.setLastName((String) updateData.get("lastName"));
        }
        if (updateData.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) updateData.get("phoneNumber"));
        }
        if (updateData.containsKey("profilePictureUrl")) {
            user.setProfilePictureUrl((String) updateData.get("profilePictureUrl"));
        }
        
        User updatedUser = userRepository.save(user);
        UserResponse userResponse = userService.mapToUserResponse(updatedUser);
        
        log.info("User profile updated successfully: {}", user.getEmail());
        return ResponseEntity.ok(userResponse);
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> passwordData,
            Authentication authentication) {
        
        log.info("Change password request received");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            log.error("Current password or new password is missing");
            throw new RuntimeException("Current password and new password are required");
        }
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.error("Current password is incorrect for user: {}", user.getEmail());
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password
        if (newPassword.length() < 8) {
            log.error("New password is too short");
            throw new RuntimeException("New password must be at least 8 characters long");
        }
        
        // Additional password validation (optional but recommended)
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,128}$")) {
            log.error("New password does not meet security requirements");
            throw new RuntimeException("Password must contain at least 1 lowercase, 1 uppercase, 1 digit, and 1 special character");
        }
        
        // Hash and update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", user.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        log.info("Getting user by ID: {}", userId);
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
