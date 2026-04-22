package com.fortagym.controller;

import com.fortagym.model.Promocion;
import com.fortagym.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Collections;

// 🔥 PASE VIP: Aniquila el error 403 de CORS
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/admin/promociones")
public class PromocionController {

    @Autowired
    private PromocionRepository promocionRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(promocionRepository.findAll());
    }

    // 🚀 MÉTODO MEJORADO: Acepta Archivo físico O un Link (URL)
    @PostMapping("/subir")
    public ResponseEntity<?> subirPromocion(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "url", required = false) String url) {
        try {
            Promocion promocion = new Promocion();
            promocion.setNombre(nombre);
            promocion.setFechaSubida(LocalDateTime.now());

            // Si envió un archivo físico
            if (file != null && !file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                promocion.setImagen("/uploads/" + fileName);
            } 
            // Si envió un link de internet
            else if (url != null && !url.isEmpty()) {
                promocion.setImagen(url);
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Debe enviar un archivo o una URL"));
            }

            promocionRepository.save(promocion);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "✅ Promoción guardada con éxito"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Error interno: " + e.getMessage()));
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        promocionRepository.deleteById(id);
        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Eliminado correctamente"));
    }
}