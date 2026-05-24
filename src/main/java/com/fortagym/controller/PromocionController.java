package com.fortagym.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fortagym.model.Promocion;
import com.fortagym.repository.PromocionRepository;

@RestController
@RequestMapping("/api/admin/promociones")
public class PromocionController {

    @Autowired
    private PromocionRepository promocionRepository;

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(promocionRepository.findAll());
    }

    @GetMapping("/ultima")
    public ResponseEntity<?> obtenerUltimaPromocion() {
        try {
            Promocion ultima = promocionRepository.findTopByOrderByFechaSubidaDesc();
            if (ultima == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ultima);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/subir")
    public ResponseEntity<?> subirPromocion(
            @RequestParam("nombre") String nombre,
            @RequestParam("titulo") String titulo,             
            @RequestParam("descripcion") String descripcion,   
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "url", required = false) String url) {
        try {
            Promocion promocion = new Promocion();
            promocion.setNombre(nombre);
            promocion.setTitulo(titulo);             
            promocion.setDescripcion(descripcion);   
            promocion.setFechaSubida(LocalDateTime.now());

            if (file != null && !file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                promocion.setImagen("/uploads/" + fileName);
            } 
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

        // Endpoint para actualizar una promoción existente por su ID
    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarPromocion(
            @PathVariable Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "url", required = false) String url) {
        try {
            // 1. Verificar si la promoción existe
            Optional<Promocion> promocionOpt = promocionRepository.findById(id);
            if (!promocionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Promocion promocion = promocionOpt.get();
            promocion.setNombre(nombre);
            promocion.setTitulo(titulo);
            promocion.setDescripcion(descripcion);

            // 2. Controlar la actualización de la imagen
            if (file != null && !file.isEmpty()) {
                // Si sube un archivo nuevo, borramos el archivo físico anterior si existía
                String rutaAnterior = promocion.getImagen();
                if (rutaAnterior != null && rutaAnterior.startsWith("/uploads/")) {
                    File archivoViejo = new File(uploadDir + rutaAnterior.replace("/uploads/", ""));
                    if (archivoViejo.exists()) {
                        archivoViejo.delete();
                    }
                }

                // Guardamos el nuevo archivo físico
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                promocion.setImagen("/uploads/" + fileName);
            } 
            else if (url != null && !url.isEmpty()) {
                promocion.setImagen(url);
            }
            // Si no envía ni archivo ni URL, mantiene la imagen que ya tenía guardada

            // 3. Guardar los cambios
            promocionRepository.save(promocion);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "🔄 Promoción actualizada con éxito"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Error al actualizar: " + e.getMessage()));
        }
    }


    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "El ID no puede ser nulo"));
            }
            
            Optional<Promocion> promocionOpt = promocionRepository.findById(id);
            if (promocionOpt.isPresent()) {
                Promocion promocion = promocionOpt.get();
                String rutaImagen = promocion.getImagen();

                if (rutaImagen != null && rutaImagen.startsWith("/uploads/")) {
                    String nombreArchivo = rutaImagen.replace("/uploads/", "");
                    File archivo = new File(uploadDir + nombreArchivo);
                    if (archivo.exists()) {
                        archivo.delete();
                    }
                }
                
                promocionRepository.deleteById(id);
                return ResponseEntity.ok(Collections.singletonMap("mensaje", "Eliminado correctamente"));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "No se pudo eliminar: " + e.getMessage()));
        }
    }
}
