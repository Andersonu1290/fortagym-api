package com.fortagym.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;

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
            entrenador.put("fotoUrl", "/api/usuarios/foto/" + e.get("id"));

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
    
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Collections.singletonMap("error", "No autorizado"));
        }
    
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
        // ============================
        // 1. VALIDAR MEMBRESÍA
        // ============================
        if (usuario.getMembresiaActiva() == null) {
            return ResponseEntity.status(403)
                    .body(Collections.singletonMap("error",
                            "Debes adquirir una membresía para reservar."));
        }
    
        // ============================
        // 2. VALIDAR LÍMITE DE RESERVAS
        // ============================
        String sqlCount = """
            SELECT COUNT(*)
            FROM reservas_entrenamiento
            WHERE usuario_id = ?
              AND MONTH(fecha_reserva) = MONTH(CURRENT_DATE())
              AND YEAR(fecha_reserva) = YEAR(CURRENT_DATE())
            """;
    
        Integer reservasEsteMes = jdbcTemplate.queryForObject(
                sqlCount,
                Integer.class,
                usuario.getId()
        );
    
        int limite = usuario.getMembresiaActiva().getLimiteReservasMensuales() != null
                ? usuario.getMembresiaActiva().getLimiteReservasMensuales()
                : 0;
    
        if (reservasEsteMes != null && reservasEsteMes >= limite) {
            return ResponseEntity.status(403)
                    .body(Collections.singletonMap(
                            "error",
                            "Has alcanzado tu límite de "
                                    + limite
                                    + " reservas este mes con el "
                                    + usuario.getMembresiaActiva().getTipo()
                    ));
        }
    
        // ============================
        // 3. OBTENER INFORMACIÓN DEL HORARIO
        // ============================
        String sqlCheck = """
            SELECT
                h.dia,
                h.hora,
                h.descripcion,
                h.disponible,
                CONCAT(u.nombre,' ',u.apellido) AS entrenador
            FROM horarios_entrenador h
            JOIN usuarios u
                ON h.entrenador_id = u.id
            WHERE h.id = ?
            """;
    
        Map<String, Object> horarioDb = jdbcTemplate.queryForMap(sqlCheck, horarioId);
    
        Boolean disponible = (Boolean) horarioDb.get("disponible");
    
        if (disponible == null || !disponible) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap(
                            "error",
                            "El horario ya no está disponible."
                    ));
        }
    
        String tituloEvento = "Entrenamiento: " + horarioDb.get("descripcion");
    
        // Ejemplo: reserva para dentro de 2 días
        java.time.LocalDateTime fechaSesion = java.time.LocalDateTime.now()
                .plusDays(2)
                .withHour(8)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    
        // ============================
        // 4. GUARDAR RESERVA
        // ============================
        jdbcTemplate.update(
                "INSERT INTO reservas_entrenamiento (usuario_id, horario_id, fecha_hora_sesion, estado) VALUES (?, ?, ?, 'CONFIRMADA')",
                usuario.getId(),
                horarioId,
                fechaSesion
        );
    
        // ============================
        // 5. MARCAR HORARIO COMO OCUPADO
        // ============================
        jdbcTemplate.update(
                "UPDATE horarios_entrenador SET disponible = false WHERE id = ?",
                horarioId
        );
    
        // ============================
        // 6. INSERTAR EN EL CALENDARIO
        // ============================
        String fechaCalendario = fechaSesion.toLocalDate().toString();
        String hora = horarioDb.get("hora").toString();
        String nombreEntrenador = horarioDb.get("entrenador").toString();
    
        jdbcTemplate.update(
                "INSERT INTO eventos_calendario (usuario_id, titulo, fecha, tipo, hora, nombre_entrenador) VALUES (?, ?, ?, ?, ?, ?)",
                usuario.getId(),
                tituloEvento,
                fechaCalendario,
                "entrenamiento",
                hora,
                nombreEntrenador
        );
    
        return ResponseEntity.ok(
                Collections.singletonMap(
                        "mensaje",
                        "✅ Reserva confirmada. Revisa tu calendario."
                )
        );
   }
   // ==========================================
    // PANEL DEL ENTRENADOR: GESTIÓN DE HORARIOS
    // ==========================================

    // 1. OBTENER MIS HORARIOS
    @GetMapping("/mis-horarios")
    public ResponseEntity<?> obtenerMisHorarios(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        
        String sql = "SELECT * FROM horarios_entrenador WHERE entrenador_id = ? ORDER BY FIELD(dia, 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'), hora";
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql, usuario.getId()));
    }

    // 2. CREAR O EDITAR HORARIO
    @PostMapping("/mis-horarios")
    @Transactional
    public ResponseEntity<?> guardarHorario(@RequestBody Map<String, Object> payload, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        
        String dia = payload.get("dia").toString();
        String hora = payload.get("hora").toString();
        int duracion = Integer.parseInt(payload.get("duracion_minutos").toString());
        String descripcion = payload.get("descripcion").toString();
        String ejercicios = payload.get("ejercicios").toString();
        
        if (payload.get("id") != null) {
            // EDITAR
            Long id = Long.valueOf(payload.get("id").toString());
            String sql = "UPDATE horarios_entrenador SET dia=?, hora=?, duracion_minutos=?, descripcion=?, ejercicios=? WHERE id=? AND entrenador_id=?";
            jdbcTemplate.update(sql, dia, hora, duracion, descripcion, ejercicios, id, usuario.getId());
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Horario actualizado con éxito"));
        } else {
            // CREAR NUEVO
            String sql = "INSERT INTO horarios_entrenador (entrenador_id, dia, hora, duracion_minutos, descripcion, ejercicios, disponible) VALUES (?, ?, ?, ?, ?, ?, true)";
            jdbcTemplate.update(sql, usuario.getId(), dia, hora, duracion, descripcion, ejercicios);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Nuevo horario creado"));
        }
    }

    // 3. CAMBIAR ESTADO (DISPONIBLE / NO DISPONIBLE)
    @PutMapping("/mis-horarios/{id}/estado")
    @Transactional
    public ResponseEntity<?> cambiarEstadoHorario(@PathVariable Long id, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        
        String sql = "UPDATE horarios_entrenador SET disponible = NOT disponible WHERE id = ? AND entrenador_id = ?";
        int afectadas = jdbcTemplate.update(sql, id, usuario.getId());
        
        if(afectadas > 0) return ResponseEntity.ok(Collections.singletonMap("mensaje", "Estado actualizado"));
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No se pudo actualizar"));
    }

    // 4. ELIMINAR HORARIO
    @DeleteMapping("/mis-horarios/{id}")
    @Transactional
    public ResponseEntity<?> eliminarHorario(@PathVariable Long id, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        String sql = "DELETE FROM horarios_entrenador WHERE id = ? AND entrenador_id = ?";
        jdbcTemplate.update(sql, id, usuario.getId());
        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Horario eliminado"));
    }
}