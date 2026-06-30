package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
}