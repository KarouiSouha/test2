package com.healthapp.doctor.client;

import com.healthapp.doctor.dto.request.BasicAuthRequest;
import com.healthapp.doctor.dto.response.AuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client Feign pour communiquer avec le Auth Service
 * 
 * Ce client permet de créer les credentials de base (email/password)
 * dans le auth-service quand un médecin s'enregistre
 */
// @FeignClient(name = "auth-service")

@FeignClient(
    name = "auth-service",
    url = "http://localhost:8082"  // Direct URL instead of service name
)
public interface AuthServiceClient {
    
    /**
     * Créer un compte utilisateur de base dans auth-service
     * Le auth-service retourne un token JWT
     */
    @PostMapping("/api/v1/auth/register-basic")
    AuthResponse createBasicAccount(@RequestBody BasicAuthRequest request);
}
