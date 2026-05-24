package com.fortagym.controller;

import com.fortagym.model.DetalleRutina;
import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;
import com.fortagym.repository.DetalleRutinaRepository;
import com.fortagym.repository.NutricionRepository;
import com.fortagym.service.RutinaService;
import com.fortagym.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional; // 🔥 Vital

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@RestController 
@RequestMapping("/api/rutinas")
public class RutinaController {

    private static final Logger logger = LoggerFactory.getLogger(RutinaController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private NutricionRepository nutricionRepository;

    @Autowired
    private DetalleRutinaRepository detalleRutinaRepository;

    // 1. ESTADO DE USUARIOS (Para la lista del Entrenador)
    @GetMapping("/usuarios-estado")
    public ResponseEntity<List<Map<String, Object>>> listarUsuariosEstado() {
        List<Usuario> usuarios = usuarioService.obtenerSoloUsuarios();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Usuario u : usuarios) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("nombre", u.getNombre() + " " + u.getApellido());
            map.put("email", u.getEmail());
            map.put("dni", u.getDni()); // 🚀 ¡ESTA ES LA LÍNEA QUE LO ARREGLA TODO!
            map.put("tieneNutricion", nutricionRepository.existsByUsuarioId(u.getId()));
            map.put("tieneRutina", rutinaService.buscarPorUsuario(u).isPresent());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    // 2. OBTENER RUTINA (Con sus detalles cargados)
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerRutinaPorUsuario(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioService.findById(usuarioId);
        if (usuario == null) return ResponseEntity.notFound().build();

        Optional<Rutina> rutinaOpt = rutinaService.buscarPorUsuario(usuario);
        if (rutinaOpt.isEmpty()) {
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Sin rutina"));
        }

        Rutina rutina = rutinaOpt.get();
        // Aseguramos que los detalles vengan en el JSON
        rutina.setDetalles(detalleRutinaRepository.findByRutina(rutina));
        return ResponseEntity.ok(rutina);
    }

    // 3. GUARDAR / EDITAR / ELIMINAR DETALLES
    @PostMapping("/guardar")
    @Transactional // 🔥 Esto garantiza que si algo falla, no se borre nada
    public ResponseEntity<?> guardarRutina(@RequestBody Map<String, Object> payload) {
        try {
            // Extraemos los datos del Map para evitar errores de casteo de objetos anidados
            Long usuarioId = Long.valueOf(payload.get("usuarioId").toString());
            String coach = payload.get("nombreEntrenador").toString();
            List<Map<String, String>> detallesInput = (List<Map<String, String>>) payload.get("detalles");

            logger.info("📌 Procesando rutina para Usuario ID: {}", usuarioId);

            Usuario usuario = usuarioService.findById(usuarioId);
            if (usuario == null) return ResponseEntity.badRequest().body("Usuario no encontrado");

            // BUSCAR SI YA EXISTE (PARA EDITAR) O CREAR NUEVA
            Rutina rutina = rutinaService.buscarPorUsuario(usuario).orElse(new Rutina());
            
            rutina.setUsuario(usuario);
            rutina.setNombreEntrenador(coach);
            // La base de datos pide observaciones NOT NULL
            rutina.setObservaciones("Rutina actualizada por coach: " + coach);

            // 🔥 EL TRUCO PARA ELIMINAR DE VERDAD:
            // Si la rutina ya existe, borramos TODOS sus detalles actuales de la DB.
            // Luego insertaremos los que vienen de Angular. Así, si borraste uno en el front, 
            // desaparecerá de la base de datos definitivamente.
            if (rutina.getId() != null) {
                detalleRutinaRepository.deleteByRutina(rutina);
                rutina.getDetalles().clear();
            }

            // Guardamos la cabecera para asegurar que tenemos un ID
            Rutina rutinaGuardada = rutinaService.guardar(rutina);

            // INSERTAR LOS NUEVOS DETALLES (AGREGAR / EDITAR)
            if (detallesInput != null) {
                for (Map<String, String> d : detallesInput) {
                    DetalleRutina nuevoDetalle = new DetalleRutina();
                    nuevoDetalle.setEjercicio(d.get("ejercicio"));
                    nuevoDetalle.setSeriesReps(d.get("seriesReps"));
                    nuevoDetalle.setDescanso(d.get("descanso"));
                    nuevoDetalle.setDias(d.get("dias"));
                    nuevoDetalle.setRutina(rutinaGuardada);
                    detalleRutinaRepository.save(nuevoDetalle);
                }
            }

            return ResponseEntity.ok(Collections.singletonMap("mensaje", "✅ Rutina procesada con éxito"));

        } catch (Exception e) {
            logger.error("❌ Error al guardar rutina: ", e);
            return ResponseEntity.internalServerError().body("Error en el servidor: " + e.getMessage());
        }
    }
}