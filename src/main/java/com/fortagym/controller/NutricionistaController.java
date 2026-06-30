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
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/nutricionistas")
public class NutricionistaController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${app.backend.url}")
    private String backendUrl;

    @GetMapping
    public ResponseEntity<?> listarNutricionistas() {
        String sql = "SELECT u.id, u.nombre, u.apellido, np.especialidad, np.descripcion " +
                     "FROM usuarios u JOIN nutricionista_perfil np ON u.id = np.usuario_id " +
                     "WHERE u.rol = 'NUTRICIONISTA'";
        
        List<Map<String, Object>> bdNutricionistas = jdbcTemplate.queryForList(sql);
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (Map<String, Object> n : bdNutricionistas) {
            Map<String, Object> nutri = new HashMap<>();
            nutri.put("id", n.get("id"));
            nutri.put("nombre", n.get("nombre") + " " + n.get("apellido"));
            nutri.put("especialidad", n.get("especialidad"));
            nutri.put("descripcion", n.get("descripcion"));
            nutri.put("fotoUrl", backendUrl + "/api/usuarios/foto/" + n.get("id"));

            String sqlHorarios = "SELECT * FROM horarios_nutricionista WHERE nutricionista_id = ?";
            List<Map<String, Object>> bdHorarios = jdbcTemplate.queryForList(sqlHorarios, n.get("id"));
            
            List<Map<String, Object>> horariosList = new ArrayList<>();
            for (Map<String, Object> h : bdHorarios) {
                Map<String, Object> horario = new HashMap<>();
                horario.put("id", h.get("id"));
                horario.put("dia", h.get("dia"));
                horario.put("hora", h.get("hora"));
                horario.put("disponible", h.get("disponible"));

                Map<String, Object> detalle = new HashMap<>();
                detalle.put("duracionMinutos", h.get("duracion_minutos"));
                detalle.put("descripcion", h.get("descripcion"));
                detalle.put("temas", Arrays.asList(h.get("temas").toString().split(",")));
                
                horario.put("detalle", detalle);
                horariosList.add(horario);
            }
            nutri.put("horarios", horariosList);
            respuesta.add(nutri);
        }
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/reservar/{horarioId}")
    @Transactional
    public ResponseEntity<?> reservarConsulta(@PathVariable Long horarioId, @RequestBody Map<String, Object> datosBasicos, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Collections.singletonMap("error", "No autorizado"));

        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElseThrow();

        if (usuario.getMembresiaActiva() == null) {
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Debes tener una membresía activa para reservar consultas nutricionales."));
        }

        String sqlCheck = "SELECT h.dia, h.hora, h.descripcion, h.disponible, u.nombre, u.apellido " +
                          "FROM horarios_nutricionista h JOIN usuarios u ON h.nutricionista_id = u.id WHERE h.id = ?";
        Map<String, Object> horarioDb = jdbcTemplate.queryForMap(sqlCheck, horarioId);

        if (!(Boolean) horarioDb.get("disponible")) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Horario no disponible."));
        }

        double peso = Double.parseDouble(datosBasicos.get("pesoKg").toString());
        double altura = Double.parseDouble(datosBasicos.get("alturaCm").toString());
        String objetivo = datosBasicos.get("objetivo").toString();
        
        // Simulamos la reserva para el día siguiente
        LocalDateTime fechaSesion = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);

        // 1. Guardar Reserva Nutricional
        jdbcTemplate.update("INSERT INTO reservas_nutricion (usuario_id, horario_id, peso_kg, altura_cm, objetivo, fecha_hora_sesion) VALUES (?, ?, ?, ?, ?, ?)",
                usuario.getId(), horarioId, peso, altura, objetivo, fechaSesion);

        // 2. Marcar Ocupado
        jdbcTemplate.update("UPDATE horarios_nutricionista SET disponible = false WHERE id = ?", horarioId);

        // 3. Añadir al Calendario Global
        String nombreNutri = horarioDb.get("nombre") + " " + horarioDb.get("apellido");
        String hora = horarioDb.get("hora").toString();
        
        jdbcTemplate.update("INSERT INTO eventos_calendario (usuario_id, titulo, fecha, tipo, hora, nombre_entrenador, descripcion) VALUES (?, ?, ?, 'nutricion', ?, ?, ?)",
                usuario.getId(), "Consulta: " + objetivo, fechaSesion.toLocalDate().toString(), hora, nombreNutri, "Consulta Nutricional: " + horarioDb.get("descripcion"));

        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Reserva confirmada."));
    }
}