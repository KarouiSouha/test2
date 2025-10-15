// package com.healthapp.shared.util;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;

// import javax.crypto.SecretKey;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;

// /**
//  * Utilitaire JWT partagé entre les microservices
//  * Permet de valider les tokens sans recréer la logique partout
//  */
// public class JwtUtil {

//     public static Claims extractAllClaims(String token, String secret) {
//         SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

//         return Jwts.parserBuilder()
//                 .setSigningKey(key)
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     public static String extractUsername(String token, String secret) {
//         return extractAllClaims(token, secret).getSubject();
//     }

//     public static String extractUserId(String token, String secret) {
//         return extractAllClaims(token, secret).get("user_id", String.class);
//     }

//     public static boolean isTokenExpired(String token, String secret) {
//         return extractAllClaims(token, secret)
//                 .getExpiration()
//                 .before(new Date());
//     }

//     public static boolean isTokenValid(String token, String username, String secret) {
//         final String extractedUsername = extractUsername(token, secret);
//         return extractedUsername.equals(username) && !isTokenExpired(token, secret);
//     }
// }
package com.healthapp.shared.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Utilitaire JWT partagé entre les microservices
 * ✅ Peut générer et valider des tokens JWT
 */
public class JwtUtil {

    // 🔹 --- EXISTANT ---
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

    // 🔹 --- NOUVEAU ---
    /**
     * Génère un token JWT signé avec une clé secrète.
     */
    public static String generateToken(Map<String, Object> claims, String subject, Long expirationMillis, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
