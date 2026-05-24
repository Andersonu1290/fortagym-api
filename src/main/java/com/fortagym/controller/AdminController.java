package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.model.Rol;
import com.fortagym.repository.UsuarioRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/admin") // ⬅️ CAMBIADO: Ahora coincide con tu ruta de Angular
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UsuarioRepository usuarioRepository;

    public AdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ============================
    // 1. OBTENER LISTA DE USUARIOS
    // ============================
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> verUsuarios() {
        logger.info("Enviando listado de usuarios (JSON) para panel de admin");
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        // Devolvemos la lista con un código HTTP 200 (OK)
        return ResponseEntity.ok(usuarios);
    }

    // ============================
    // 2. CAMBIAR ROL DE USUARIO
    // ============================
    @PutMapping("/cambiar-rol/{id}") // ⬅️ Usamos PUT para actualizar datos
    public ResponseEntity<?> cambiarRol(@PathVariable Long id, @RequestParam Rol rol) {
        logger.info("Intentando cambiar rol del usuario ID={} a {}", id, rol);

        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(404).body(new MensajeResponse("Usuario no encontrado"));
        }

        usuario.setRol(rol);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new MensajeResponse("Rol actualizado correctamente a " + rol));
    }

    // ============================
    // 3. ELIMINAR USUARIO
    // ============================
    @DeleteMapping("/eliminar/{id}") // ⬅️ Usamos DELETE para borrar
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        logger.warn("Solicitud de eliminación del usuario ID={}", id);

        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(404).body(new MensajeResponse("Usuario no encontrado"));
        }

        usuarioRepository.deleteById(id);
        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado exitosamente"));
    }
}