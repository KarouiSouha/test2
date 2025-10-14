// package com.healthapp.doctor.service;

// import com.healthapp.doctor.client.AuthServiceClient;
// import com.healthapp.doctor.client.NotificationClient;
// import com.healthapp.doctor.dto.request.BasicAuthRequest;
// import com.healthapp.doctor.dto.request.DoctorRegisterRequest;
// import com.healthapp.doctor.dto.request.EmailNotificationRequest;
// import com.healthapp.doctor.dto.response.AuthResponse;
// import com.healthapp.doctor.dto.response.DoctorResponse;
// import com.healthapp.doctor.entity.Doctor;
// import com.healthapp.doctor.entity.DoctorActivationRequest;
// import com.healthapp.doctor.repository.DoctorActivationRequestRepository;
// import com.healthapp.doctor.repository.DoctorRepository;
// import com.healthapp.shared.enums.UserRole;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.Map;
// import java.util.Set;

// /**
//  * DoctorAuthService - Gestion de l'enregistrement des médecins
//  * 
//  * Ce service orchestre l'enregistrement d'un médecin en:
//  * 1. Créant les credentials dans auth-service (via Feign)
//  * 2. Créant le profil médecin dans ce service
//  * 3. Créant une demande d'activation
//  * 4. Envoyant une notification aux admins (via Feign)
//  */
// @Service
// @RequiredArgsConstructor
// @Transactional
// @Slf4j
// public class DoctorAuthService {
    
//     private final DoctorRepository doctorRepository;
//     private final DoctorActivationRequestRepository activationRequestRepository;
//     private final AuthServiceClient authServiceClient;
//     private final NotificationClient notificationClient;
    
//     /**
//      * Enregistrer un nouveau médecin
//      * 
//      * WORKFLOW:
//      * 1. Appeler auth-service pour créer email/password
//      * 2. Créer le profil Doctor avec statut PENDING
//      * 3. Créer une DoctorActivationRequest
//      * 4. Notifier les admins par email
//      */
//     public DoctorResponse registerDoctor(DoctorRegisterRequest request) {
//         log.info("🏥 Starting doctor registration for: {}", request.getEmail());
        
//         // Vérifier si le médecin existe déjà
//         if (doctorRepository.existsByEmail(request.getEmail())) {
//             throw new RuntimeException("Doctor already exists with email: " + request.getEmail());
//         }
        
//         if (doctorRepository.existsByMedicalLicenseNumber(request.getMedicalLicenseNumber())) {
//             throw new RuntimeException("Medical license number already registered");
//         }
        
//         try {
//             // ÉTAPE 1: Créer le compte de base dans auth-service
//             log.info("Step 1: Creating basic auth account for {}", request.getEmail());
//             AuthResponse authResponse = createBasicAuthAccount(request);
            
//             // ÉTAPE 2: Créer le profil Doctor
//             log.info("Step 2: Creating doctor profile");
//             Doctor doctor = createDoctorProfile(request, authResponse.getUserId());
//             Doctor savedDoctor = doctorRepository.save(doctor);
            
//             // ÉTAPE 3: Créer la demande d'activation
//             log.info("Step 3: Creating activation request");
//             createActivationRequest(savedDoctor);
            
//             // ÉTAPE 4: Notifier les admins
//             log.info("Step 4: Notifying admins");
//             notifyAdmins(savedDoctor);
            
//             log.info("✅ Doctor registration completed successfully for: {}", request.getEmail());
            
//             return mapToDoctorResponse(savedDoctor);
            
//         } catch (Exception e) {
//             log.error("❌ Failed to register doctor: {}", request.getEmail(), e);
//             // En cas d'erreur, il faudrait rollback la création dans auth-service
//             // Pour simplifier, on relance l'exception
//             throw new RuntimeException("Failed to register doctor: " + e.getMessage(), e);
//         }
//     }
    
