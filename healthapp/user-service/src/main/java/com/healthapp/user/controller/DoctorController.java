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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctor")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
@Slf4j
public class DoctorController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getDoctorProfile(Authentication authentication) {
        log.info("Getting doctor profile");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User doctor = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!doctor.isDoctor()) {
            log.error("User is not a doctor: {}", doctor.getEmail());
            throw new RuntimeException("User is not a doctor");
        }
        
        UserResponse doctorResponse = userService.mapToUserResponse(doctor);
        return ResponseEntity.ok(doctorResponse);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateDoctorProfile(
            @RequestBody Map<String, Object> updateData,
            Authentication authentication) {
        
        log.info("Updating doctor profile");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User doctor = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!doctor.isDoctor()) {
            throw new RuntimeException("User is not a doctor");
        }
        
        // Update allowed fields
        if (updateData.containsKey("firstName")) {
            doctor.setFirstName((String) updateData.get("firstName"));
        }
        if (updateData.containsKey("lastName")) {
            doctor.setLastName((String) updateData.get("lastName"));
        }
        if (updateData.containsKey("phoneNumber")) {
            doctor.setPhoneNumber((String) updateData.get("phoneNumber"));
        }
        if (updateData.containsKey("specialization")) {
            doctor.setSpecialization((String) updateData.get("specialization"));
        }
        if (updateData.containsKey("hospitalAffiliation")) {
            doctor.setHospitalAffiliation((String) updateData.get("hospitalAffiliation"));
        }
        if (updateData.containsKey("yearsOfExperience")) {
            doctor.setYearsOfExperience((Integer) updateData.get("yearsOfExperience"));
        }
        if (updateData.containsKey("profilePictureUrl")) {
            doctor.setProfilePictureUrl((String) updateData.get("profilePictureUrl"));
        }
        
        User updatedDoctor = userRepository.save(doctor);
        UserResponse doctorResponse = userService.mapToUserResponse(updatedDoctor);
        
        log.info("Doctor profile updated: {}", doctor.getEmail());
        return ResponseEntity.ok(doctorResponse);
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changeDoctorPassword(
            @RequestBody Map<String, String> passwordData,
            Authentication authentication) {
        
        log.info("Doctor change password request received");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User doctor = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!doctor.isDoctor()) {
            throw new RuntimeException("User is not a doctor");
        }
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            log.error("Current password or new password is missing");
            throw new RuntimeException("Current password and new password are required");
        }
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, doctor.getPassword())) {
            log.error("Current password is incorrect for doctor: {}", doctor.getEmail());
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Validate new password
        if (newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters long");
        }
        
        if (!newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,128}$")) {
            throw new RuntimeException("Password must contain at least 1 lowercase, 1 uppercase, 1 digit, and 1 special character");
        }
        
        // Hash and update password
        doctor.setPassword(passwordEncoder.encode(newPassword));
        doctor.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(doctor);
        
        log.info("Password changed successfully for doctor: {}", doctor.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDoctorDashboard(Authentication authentication) {
        log.info("Getting doctor dashboard");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User doctor = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!doctor.isDoctor() || !doctor.getIsActivated()) {
            throw new RuntimeException("Doctor account is not activated");
        }
        
        Map<String, Object> dashboardData = Map.of(
            "doctorInfo", userService.mapToUserResponse(doctor),
            "activationStatus", "ACTIVATED",
            "activationDate", doctor.getActivationDate() != null ? doctor.getActivationDate() : "N/A",
            "message", "Welcome to your doctor dashboard!"
        );
        
        return ResponseEntity.ok(dashboardData);
    }
    
    @GetMapping("/activation-status")
    public ResponseEntity<Map<String, Object>> getActivationStatus(Authentication authentication) {
        log.info("Getting doctor activation status");
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
        
        User doctor = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        if (!doctor.isDoctor()) {
            throw new RuntimeException("User is not a doctor");
        }
        
        String status = doctor.getIsActivated() ? "ACTIVATED" : "PENDING_ACTIVATION";
        String message = doctor.getIsActivated() 
            ? "Your account is activated and ready to use"
            : "Your account is pending admin approval. You will receive an email once approved.";
        
        Map<String, Object> statusData = Map.of(
            "isActivated", doctor.getIsActivated(),
            "status", status,
            "message", message,
            "activationRequestDate", doctor.getActivationRequestDate() != null ? doctor.getActivationRequestDate() : "N/A",
            "activationDate", doctor.getActivationDate() != null ? doctor.getActivationDate() : "Not activated yet"
        );
        
        return ResponseEntity.ok(statusData);
    }
}
