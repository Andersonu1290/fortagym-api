package com.fortagym.controller;

import com.fortagym.model.Nutricion;
import com.fortagym.model.Usuario;
import com.fortagym.repository.NutricionRepository;
import com.fortagym.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // 👈 Aquí viene el CrossOrigin

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

// 🔥 EL PASE VIP: Esto aniquila el error 403 de CORS a nivel de controlador
@CrossOrigin(origins = "http://localhost:4200") 
@RestController 
@RequestMapping("/api/nutricion") 
public class NutricionController {

    private static final Logger logger = LoggerFactory.getLogger(NutricionController.class);

    @Autowired
    private NutricionRepository nutricionRepository;

    @Autowired
    private UsuarioService usuarioService;

    // ==============================================================
    // 1. OBTENER CARTILLA NUTRICIONAL
    // ==============================================================
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerNutricionPorUsuario(@PathVariable Long usuarioId) {
        logger.info("📌 Consultando cartilla nutricional del usuario ID: {}", usuarioId);
        
        Usuario usuario = usuarioService.findById(usuarioId);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<Nutricion> nutricionOpt = nutricionRepository.findByUsuario(usuario);
        if (nutricionOpt.isEmpty()) {
            return ResponseEntity.ok(new HashMap<String, String>() {{ 
                put("mensaje", "Sin cartilla nutricional"); 
            }});
        }

        return ResponseEntity.ok(nutricionOpt.get());
    }

    // ==============================================================
    // 2. GUARDAR O ACTUALIZAR CARTILLA
    // ==============================================================
    // ==============================================================
    // 2. GUARDAR O ACTUALIZAR CARTILLA (MÉTODO BLINDADO)
    // ==============================================================
    @PostMapping("/guardar")
    public ResponseEntity<?> guardarNutricion(@RequestBody java.util.Map<String, Object> payload) {
        logger.info("📌 Recibiendo datos de Angular de forma segura...");

        try {
            // 1. Extraemos los datos del JSON a mano para que Java no explote
            java.util.Map<String, Object> usuarioMap = (java.util.Map<String, Object>) payload.get("usuario");
            Long usuarioId = Long.valueOf(usuarioMap.get("id").toString());
            
            String analisis = payload.get("analisisCorporal").toString();
            String observaciones = payload.get("observaciones").toString();

            // 2. Buscamos al usuario en la BD
            Usuario usuario = usuarioService.findById(usuarioId);
            if (usuario == null) {
                return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Usuario no encontrado"));
            }

            // 3. Guardamos la nutrición
            Nutricion nutricion = nutricionRepository.findByUsuario(usuario).orElse(new Nutricion());
            nutricion.setUsuario(usuario);
            nutricion.setAnalisisCorporal(analisis);
            nutricion.setObservaciones(observaciones);
            
            if (nutricion.getFechaRegistro() == null) {
                nutricion.setFechaRegistro(LocalDateTime.now()); 
            }

            nutricionRepository.save(nutricion);

            return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", "✅ Cartilla nutricional guardada con éxito"));

        } catch (Exception e) {
            // Si algo falla, ahora sí veremos el error real en la consola de Java
            logger.error("🔥 Error crítico al guardar la nutrición: ", e);
            return ResponseEntity.internalServerError().body(java.util.Collections.singletonMap("error", "Error interno: " + e.getMessage()));
        }
    }
}