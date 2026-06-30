package com.fortagym.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/calendario")
public class EventoController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/mis-eventos")
    public ResponseEntity<?> obtenerMisEventos(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
    
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
        String sql = "SELECT titulo, fecha, tipo, hora, nombre_entrenador, descripcion " +
                     "FROM eventos_calendario WHERE usuario_id = ?";
    
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql, usuario.getId());
        List<Map<String, String>> eventosProcesados = new ArrayList<>();
    
        for (Map<String, Object> fila : filas) {
        
            Map<String, String> evento = new HashMap<>();
        
            evento.put("titulo",
                    fila.get("titulo") != null ? fila.get("titulo").toString() : "Sin título");
        
            evento.put("fecha",
                    fila.get("fecha") != null ? fila.get("fecha").toString() : "");
        
            evento.put("tipo",
                    fila.get("tipo") != null ? fila.get("tipo").toString() : "");
        
            evento.put("hora",
                    fila.get("hora") != null ? fila.get("hora").toString() : "No especificada");
        
            // Este campo sirve para entrenador o nutricionista
            evento.put("profesional",
                    fila.get("nombre_entrenador") != null
                            ? fila.get("nombre_entrenador").toString()
                            : "Por asignar");
        
            evento.put("descripcion",
                    fila.get("descripcion") != null
                            ? fila.get("descripcion").toString()
                            : "Sin detalles disponibles.");
        
            eventosProcesados.add(evento);
        }
    
        return ResponseEntity.ok(eventosProcesados);
    }
}