//     /**
//      * Créer le compte de base dans auth-service via Feign
//      */
//     private AuthResponse createBasicAuthAccount(DoctorRegisterRequest request) {
//         BasicAuthRequest authRequest = BasicAuthRequest.builder()
//                 .email(request.getEmail())
//                 .password(request.getPassword())
//                 .firstName(request.getFirstName())
//                 .lastName(request.getLastName())
//                 .birthDate(request.getBirthDate())
//                 .gender(request.getGender())
//                 .phoneNumber(request.getPhoneNumber())
//                 .roles(Set.of(UserRole.DOCTOR))
//                 .build();
        
//         return authServiceClient.createBasicAccount(authRequest);
//     }
    
//     /**
//      * Créer le profil Doctor
//      */
//     private Doctor createDoctorProfile(DoctorRegisterRequest request, String userId) {
//         return Doctor.builder()
//                 .userId(userId)
//                 .email(request.getEmail())
//                 .firstName(request.getFirstName())
//                 .lastName(request.getLastName())
//                 .phoneNumber(request.getPhoneNumber())
//                 .medicalLicenseNumber(request.getMedicalLicenseNumber())
//                 .specialization(request.getSpecialization())
//                 .hospitalAffiliation(request.getHospitalAffiliation())
//                 .yearsOfExperience(request.getYearsOfExperience())
//                 .officeAddress(request.getOfficeAddress())
//                 .consultationHours(request.getConsultationHours())
//                 .isActivated(false)
//                 .activationStatus("PENDING")
//                 .activationRequestDate(LocalDateTime.now())
//                 .totalPatients(0)
//                 .totalConsultations(0)
//                 .averageRating(0.0)
//                 .build();
//     }
    
//     /**
//      * Créer une demande d'activation
//      */
//     private void createActivationRequest(Doctor doctor) {
//         DoctorActivationRequest activationRequest = DoctorActivationRequest.builder()
//                 .doctorId(doctor.getId())
//                 .doctorEmail(doctor.getEmail())
//                 .doctorFullName(doctor.getFullName())
//                 .medicalLicenseNumber(doctor.getMedicalLicenseNumber())
//                 .specialization(doctor.getSpecialization())
//                 .hospitalAffiliation(doctor.getHospitalAffiliation())
//                 .yearsOfExperience(doctor.getYearsOfExperience())
//                 .isPending(true)
//                 .requestedAt(LocalDateTime.now())
//                 .build();
        
//         activationRequestRepository.save(activationRequest);
//     }
    
//     /**
//      * Notifier les admins par email via notification-service
//      */
//     private void notifyAdmins(Doctor doctor) {
//         try {
//             EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
//                     .to("admin@healthapp.com")  // TODO: Récupérer les vrais emails d'admins
//                     .subject("New Doctor Registration - Approval Required")
//                     .templateType("DOCTOR_REGISTRATION_ADMIN_NOTIFICATION")
//                     .templateVariables(Map.of(
//                         "adminName", "Admin",
//                         "doctorName", doctor.getFullName(),
//                         "doctorEmail", doctor.getEmail(),
//                         "medicalLicense", doctor.getMedicalLicenseNumber(),
//                         "specialization", doctor.getSpecialization(),
//                         "hospital", doctor.getHospitalAffiliation(),
//                         "experience", doctor.getYearsOfExperience(),
//                         "registrationDate", doctor.getCreatedAt().toString()
//                     ))
//                     .build();
            
//             notificationClient.sendEmail(emailRequest);
//             log.info("📧 Admin notification sent for doctor: {}", doctor.getEmail());
            
//         } catch (Exception e) {
//             log.error("Failed to send admin notification", e);
//             // On ne bloque pas l'enregistrement si l'email échoue
//         }
//     }
    
