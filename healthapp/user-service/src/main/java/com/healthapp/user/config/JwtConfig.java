package com.healthapp.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    private String secret = "e3122928953516752200dd6346b870e53585fb28542d9b7ebeb04835586184f0";
    private long accessTokenExpiration = 900000;
    private String issuer = "health-app";
}
