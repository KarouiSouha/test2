// package com.healthapp.doctor.service;

// import com.healthapp.doctor.entity.Doctor;
// import com.healthapp.doctor.repository.DoctorRepository;
// import com.healthapp.shared.util.JwtUtil;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.stereotype.Service;

// import java.util.HashMap;
// import java.util.Map;

// /**
//  * DoctorLoginService - Gestion de l'authentification des docteurs
//  */
// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class DoctorLoginService {
    
//     private final DoctorRepository doctorRepository;
//     private final BCryptPasswordEncoder passwordEncoder;
//     private final JwtUtil jwtUtil;
    
//     @Value("${app.jwt.secret}")
//     private String jwtSecret;
    
//     @Value("${app.jwt.expiration:86400000}")  // 24h par défaut
//     private Long jwtExpiration;
    
//     /**
//      * Login d'un docteur
//      */
//     public Map<String, Object> loginDoctor(String email, String password) {
//         log.info("🔐 Doctor login attempt for: {}", email);
        
//         // Trouver le docteur par email
//         Doctor doctor = doctorRepository.findByEmail(email)
//                 .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
//         // Vérifier le password
//         if (!passwordEncoder.matches(password, doctor.getPassword())) {
//             log.error("❌ Invalid password for: {}", email);
//             throw new RuntimeException("Invalid email or password");
//         }
        
//         // Vérifier si le compte est activé
//         if (!doctor.getIsActivated()) {
//             log.warn("⚠️ Doctor account not activated: {}", email);
//             return Map.of(
//                 "error", "ACCOUNT_NOT_ACTIVATED",
//                 "message", "Your account is pending admin approval. Please wait for activation.",
//                 "activationStatus", doctor.getActivationStatus(),
//                 "email", doctor.getEmail()
//             );
//         }
        
//         // Générer JWT token
//         Map<String, Object> claims = new HashMap<>();
//         claims.put("email", doctor.getEmail());
//         claims.put("userId", doctor.getUserId());
//         claims.put("role", "DOCTOR");
//         claims.put("doctorId", doctor.getId());
        
//         String accessToken = JwtUtil.generateToken(claims, doctor.getEmail(), jwtExpiration, jwtSecret);
//         String refreshToken = JwtUtil.generateToken(claims, doctor.getEmail(), jwtExpiration * 7, jwtSecret); // 7 jours
        
//         log.info("✅ Login successful for doctor: {}", email);
        
//         return Map.of(
//             "accessToken", accessToken,
//             "refreshToken", refreshToken,
//             "userId", doctor.getUserId(),
//             "doctorId", doctor.getId(),
//             "email", doctor.getEmail(),
//             "fullName", doctor.getFullName(),
//             "isActivated", doctor.getIsActivated(),
//             "role", "DOCTOR"
//         );
//     }
// }


package com.healthapp.doctor.service;

import com.healthapp.doctor.entity.Doctor;
import com.healthapp.doctor.repository.DoctorRepository;
import com.healthapp.shared.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * DoctorLoginService - Doctor authentication management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorLoginService {
    
    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")  // 24h default
    private Long jwtExpiration;
    
    /**
     * Doctor login
     */
    public Map<String, Object> loginDoctor(String email, String password) {
        log.info("🔐 Doctor login attempt for: {}", email);
        
        // Find doctor by email
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(password, doctor.getPassword())) {
            log.error("❌ Invalid password for: {}", email);
            throw new RuntimeException("Invalid email or password");
        }
        
        // Check if account is activated
        if (!doctor.getIsActivated()) {
            log.warn("⚠️ Doctor account not activated: {}", email);
            return Map.of(
                "error", "ACCOUNT_NOT_ACTIVATED",
                "message", "Your account is pending admin approval. Please wait for activation.",
                "activationStatus", doctor.getActivationStatus(),
                "email", doctor.getEmail()
            );
        }
        
        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", doctor.getEmail());
        claims.put("userId", doctor.getUserId());
        claims.put("role", "DOCTOR");
        claims.put("doctorId", doctor.getId());
        
        // Call static method directly (no injection needed)
        String accessToken = JwtUtil.generateToken(claims, doctor.getEmail(), jwtExpiration, jwtSecret);
        String refreshToken = JwtUtil.generateToken(claims, doctor.getEmail(), jwtExpiration * 7, jwtSecret);
        
        log.info("✅ Login successful for doctor: {}", email);
        
        return Map.of(
            "accessToken", accessToken,
            "refreshToken", refreshToken,
            "userId", doctor.getUserId(),
            "doctorId", doctor.getId(),
            "email", doctor.getEmail(),
            "fullName", doctor.getFullName(),
            "isActivated", doctor.getIsActivated(),
            "role", "DOCTOR"
        );
    }
}