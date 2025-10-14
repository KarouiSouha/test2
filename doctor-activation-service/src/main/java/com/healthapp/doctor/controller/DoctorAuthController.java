package com.healthapp.doctor.controller;

import com.healthapp.doctor.dto.request.DoctorRegisterRequest;
import com.healthapp.doctor.dto.response.DoctorResponse;
import com.healthapp.doctor.service.DoctorAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DoctorAuthController - Endpoints publics pour l'enregistrement des médecins
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Slf4j
public class DoctorAuthController {
    
    private final DoctorAuthService doctorAuthService;
    
    /**
     * Endpoint public pour l'enregistrement d'un médecin
     * 
     * Ce endpoint ne nécessite pas d'authentification
     */
    @PostMapping("/register")
    public ResponseEntity<DoctorResponse> registerDoctor(@Valid @RequestBody DoctorRegisterRequest request) {
        log.info("🏥 Doctor registration request received for: {}", request.getEmail());
        
        DoctorResponse response = doctorAuthService.registerDoctor(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Doctor Activation Service is UP");
    }
}