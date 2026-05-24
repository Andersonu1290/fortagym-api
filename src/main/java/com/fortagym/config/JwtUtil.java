package com.fortagym.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; // 🚀 Importante
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 🚀 Inyectamos la llave secreta desde los application.properties
    @Value("${jwt.secret}")
    private String secretKeyString;

    // Método seguro para obtener y transformar la llave
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    // 1️⃣ Generar un token nuevo para el usuario
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    // 2️⃣ Validar si el token es correcto y pertenece al usuario
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // 3️⃣ Extraer el correo (username) oculto dentro del token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ==========================================
    // METODOS INTERNOS DE APOYO
    // ==========================================
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // 🚀 Usamos getSigningKey() en lugar de la variable estática
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // Aquí guardamos el email del usuario
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Caduca en 10 horas
                // 🚀 Usamos getSigningKey()
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) 
                .compact();
    }
}