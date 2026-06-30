package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.model.Rol;
import com.fortagym.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate; // 🔥 Añade esto debajo de tus otros repositorios

    // ============================
    // 4. OBTENER TODOS LOS PEDIDOS (TIENDA)
    // ============================
    @GetMapping("/pedidos")
    public ResponseEntity<?> obtenerTodosLosPedidos() {
        logger.info("Admin solicitando todos los pedidos de la tienda");
        
        String sqlPedidos = """
            SELECT p.*, u.nombre AS u_nombre, u.apellido AS u_apellido, u.email AS u_correo 
            FROM pago_tienda p 
            JOIN usuarios u ON p.usuario_id = u.id 
            ORDER BY p.fecha_compra DESC
        """;
        
        List<java.util.Map<String, Object>> pedidosRaw = jdbcTemplate.queryForList(sqlPedidos);
        List<java.util.Map<String, Object>> respuesta = new java.util.ArrayList<>();
        
        for (java.util.Map<String, Object> p : pedidosRaw) {
            java.util.Map<String, Object> pedido = new java.util.HashMap<>();
            
            pedido.put("id", p.get("id"));
            pedido.put("numeroOrden", p.get("numero_orden"));
            pedido.put("fechaCreacion", p.get("fecha_compra"));
            
            // Traducción de BD a Angular
            String estadoDB = p.get("estado_pedido") != null ? p.get("estado_pedido").toString().toUpperCase() : "RECIBIDO";
            String estadoFront = "procesando"; 
            if(estadoDB.equals("EN CAMINO") || estadoDB.equals("ENVIADO")) estadoFront = "en_camino";
            else if(estadoDB.equals("ENTREGADO")) estadoFront = "entregado";
            else if(estadoDB.equals("CANCELADO")) estadoFront = "cancelado";
            
            pedido.put("estado", estadoFront);
            pedido.put("total", p.get("total_pagado"));
            pedido.put("nombreCliente", p.get("u_nombre") + " " + p.get("u_apellido"));
            pedido.put("correo", p.get("u_correo"));
            pedido.put("metodoEntrega", p.get("metodo_entrega"));
            pedido.put("direccion", p.get("direccion_envio"));
            pedido.put("metodoPago", p.get("metodo_pago"));
            
            // Obtener los productos comprados
            String sqlItems = "SELECT d.*, pr.nombre, pr.img FROM detalle_pago_tienda d JOIN productos pr ON d.producto_id = pr.id WHERE d.pago_tienda_id = ?";
            List<java.util.Map<String, Object>> itemsRaw = jdbcTemplate.queryForList(sqlItems, p.get("id"));
            List<java.util.Map<String, Object>> itemsProcesados = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> i : itemsRaw) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("nombre", i.get("nombre"));
                item.put("img", i.get("img"));
                item.put("precio", i.get("precio_unitario"));
                item.put("cantidad", i.get("cantidad"));
                itemsProcesados.add(item);
            }
            pedido.put("items", itemsProcesados);
            respuesta.add(pedido);
        }
        return ResponseEntity.ok(respuesta);
    }

    // ============================
    // 5. CAMBIAR ESTADO DE UN PEDIDO
    // ============================
    @PutMapping("/pedidos/{id}/estado")
    public ResponseEntity<?> actualizarEstadoPedido(@PathVariable Long id, @RequestParam String nuevoEstado) {
        // Traducimos el estado de Angular al de Base de Datos
        String estadoDB = "RECIBIDO";
        if(nuevoEstado.equals("en_camino")) estadoDB = "EN CAMINO";
        else if(nuevoEstado.equals("entregado")) estadoDB = "ENTREGADO";
        else if(nuevoEstado.equals("cancelado")) estadoDB = "CANCELADO";

        String sql = "UPDATE pago_tienda SET estado_pedido = ? WHERE id = ?";
        jdbcTemplate.update(sql, estadoDB, id);
        
        return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", "Estado del pedido actualizado a " + estadoDB));
    }
    
}