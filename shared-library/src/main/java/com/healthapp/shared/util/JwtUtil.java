package com.healthapp.shared.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitaire JWT partagé entre les microservices
 * Permet de valider les tokens sans recréer la logique partout
 */
public class JwtUtil {

    public static Claims extractAllClaims(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractUsername(String token, String secret) {
        return extractAllClaims(token, secret).getSubject();
    }

    public static String extractUserId(String token, String secret) {
        return extractAllClaims(token, secret).get("user_id", String.class);
    }

    public static boolean isTokenExpired(String token, String secret) {
        return extractAllClaims(token, secret)
                .getExpiration()
                .before(new Date());
    }

    public static boolean isTokenValid(String token, String username, String secret) {
        final String extractedUsername = extractUsername(token, secret);
        return extractedUsername.equals(username) && !isTokenExpired(token, secret);
    }
}