//     /**
//      * Mapper Doctor vers DoctorResponse
//      */
//     private DoctorResponse mapToDoctorResponse(Doctor doctor) {
//         return DoctorResponse.builder()
//                 .id(doctor.getId())
//                 .userId(doctor.getUserId())
//                 .email(doctor.getEmail())
//                 .firstName(doctor.getFirstName())
//                 .lastName(doctor.getLastName())
//                 .fullName(doctor.getFullName())
//                 .phoneNumber(doctor.getPhoneNumber())
//                 .medicalLicenseNumber(doctor.getMedicalLicenseNumber())
//                 .specialization(doctor.getSpecialization())
//                 .hospitalAffiliation(doctor.getHospitalAffiliation())
//                 .yearsOfExperience(doctor.getYearsOfExperience())
//                 .officeAddress(doctor.getOfficeAddress())
//                 .consultationHours(doctor.getConsultationHours())
//                 .isActivated(doctor.getIsActivated())
//                 .activationStatus(doctor.getActivationStatus())
//                 .activationDate(doctor.getActivationDate())
//                 .activationRequestDate(doctor.getActivationRequestDate())
//                 .totalPatients(doctor.getTotalPatients())
//                 .averageRating(doctor.getAverageRating())
//                 .totalConsultations(doctor.getTotalConsultations())
//                 .createdAt(doctor.getCreatedAt())
//                 .build();
//     }
// }
package com.healthapp.doctor.service;

