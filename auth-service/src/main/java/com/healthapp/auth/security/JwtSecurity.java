// // package com.healthapp.auth.security;

// // import io.jsonwebtoken.Claims;
// // import io.jsonwebtoken.Jwts;
// // import io.jsonwebtoken.io.Decoders;
// // import io.jsonwebtoken.security.Keys;
// // import org.springframework.beans.factory.annotation.Value;
// // import org.springframework.security.core.userdetails.UserDetails;
// // import org.springframework.stereotype.Service;

// // import javax.crypto.SecretKey;
// // import java.util.Date;
// // import java.util.HashMap;
// // import java.util.Map;
// // import java.util.function.Function;

// // @Service
// // public class JwtService {
    
// //     @Value("${jwt.secret}")
// //     private String secretKey;
    
// //     @Value("${jwt.expiration}")
// //     private long jwtExpiration;
    
// //     @Value("${jwt.refresh-token.expiration}")
// //     private long refreshExpiration;
    
// //     public String extractUsername(String token) {
// //         return extractClaim(token, Claims::getSubject);
// //     }
    
// //     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
// //         final Claims claims = extractAllClaims(token);
// //         return claimsResolver.apply(claims);
// //     }
    
// //     public String generateToken(UserDetails userDetails) {
// //         return generateToken(new HashMap<>(), userDetails);
// //     }
    
// //     public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
// //         return buildToken(extraClaims, userDetails, jwtExpiration);
// //     }
    
// //     public String generateRefreshToken(UserDetails userDetails) {
// //         return buildToken(new HashMap<>(), userDetails, refreshExpiration);
// //     }
    
// //     private String buildToken(
// //             Map<String, Object> extraClaims,
// //             UserDetails userDetails,
// //             long expiration
// //     ) {
// //         return Jwts
// //                 .builder()
// //                 .claims(extraClaims)
// //                 .subject(userDetails.getUsername())
// //                 .issuedAt(new Date(System.currentTimeMillis()))
// //                 .expiration(new Date(System.currentTimeMillis() + expiration))
// //                 .signWith(getSignInKey())
// //                 .compact();
// //     }
    
// //     public boolean isTokenValid(String token, UserDetails userDetails) {
// //         final String username = extractUsername(token);
// //         return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
// //     }
    
// //     private boolean isTokenExpired(String token) {
// //         return extractExpiration(token).before(new Date());
// //     }
    
// //     private Date extractExpiration(String token) {
// //         return extractClaim(token, Claims::getExpiration);
// //     }
    
// //     private Claims extractAllClaims(String token) {
// //         return Jwts
// //                 .parser()
// //                 .verifyWith(getSignInKey())
// //                 .build()
// //                 .parseSignedClaims(token)
// //                 .getPayload();
// //     }
    
// //     private SecretKey getSignInKey() {
// //         byte[] keyBytes = Decoders.BASE64.decode(secretKey);
// //         return Keys.hmacShaKeyFor(keyBytes);
// //     }
// // }


// package com.healthapp.auth.security;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.io.Decoders;
// import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Service;

// import javax.crypto.SecretKey;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Function;

// @Service
// public class JwtService {
    
//     @Value("${jwt.secret}")
//     private String secretKey;
    
//     @Value("${jwt.expiration}")
//     private long jwtExpiration;
    
//     @Value("${jwt.refresh-token.expiration}")
//     private long refreshExpiration;
    
//     public String extractUsername(String token) {
//         return extractClaim(token, Claims::getSubject);
//     }
    
//     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//         final Claims claims = extractAllClaims(token);
//         return claimsResolver.apply(claims);
//     }
    
//     public String generateToken(UserDetails userDetails) {
//         return generateToken(new HashMap<>(), userDetails);
//     }
    
//     public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//         return buildToken(extraClaims, userDetails, jwtExpiration);
//     }
    
