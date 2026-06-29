package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/entrenadores")
public class EntrenadorController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Obtener todos los entrenadores con sus horarios para Angular
    @GetMapping
    public ResponseEntity<?> listarEntrenadoresDisponibles() {
        // Consulta que une usuarios (entrenadores) con su perfil
        String sqlEntrenadores = "SELECT u.id, u.nombre, u.apellido, ep.especialidad, ep.descripcion " +
                                 "FROM usuarios u JOIN entrenador_perfil ep ON u.id = ep.usuario_id " +
                                 "WHERE u.rol = 'ENTRENADOR'";
        
        List<Map<String, Object>> entrenadoresDB = jdbcTemplate.queryForList(sqlEntrenadores);
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (Map<String, Object> e : entrenadoresDB) {
            Map<String, Object> entrenador = new HashMap<>();
            entrenador.put("id", e.get("id"));
            entrenador.put("nombre", e.get("nombre") + " " + e.get("apellido"));
            entrenador.put("especialidad", e.get("especialidad"));
            entrenador.put("descripcion", e.get("descripcion"));
            entrenador.put("fotoUrl", "http://localhost:8089/api/usuarios/foto/" + e.get("id")); // Ruta dinámica de la foto

            // Buscar los horarios de este entrenador
            String sqlHorarios = "SELECT * FROM horarios_entrenador WHERE entrenador_id = ?";
            List<Map<String, Object>> horariosDB = jdbcTemplate.queryForList(sqlHorarios, e.get("id"));
            
            List<Map<String, Object>> horariosList = new ArrayList<>();
            for (Map<String, Object> h : horariosDB) {
                Map<String, Object> horario = new HashMap<>();
                horario.put("id", h.get("id"));
                horario.put("dia", h.get("dia"));
                horario.put("hora", h.get("hora"));
                horario.put("disponible", h.get("disponible"));

                Map<String, Object> sesion = new HashMap<>();
                sesion.put("duracionMinutos", h.get("duracion_minutos"));
                sesion.put("descripcion", h.get("descripcion"));
                // Convertimos el string separado por comas en un Array para Angular
                sesion.put("ejercicios", Arrays.asList(h.get("ejercicios").toString().split(",")));
                
                horario.put("sesion", sesion);
                horariosList.add(horario);
            }
            entrenador.put("horarios", horariosList);
            respuesta.add(entrenador);
        }
        return ResponseEntity.ok(respuesta);
    }

    // 2. Procesar la reserva de un horario
    @PostMapping("/reservar/{horarioId}")
    @Transactional
    public ResponseEntity<?> reservarHorario(@PathVariable Long horarioId, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Collections.singletonMap("error", "No autorizado"));

        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 🔥 VALIDACIÓN DE MEMBRESÍA: Solo si tiene membresía activa puede reservar
        if (usuario.getMembresiaActiva() == null) {
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Debes adquirir una membresía activa para reservar entrenadores."));
        }

        // Verificar si el horario sigue disponible
        String sqlCheck = "SELECT disponible FROM horarios_entrenador WHERE id = ?";
        Boolean disponible = jdbcTemplate.queryForObject(sqlCheck, Boolean.class, horarioId);

        if (disponible == null || !disponible) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "El horario ya no está disponible."));
        }

        // Registrar la reserva y marcar el horario como ocupado
        jdbcTemplate.update("INSERT INTO reservas_entrenamiento (usuario_id, horario_id) VALUES (?, ?)", usuario.getId(), horarioId);
        jdbcTemplate.update("UPDATE horarios_entrenador SET disponible = false WHERE id = ?", horarioId);

        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Reserva confirmada con éxito."));
    }
}