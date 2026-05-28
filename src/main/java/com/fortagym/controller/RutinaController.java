package com.fortagym.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fortagym.model.DetalleRutina;
import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;
import com.fortagym.repository.DetalleRutinaRepository;
import com.fortagym.repository.NutricionRepository;
import com.fortagym.service.RutinaService;
import com.fortagym.service.UsuarioService;

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
            map.put("dni", u.getDni());
            map.put("tieneNutricion", nutricionRepository.existsByUsuarioId(u.getId()));
            

            // Evaluamos si la rutina existe Y ADEMÁS tiene al menos 1 detalle adentro
            Optional<Rutina> rutinaOpt = rutinaService.buscarPorUsuario(u);
            boolean tieneRutinaReal = rutinaOpt.isPresent() && 
                                      rutinaOpt.get().getDetalles() != null && 
                                      !rutinaOpt.get().getDetalles().isEmpty();
            
            map.put("tieneRutina", tieneRutinaReal);
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
    @Transactional // 🔥 Garantiza que los cambios se confirmen como una transacción atómica
    public ResponseEntity<?> guardarRutina(@RequestBody Map<String, Object> payload) {
        try {
            // Extraemos los datos del Map de forma segura
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
            rutina.setObservaciones("Rutina actualizada por coach: " + coach);

            // GESTIÓN DE COLECCIÓN INTEGRAL PARA HIBERNATE:
            if (rutina.getDetalles() == null) {
                rutina.setDetalles(new ArrayList<>());
            } else {
                // Al limpiar la lista, 'orphanRemoval = true' de JPA remueve automáticamente 
                // de la base de datos los registros huérfanos sin entrar en conflicto con el contexto
                rutina.getDetalles().clear();
            }

            // INSERTAR LOS NUEVOS DETALLES DIRECTAMENTE A LA ENTIDAD PADRE
            if (detallesInput != null) {
                for (Map<String, String> d : detallesInput) {
                    
                    // 1. EXTRAER DE FORMA SEGURA (Evita ClassCastException si Angular envía números)
                    String ejercicioStr = d.get("ejercicio") != null ? String.valueOf(d.get("ejercicio")).trim() : "";
                    String seriesStr = d.get("seriesReps") != null ? String.valueOf(d.get("seriesReps")).trim() : "";
                    String descansoStr = d.get("descanso") != null ? String.valueOf(d.get("descanso")).trim() : "";
                    String diasStr = d.get("dias") != null ? String.valueOf(d.get("dias")).trim() : "";

                    // 2. IGNORAR FILAS VACÍAS: Si Angular envía una fila sin nombre de ejercicio, la saltamos
                    if (ejercicioStr.isEmpty()) {
                        continue; 
                    }

                    // 3. ASIGNAR LOS DATOS LIMPIOS (Sin espacios fantasma que rompan tus @Pattern)
                    DetalleRutina nuevoDetalle = new DetalleRutina();
                    nuevoDetalle.setEjercicio(ejercicioStr);
                    nuevoDetalle.setSeriesReps(seriesStr);
                    nuevoDetalle.setDescanso(descansoStr);
                    nuevoDetalle.setDias(diasStr);
                    nuevoDetalle.setRutina(rutina); 
                    
                    rutina.getDetalles().add(nuevoDetalle);
                }
            }

            // Al guardar la entidad raíz (Rutina), las operaciones se propagan en cascada (CascadeType.ALL)
            rutinaService.guardar(rutina);

            return ResponseEntity.ok(Collections.singletonMap("mensaje", "✅ Rutina procesada con éxito"));

        } catch (Exception e) {
            logger.error("❌ Error al guardar rutina: ", e);
            return ResponseEntity.internalServerError().body("Error en el servidor: " + e.getMessage());
        }
    }
}