//     // Méthode pour générer un token à partir d'un User directement
//     public String generateToken(Object user) {
//         Map<String, Object> claims = new HashMap<>();
//         String email = null;
        
//         // Extraire l'email selon le type d'objet
//         try {
//             java.lang.reflect.Method getEmailMethod = user.getClass().getMethod("getEmail");
//             email = (String) getEmailMethod.invoke(user);
//         } catch (Exception e) {
//             throw new RuntimeException("Cannot extract email from user object", e);
//         }
        
//         return buildTokenWithEmail(claims, email, jwtExpiration);
//     }
    
//     public long getAccessTokenExpiration() {
//         return jwtExpiration;
//     }
    
//     public String generateRefreshToken(UserDetails userDetails) {
//         return buildToken(new HashMap<>(), userDetails, refreshExpiration);
//     }
    
//     private String buildToken(
//             Map<String, Object> extraClaims,
//             UserDetails userDetails,
//             long expiration
//     ) {
//         return Jwts
//                 .builder()
//                 .claims(extraClaims)
//                 .subject(userDetails.getUsername())
//                 .issuedAt(new Date(System.currentTimeMillis()))
//                 .expiration(new Date(System.currentTimeMillis() + expiration))
//                 .signWith(getSignInKey())
//                 .compact();
//     }
    
//     // Méthode séparée pour construire un token avec un email (String)
//     private String buildTokenWithEmail(
//             Map<String, Object> extraClaims,
//             String email,
//             long expiration
//     ) {
//         return Jwts
//                 .builder()
//                 .claims(extraClaims)
//                 .subject(email)
//                 .issuedAt(new Date(System.currentTimeMillis()))
//                 .expiration(new Date(System.currentTimeMillis() + expiration))
//                 .signWith(getSignInKey())
//                 .compact();
//     }
    
//     public boolean isTokenValid(String token, UserDetails userDetails) {
//         final String username = extractUsername(token);
//         return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//     }
    
//     private boolean isTokenExpired(String token) {
//         return extractExpiration(token).before(new Date());
//     }
    
//     private Date extractExpiration(String token) {
//         return extractClaim(token, Claims::getExpiration);
//     }
    
//     private Claims extractAllClaims(String token) {
//         return Jwts
//                 .parser()
//                 .verifyWith(getSignInKey())
//                 .build()
//                 .parseSignedClaims(token)
//                 .getPayload();
//     }
    
//     private SecretKey getSignInKey() {
//         byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//         return Keys.hmacShaKeyFor(keyBytes);
//     }
// }

package com.healthapp.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtSecurity {
    
    @Value("${app.jwt.secret}")
    private String secretKey;
    
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${app.jwt.refresh-token.expiration}")
    private long refreshExpiration;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }
    
    // Méthode pour générer un token à partir d'un User directement
    public String generateToken(Object user) {
        Map<String, Object> claims = new HashMap<>();
        String email = null;
        
        // Extraire l'email selon le type d'objet
        try {
            java.lang.reflect.Method getEmailMethod = user.getClass().getMethod("getEmail");
            email = (String) getEmailMethod.invoke(user);
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract email from user object", e);
        }
        
        return buildTokenWithEmail(claims, email, jwtExpiration);
    }
    
    public long getAccessTokenExpiration() {
        return jwtExpiration;
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }
    
    // Surcharge pour accepter un objet User directement
    public String generateRefreshToken(Object user) {
        Map<String, Object> claims = new HashMap<>();
        String email = null;
        
        try {
            java.lang.reflect.Method getEmailMethod = user.getClass().getMethod("getEmail");
            email = (String) getEmailMethod.invoke(user);
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract email from user object", e);
        }
        
        return buildTokenWithEmail(claims, email, refreshExpiration);
    }
    
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }
    
    // Méthode séparée pour construire un token avec un email (String)
    private String buildTokenWithEmail(
            Map<String, Object> extraClaims,
            String email,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}