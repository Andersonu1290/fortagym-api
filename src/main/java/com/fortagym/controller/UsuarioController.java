package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import com.fortagym.service.UsuarioService;
import com.fortagym.service.EmailAlreadyExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // =======================
    // 1. REGISTRO
    // =======================
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrar(usuario);
            // Gracias a @JsonIgnore en el Modelo, no es necesario hacer setPassword(null)
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (EmailAlreadyExistsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // =======================
    // 2. OBTENER PERFIL
    // =======================
    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensajeResponse("No autorizado"));
        }
        
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        return ResponseEntity.ok(usuario);
    }

    // =======================
    // 3. ACTUALIZAR PERFIL
    // =======================
    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> datosActualizados, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (datosActualizados.containsKey("nombre")) {
            usuario.setNombre(datosActualizados.get("nombre"));
        }
        if (datosActualizados.containsKey("apellido")) {
            usuario.setApellido(datosActualizados.get("apellido"));
        }
        if (datosActualizados.containsKey("password") && !datosActualizados.get("password").isBlank()) {
            usuarioService.actualizarPassword(usuario, datosActualizados.get("password"));
        }

        usuarioService.guardar(usuario);
        return ResponseEntity.ok(usuario);
    }

    // =======================
    // 4. SUBIR FOTO
    // =======================
    @PostMapping("/perfil/foto")
    public ResponseEntity<?> subirFotoPerfil(@RequestParam("foto") MultipartFile archivo, Principal principal) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(new MensajeResponse("Selecciona una imagen válida."));
        }
        try {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            usuario.setFotoPerfil(archivo.getBytes());
            usuarioService.guardar(usuario);
            return ResponseEntity.ok(new MensajeResponse("Foto actualizada correctamente."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MensajeResponse("Error al guardar la imagen."));
        }
    }

    // =======================
    // 5. OBTENER FOTO
    // =======================
    @GetMapping("/foto/{id}")
    public ResponseEntity<byte[]> mostrarFoto(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null || usuario.getFotoPerfil() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(usuario.getFotoPerfil());
    }

    // =======================
    // 6. OBTENER POR ID
    // =======================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeResponse("Usuario no encontrado"));
        }
        
        return ResponseEntity.ok(usuario);
    }
}