import com.healthapp.doctor.client.AuthServiceClient;
import com.healthapp.doctor.client.NotificationClient;
import com.healthapp.doctor.dto.request.BasicAuthRequest;
import com.healthapp.doctor.dto.request.DoctorRegisterRequest;
import com.healthapp.doctor.dto.request.EmailNotificationRequest;
import com.healthapp.doctor.dto.response.AuthResponse;
import com.healthapp.doctor.dto.response.DoctorResponse;
import com.healthapp.doctor.entity.Doctor;
import com.healthapp.doctor.entity.DoctorActivationRequest;
import com.healthapp.doctor.repository.DoctorActivationRequestRepository;
import com.healthapp.doctor.repository.DoctorRepository;
import com.healthapp.shared.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DoctorAuthService {
    
    private final DoctorRepository doctorRepository;
    private final DoctorActivationRequestRepository activationRequestRepository;
    private final AuthServiceClient authServiceClient;
    private final NotificationClient notificationClient;
    
    /**
     * Enregistrer un nouveau médecin
     * 
     * WORKFLOW:
     * 1. Appeler auth-service pour créer email/password
     * 2. Créer le profil Doctor avec statut PENDING
     * 3. Créer une DoctorActivationRequest
     * 4. Envoyer email au MÉDECIN : "Votre compte est en cours de validation"
     * 5. Notifier les admins par email
     */
    public DoctorResponse registerDoctor(DoctorRegisterRequest request) {
        log.info("🏥 Starting doctor registration for: {}", request.getEmail());
        
        // Vérifier si le médecin existe déjà
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Doctor already exists with email: " + request.getEmail());
        }
        
        if (doctorRepository.existsByMedicalLicenseNumber(request.getMedicalLicenseNumber())) {
            throw new RuntimeException("Medical license number already registered");
        }
        
        try {
            // ÉTAPE 1: Créer le compte de base dans auth-service
            log.info("Step 1: Creating basic auth account for {}", request.getEmail());
            AuthResponse authResponse = createBasicAuthAccount(request);
            
            // ÉTAPE 2: Créer le profil Doctor
            log.info("Step 2: Creating doctor profile");
            Doctor doctor = createDoctorProfile(request, authResponse.getUserId());
            Doctor savedDoctor = doctorRepository.save(doctor);
            
            // ÉTAPE 3: Créer la demande d'activation
            log.info("Step 3: Creating activation request");
            createActivationRequest(savedDoctor);
            
            // ÉTAPE 4: Envoyer email au MÉDECIN (compte en validation)
            log.info("Step 4: Sending pending validation email to doctor");
            sendPendingValidationEmailToDoctor(savedDoctor);
            
            // ÉTAPE 5: Notifier les admins
            log.info("Step 5: Notifying admins");
            notifyAdmins(savedDoctor);
            
            log.info("✅ Doctor registration completed successfully for: {}", request.getEmail());
            
            return mapToDoctorResponse(savedDoctor);
            
        } catch (Exception e) {
            log.error("❌ Failed to register doctor: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to register doctor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Créer le compte de base dans auth-service via Feign
     */
    private AuthResponse createBasicAuthAccount(DoctorRegisterRequest request) {
        BasicAuthRequest authRequest = BasicAuthRequest.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(UserRole.DOCTOR))
                .build();
        
        return authServiceClient.createBasicAccount(authRequest);
    }
    
    /**
     * Créer le profil Doctor
     */
    private Doctor createDoctorProfile(DoctorRegisterRequest request, String userId) {
        return Doctor.builder()
                .userId(userId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .medicalLicenseNumber(request.getMedicalLicenseNumber())
                .specialization(request.getSpecialization())
                .hospitalAffiliation(request.getHospitalAffiliation())
                .yearsOfExperience(request.getYearsOfExperience())
                .officeAddress(request.getOfficeAddress())
                .consultationHours(request.getConsultationHours())
                .isActivated(false)
                .activationStatus("PENDING")
                .activationRequestDate(LocalDateTime.now())
                .totalPatients(0)
                .totalConsultations(0)
                .averageRating(0.0)
                .build();
    }
    
    /**
     * Créer une demande d'activation
     */
    private void createActivationRequest(Doctor doctor) {
        DoctorActivationRequest activationRequest = DoctorActivationRequest.builder()
                .doctorId(doctor.getId())
                .doctorEmail(doctor.getEmail())
                .doctorFullName(doctor.getFullName())
                .medicalLicenseNumber(doctor.getMedicalLicenseNumber())
                .specialization(doctor.getSpecialization())
                .hospitalAffiliation(doctor.getHospitalAffiliation())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .isPending(true)
                .requestedAt(LocalDateTime.now())
                .build();
        
        activationRequestRepository.save(activationRequest);
    }
    
    /**
     * NOUVEAU : Envoyer email au médecin pour lui dire que son compte est en validation
     */
    private void sendPendingValidationEmailToDoctor(Doctor doctor) {
        try {
            EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                    .to(doctor.getEmail())
                    .subject("Account Registration Received - Pending Validation")
                    .templateType("DOCTOR_REGISTRATION_PENDING")
                    .templateVariables(Map.of(
                        "doctorFirstName", doctor.getFirstName(),
                        "doctorLastName", doctor.getLastName(),
                        "registrationDate", doctor.getCreatedAt().toString()
                    ))
                    .build();
            
            notificationClient.sendEmail(emailRequest);
            log.info("📧 Pending validation email sent to doctor: {}", doctor.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send pending validation email to doctor", e);
            // On ne bloque pas l'enregistrement si l'email échoue
        }
    }
    
    /**
     * Notifier les admins par email via notification-service
     */
    private void notifyAdmins(Doctor doctor) {
        try {
            EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                    .to("admin@healthapp.com")
                    .subject("New Doctor Registration - Approval Required")
                    .templateType("DOCTOR_REGISTRATION_ADMIN_NOTIFICATION")
                    .templateVariables(Map.of(
                        "adminName", "Admin",
                        "doctorName", doctor.getFullName(),
                        "doctorEmail", doctor.getEmail(),
                        "medicalLicense", doctor.getMedicalLicenseNumber(),
                        "specialization", doctor.getSpecialization(),
                        "hospital", doctor.getHospitalAffiliation(),
                        "experience", doctor.getYearsOfExperience(),
                        "registrationDate", doctor.getCreatedAt().toString()
                    ))
                    .build();
            
            notificationClient.sendEmail(emailRequest);
            log.info("📧 Admin notification sent for doctor: {}", doctor.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send admin notification", e);
        }
    }
    
    /**
     * Mapper Doctor vers DoctorResponse
     */
    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(doctor.getUserId())
                .email(doctor.getEmail())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .fullName(doctor.getFullName())
                .phoneNumber(doctor.getPhoneNumber())
                .medicalLicenseNumber(doctor.getMedicalLicenseNumber())
                .specialization(doctor.getSpecialization())
                .hospitalAffiliation(doctor.getHospitalAffiliation())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .officeAddress(doctor.getOfficeAddress())
                .consultationHours(doctor.getConsultationHours())
                .isActivated(doctor.getIsActivated())
                .activationStatus(doctor.getActivationStatus())
                .activationDate(doctor.getActivationDate())
                .activationRequestDate(doctor.getActivationRequestDate())
                .totalPatients(doctor.getTotalPatients())
                .averageRating(doctor.getAverageRating())
                .totalConsultations(doctor.getTotalConsultations())
                .createdAt(doctor.getCreatedAt())
                .build();
    }
}