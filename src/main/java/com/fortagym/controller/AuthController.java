package com.fortagym.controller;

import com.fortagym.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // 1. Angular hará un POST a http://localhost:8089/api/auth/login enviando un JSON
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // 2. Intentamos iniciar sesión con Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (Exception e) {
            // Si la clave o correo están mal, devolvemos un error 401 (No autorizado)
            return ResponseEntity.status(401).body(new ErrorResponse("❌ Credenciales incorrectas."));
        }

        // 3. Si todo está bien, buscamos al usuario en la base de datos
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        
        // 4. Creamos el Token matemático
        final String jwt = jwtUtil.generateToken(userDetails);

        // 5. Se lo enviamos a Angular en formato JSON
        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}

// ==========================================
// DTOs (Objetos de Transferencia de Datos)
// Estas clases pequeñas le enseñan a Spring Boot cómo leer y escribir los JSON
// ==========================================

class AuthRequest {
    private String email;
    private String password;

    public AuthRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class AuthResponse {
    private final String token;

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}

class ErrorResponse {
    private final String mensaje;

    public ErrorResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() { return mensaje; }
}