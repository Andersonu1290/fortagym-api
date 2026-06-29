package com.fortagym.config;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fortagym.service.EmailAlreadyExistsException;
import com.fortagym.service.DniAlreadyExistsException;

@RestControllerAdvice
public class GlobalAdvice {

    // 🛡️ Atrapa el error cuando un usuario intenta registrar un correo que ya existe
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", ex.getMessage()));
    }

    // 🛡️ Atrapa el error cuando un usuario intenta registrar un DNI que ya existe
    @ExceptionHandler(DniAlreadyExistsException.class)
    public ResponseEntity<?> handleDniAlreadyExists(DniAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", ex.getMessage()));
    }

    // Aquí en el futuro puedes agregar más @ExceptionHandler para otros errores
    // (Por ejemplo, si un usuario no existe, si el token expiró, etc.)
}