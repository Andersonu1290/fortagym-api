package com.fortagym.controller;

import com.fortagym.service.ConsejoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/consejos")
public class ConsejoController {
 
    @Autowired
    private ConsejoService consejoService;

    @GetMapping("/aleatorio")
    public ResponseEntity<?> obtenerConsejo() {
        // Devuelve el consejo en formato JSON: { "consejo": "Toma al menos 2 litros de agua..." }
        return ResponseEntity.ok(Collections.singletonMap("consejo", consejoService.obtenerConsejo()));
    }
}