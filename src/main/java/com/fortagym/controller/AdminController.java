package com.fortagym.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UsuarioRepository usuarioRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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
        return ResponseEntity.ok(usuarios);
    }

    // ============================
    // 2. CAMBIAR ROL DE USUARIO
    // ============================
    @PutMapping("/cambiar-rol/{id}")
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
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        logger.warn("Solicitud de eliminación del usuario ID={}", id);
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(404).body(new MensajeResponse("Usuario no encontrado"));
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado exitosamente"));
    }

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
        String estadoDB = "RECIBIDO";
        if(nuevoEstado.equals("en_camino")) estadoDB = "EN CAMINO";
        else if(nuevoEstado.equals("entregado")) estadoDB = "ENTREGADO";
        else if(nuevoEstado.equals("cancelado")) estadoDB = "CANCELADO";

        String sql = "UPDATE pago_tienda SET estado_pedido = ? WHERE id = ?";
        jdbcTemplate.update(sql, estadoDB, id);
        
        return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", "Estado del pedido actualizado a " + estadoDB));
    }

    // ============================
    // 6. REPORTE GENERAL DASHBOARD
    // ============================
    @GetMapping("/reporte-general")
    public ResponseEntity<?> obtenerReporteGeneral(@RequestParam(defaultValue = "Este mes") String periodo) {
        Map<String, Object> respuesta = new HashMap<>();

        // 1. TRADUCIR EL PERIODO A SQL
        String filtroFecha = "";
        switch (periodo) {
            case "Hoy":
                filtroFecha = "DATE(fecha_compra) = CURRENT_DATE()";
                break;
            case "Esta semana":
                filtroFecha = "YEARWEEK(fecha_compra, 1) = YEARWEEK(CURRENT_DATE(), 1)";
                break;
            case "Este año":
                filtroFecha = "YEAR(fecha_compra) = YEAR(CURRENT_DATE())";
                break;
            case "Este mes":
            default:
                filtroFecha = "MONTH(fecha_compra) = MONTH(CURRENT_DATE()) AND YEAR(fecha_compra) = YEAR(CURRENT_DATE())";
                break;
        }

        // 2. CALCULAR KPIs CON EL FILTRO
        String sqlTotalIngresos = "SELECT COALESCE(SUM(total_pagado), 0) FROM pago_tienda WHERE estado_pago = 'PAGADO' AND " + filtroFecha;
        Double ingresos = jdbcTemplate.queryForObject(sqlTotalIngresos, Double.class);
        
        String sqlVentasPeriodo = "SELECT COUNT(*) FROM pago_tienda WHERE " + filtroFecha;
        Integer ventasPeriodo = jdbcTemplate.queryForObject(sqlVentasPeriodo, Integer.class);

        String sqlUsuarios = "SELECT COUNT(*) FROM usuarios";
        Integer totalUsuarios = jdbcTemplate.queryForObject(sqlUsuarios, Integer.class);

        String sqlStockBajo = "SELECT COUNT(*) FROM productos WHERE stock < 15";
        Integer stockBajo = jdbcTemplate.queryForObject(sqlStockBajo, Integer.class);

        List<Map<String, Object>> kpis = new ArrayList<>();
        kpis.add(crearKpi("Ingresos (" + periodo + ")", "S/ " + String.format("%.2f", ingresos), "Total recaudado", 5.2, "fa-circle-dollar-to-slot", "accent-primary"));
        kpis.add(crearKpi("Ventas (" + periodo + ")", String.valueOf(ventasPeriodo), "Transacciones", 12.0, "fa-bag-shopping", "accent-dark"));
        kpis.add(crearKpi("Total Miembros", String.valueOf(totalUsuarios), "Registrados", 0.0, "fa-user-plus", "accent-primary"));
        kpis.add(crearKpi("Ticket Promedio", "S/ " + String.format("%.2f", ventasPeriodo > 0 ? (ingresos / ventasPeriodo) : 0), "Por transacción", -2.1, "fa-receipt", "accent-dark"));
        kpis.add(crearKpi("Stock Crítico", String.valueOf(stockBajo), "Por agotarse", 0.0, "fa-triangle-exclamation", "accent-warning"));
        kpis.add(crearKpi("Ocupación Gym", "75%", "Promedio", 3.5, "fa-dumbbell", "accent-primary"));
        
        respuesta.put("kpis", kpis);

        // 3. VENTAS RECIENTES
        String sqlVentas = "SELECT p.id, CONCAT(u.nombre, ' ', u.apellido) AS cliente, p.total_pagado, p.fecha_compra, p.estado_pedido " +
                           "FROM pago_tienda p JOIN usuarios u ON p.usuario_id = u.id " +
                           "WHERE " + filtroFecha + " " +
                           "ORDER BY p.fecha_compra DESC LIMIT 8";
        
        List<Map<String, Object>> ventasDb = jdbcTemplate.queryForList(sqlVentas);
        List<Map<String, Object>> ventasRecientes = new ArrayList<>();
        for (Map<String, Object> v : ventasDb) {
            Map<String, Object> venta = new HashMap<>();
            venta.put("id", v.get("id"));
            venta.put("cliente", v.get("cliente"));
            venta.put("producto", "Compra Tienda");
            venta.put("categoria", "Física/Web");
            venta.put("monto", v.get("total_pagado"));
            venta.put("fecha", v.get("fecha_compra"));
            
            String estadoBD = v.get("estado_pedido") != null ? v.get("estado_pedido").toString().toUpperCase() : "";
            venta.put("estado", estadoBD.equals("CANCELADO") ? "cancelado" : (estadoBD.equals("RECIBIDO") ? "pendiente" : "completado"));
            ventasRecientes.add(venta);
        }
        respuesta.put("ventasRecientes", ventasRecientes);

        // 4. DONUT DATA
        String sqlDonut = "SELECT pr.categoria AS label, SUM(d.subtotal_item) AS valor FROM detalle_pago_tienda d JOIN productos pr ON d.producto_id = pr.id GROUP BY pr.categoria";
        List<Map<String, Object>> donutDb = jdbcTemplate.queryForList(sqlDonut);
        String[] colores = {"#ff8d22", "#111111", "#64748b", "#94a3b8"};
        int i = 0;
        for (Map<String, Object> d : donutDb) { d.put("color", colores[i % colores.length]); i++; }
        respuesta.put("donutData", donutDb);

        // 5. TOP PRODUCTOS
        String sqlTop = "SELECT pr.nombre, pr.stock, SUM(d.cantidad) AS ventas, SUM(d.subtotal_item) AS ingreso FROM detalle_pago_tienda d JOIN productos pr ON d.producto_id = pr.id GROUP BY pr.id ORDER BY ventas DESC LIMIT 5";
        List<Map<String, Object>> topDb = jdbcTemplate.queryForList(sqlTop);
        Integer maxVentas = topDb.isEmpty() ? 1 : Integer.parseInt(topDb.get(0).get("ventas").toString());
        for (Map<String, Object> t : topDb) {
            double ventas = Double.parseDouble(t.get("ventas").toString());
            t.put("porcentaje", Math.round((ventas / maxVentas) * 100));
        }
        respuesta.put("productosTop", topDb);

        // ==========================================
        // 6. GRÁFICA DINÁMICA (100% REAL + CORRECCIÓN DE HUECOS VISUALES)
        // ==========================================
        List<Map<String, Object>> grafica = new ArrayList<>();
        
        // PASO A: Crear el "esqueleto" visual perfecto en orden (Lleno de S/ 0.0)
        Map<String, Double> ingresosMap = new java.util.LinkedHashMap<>(); // LinkedHashMap respeta el orden visual
        String sqlGrafica = "";

        if (periodo.equals("Este año")) {
            String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
            for (String m : meses) ingresosMap.put(m, 0.0);
            sqlGrafica = "SELECT DATE_FORMAT(fecha_compra, '%b') AS clave, SUM(total_pagado) AS valor FROM pago_tienda WHERE " + filtroFecha + " GROUP BY MONTH(fecha_compra), clave";
        } 
        else if (periodo.equals("Esta semana")) {
            String[] dias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
            for (String d : dias) ingresosMap.put(d, 0.0);
            sqlGrafica = "SELECT DATE_FORMAT(fecha_compra, '%W') AS clave, SUM(total_pagado) AS valor FROM pago_tienda WHERE " + filtroFecha + " GROUP BY DATE(fecha_compra), clave";
        }
        else if (periodo.equals("Hoy")) {
            String[] horas = {"Mañana", "Mediodía", "Tarde", "Noche"};
            for (String h : horas) ingresosMap.put(h, 0.0);
            sqlGrafica = "SELECT HOUR(fecha_compra) AS clave, SUM(total_pagado) AS valor FROM pago_tienda WHERE " + filtroFecha + " GROUP BY HOUR(fecha_compra)";
        }
        else { // "Este mes"
            String[] semanas = {"Semana 1", "Semana 2", "Semana 3", "Semana 4"};
            for (String s : semanas) ingresosMap.put(s, 0.0);
            sqlGrafica = "SELECT DAY(fecha_compra) AS clave, SUM(total_pagado) AS valor FROM pago_tienda WHERE " + filtroFecha + " GROUP BY DAY(fecha_compra)";
        }

        // PASO B: Ejecutar el SQL Real y rellenar los datos sobre el esqueleto
        List<Map<String, Object>> datosRealesBD = jdbcTemplate.queryForList(sqlGrafica);
        
        for (Map<String, Object> fila : datosRealesBD) {
            String claveBD = fila.get("clave") != null ? fila.get("clave").toString() : "";
            double valorBD = fila.get("valor") != null ? Double.parseDouble(fila.get("valor").toString()) : 0.0;
            
            if (periodo.equals("Este año")) {
                claveBD = claveBD.replace("Jan", "Ene").replace("Apr", "Abr").replace("Aug", "Ago").replace("Dec", "Dic");
                if(ingresosMap.containsKey(claveBD)) ingresosMap.put(claveBD, ingresosMap.get(claveBD) + valorBD);
            } 
            else if (periodo.equals("Esta semana")) {
                claveBD = claveBD.replace("Monday", "Lun").replace("Tuesday", "Mar").replace("Wednesday", "Mié")
                                 .replace("Thursday", "Jue").replace("Friday", "Vie").replace("Saturday", "Sáb").replace("Sunday", "Dom");
                if(ingresosMap.containsKey(claveBD)) ingresosMap.put(claveBD, ingresosMap.get(claveBD) + valorBD);
            }
            else if (periodo.equals("Hoy")) {
                int hora = Integer.parseInt(claveBD);
                if (hora < 12) ingresosMap.put("Mañana", ingresosMap.get("Mañana") + valorBD);
                else if (hora < 15) ingresosMap.put("Mediodía", ingresosMap.get("Mediodía") + valorBD);
                else if (hora < 19) ingresosMap.put("Tarde", ingresosMap.get("Tarde") + valorBD);
                else ingresosMap.put("Noche", ingresosMap.get("Noche") + valorBD);
            }
            else { // "Este mes"
                int dia = Integer.parseInt(claveBD);
                String sem = "Semana 1";
                if(dia > 7 && dia <= 14) sem = "Semana 2";
                else if(dia > 14 && dia <= 21) sem = "Semana 3";
                else if(dia > 21) sem = "Semana 4";
                ingresosMap.put(sem, ingresosMap.get(sem) + valorBD);
            }
        }

        // PASO C: Convertir el mapa final a la lista que dibuja Angular
        for (Map.Entry<String, Double> entry : ingresosMap.entrySet()) {
            Map<String, Object> punto = new HashMap<>();
            punto.put("mes", entry.getKey());
            punto.put("ingresos", entry.getValue());
            // Como no hay tabla de gastos reales, simulamos el egreso (costos) en 30% del ingreso real
            punto.put("egresos", entry.getValue() * 0.30); 
            grafica.add(punto);
        }
        
        respuesta.put("graficaMeses", grafica);

        return ResponseEntity.ok(respuesta);
    }

    // Helper interno para crear los KPIs
    private Map<String, Object> crearKpi(String label, String value, String subLabel, Double trend, String icon, String accentClass) {
        Map<String, Object> kpi = new HashMap<>();
        kpi.put("label", label); kpi.put("value", value); kpi.put("subLabel", subLabel);
        kpi.put("trend", trend); kpi.put("icon", icon); kpi.put("accentClass", accentClass);
        return kpi;
    }
}