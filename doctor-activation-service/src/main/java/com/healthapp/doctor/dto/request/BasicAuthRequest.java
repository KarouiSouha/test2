package com.healthapp.doctor.dto.request;

import com.healthapp.shared.enums.Gender;
import com.healthapp.shared.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request pour créer un compte de base dans auth-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicAuthRequest {
    
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String phoneNumber;
    private Set<UserRole> roles;
}