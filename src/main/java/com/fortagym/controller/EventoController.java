package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendario")
@CrossOrigin(origins = "http://localhost:4200")
public class EventoController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/mis-eventos")
    public ResponseEntity<?> obtenerMisEventos(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        
        // 🌟 Consulta SQL robusta
        String sql = "SELECT titulo, fecha, tipo FROM eventos_calendario WHERE usuario_id = ?";
        
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(sql, usuario.getId());
        List<Map<String, String>> eventosProcesados = new ArrayList<>();

        // Forzamos a que las llaves sean minúsculas para que Angular no se maree
        for (Map<String, Object> fila : filas) {
            Map<String, String> evento = new HashMap<>();
            evento.put("titulo", fila.get("titulo").toString());
            evento.put("fecha", fila.get("fecha").toString()); // YYYY-MM-DD
            evento.put("tipo", fila.get("tipo").toString());
            eventosProcesados.add(evento);
        }
        
        return ResponseEntity.ok(eventosProcesados);
    }
}