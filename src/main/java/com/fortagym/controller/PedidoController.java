package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/tienda/pedidos")
public class PedidoController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<?> obtenerMisPedidos(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // 1. OBTENEMOS EL PEDIDO Y UNIMOS CON EL USUARIO PARA SUS DATOS PERSONALES
        String sqlPedidos = """
            SELECT p.*, u.nombre AS u_nombre, u.apellido AS u_apellido, u.email AS u_correo 
            FROM pago_tienda p 
            JOIN usuarios u ON p.usuario_id = u.id 
            WHERE p.usuario_id = ? 
            ORDER BY p.fecha_compra DESC
        """;
        
        List<Map<String, Object>> pedidosRaw = jdbcTemplate.queryForList(sqlPedidos, usuario.getId());
        List<Map<String, Object>> respuesta = new ArrayList<>();
        
        for (Map<String, Object> p : pedidosRaw) {
            Map<String, Object> pedido = new HashMap<>();
            
            // 🔥 TRADUCCIÓN EXACTA A LO QUE PIDE EL FRONTEND (historial-compras.ts)
            pedido.put("id", p.get("id"));
            pedido.put("numeroOrden", p.get("numero_orden"));
            pedido.put("fechaCreacion", p.get("fecha_compra"));
            
            // Mapeo del estado para que los iconos y colores en Angular funcionen
            String estadoDB = p.get("estado_pedido") != null ? p.get("estado_pedido").toString().toUpperCase() : "";
            String estadoFront = "procesando"; // Por defecto
            
            if(estadoDB.equals("EN CAMINO") || estadoDB.equals("ENVIADO")) estadoFront = "en_camino";
            else if(estadoDB.equals("ENTREGADO")) estadoFront = "entregado";
            else if(estadoDB.equals("CANCELADO")) estadoFront = "cancelado";
            
            pedido.put("estado", estadoFront);
            
            // Totales
            pedido.put("subtotal", p.get("subtotal"));
            pedido.put("costoEnvio", p.get("costo_envio") != null ? p.get("costo_envio") : 0);
            pedido.put("descuento", p.get("descuento") != null ? p.get("descuento") : 0);
            pedido.put("igv", p.get("igv"));
            pedido.put("total", p.get("total_pagado"));
            
            // Datos del Cliente (Traídos del JOIN con usuarios)
            pedido.put("nombreCliente", p.get("u_nombre") + " " + p.get("u_apellido"));
            pedido.put("correo", p.get("u_correo"));
            
            // Datos de envío y pago
            pedido.put("metodoEntrega", p.get("metodo_entrega"));
            pedido.put("direccion", p.get("direccion_envio"));
            pedido.put("distrito", p.get("distrito"));
            pedido.put("departamento", p.get("departamento"));
            pedido.put("metodoPago", p.get("metodo_pago"));
            
            // 2. OBTENEMOS LOS DETALLES Y UNIMOS CON PRODUCTOS PARA TRAER LA FOTO Y EL NOMBRE
            String sqlItems = """
                SELECT d.*, pr.nombre, pr.categoria, pr.img 
                FROM detalle_pago_tienda d 
                JOIN productos pr ON d.producto_id = pr.id 
                WHERE d.pago_tienda_id = ?
            """;
            List<Map<String, Object>> itemsRaw = jdbcTemplate.queryForList(sqlItems, p.get("id"));
            
            List<Map<String, Object>> itemsProcesados = new ArrayList<>();
            for (Map<String, Object> i : itemsRaw) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", i.get("producto_id"));
                item.put("nombre", i.get("nombre"));           // Viene de tabla productos
                item.put("categoria", i.get("categoria"));     // Viene de tabla productos
                item.put("img", i.get("img"));                 // Viene de tabla productos
                item.put("precio", i.get("precio_unitario"));  // Traducido para Angular
                item.put("cantidad", i.get("cantidad"));
                itemsProcesados.add(item);
            }
            
            pedido.put("items", itemsProcesados);
            respuesta.add(pedido);
        }
        
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        // Actualizamos el estado a CANCELADO en la base de datos
        String sql = "UPDATE pago_tienda SET estado_pedido = 'CANCELADO' WHERE id = ?";
        int filasAfectadas = jdbcTemplate.update(sql, id);

        if (filasAfectadas > 0) {
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Pedido cancelado con éxito"));
        } else {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No se pudo cancelar"));
        }
    }
}