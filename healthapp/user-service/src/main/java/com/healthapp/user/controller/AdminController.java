
package com.healthapp.user.controller;

import com.healthapp.user.dto.response.UserResponse;
import com.healthapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin requesting all users");
        // TODO: Implémenter getAllUsers dans UserService
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        log.info("Admin requesting user: {}", userId);
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}