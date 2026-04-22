package com.fortagym.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fortagym.model.Membresia;
import com.fortagym.model.Pago;
import com.fortagym.model.Usuario;
import com.fortagym.service.PagoService;
import com.fortagym.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController // ⬅️ Convertido a API REST
@RequestMapping("/api/pagos") // ⬅️ Nueva ruta base
public class PagoController {

    private static final Logger logger = LoggerFactory.getLogger(PagoController.class);

    @Autowired
    private PagoService pagoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarPago(@RequestBody PagoRequest request, Principal principal) {
        
        logger.info("💳 Iniciando proceso de pago. Método: {}, Membresía ID: {}, DNI: {}",
                request.getMetodoPago(), request.getMembresiaId(), request.getDni());

        // 1. Validar que haya sesión JWT
        if (principal == null) {
            return ResponseEntity.status(401).body(new MensajeResponse("Debes iniciar sesión antes de pagar."));
        }

        Usuario usuario = usuarioRepository.findByEmail(principal.getName());

        // 2. Validar DNI
        if (request.getDni() == null || !request.getDni().matches("\\d{8}")) {
            return ResponseEntity.badRequest().body(new MensajeResponse("El DNI debe tener exactamente 8 dígitos numéricos."));
        }

        // 3. Validar método de pago
        if (request.getMetodoPago() == null || request.getMetodoPago().isBlank()) {
            return ResponseEntity.badRequest().body(new MensajeResponse("Debe seleccionar un método de pago."));
        }

        // 4. Obtener membresía
        Membresia membresia = pagoService.obtenerMembresiaPorId(request.getMembresiaId());
        if (membresia == null) {
            return ResponseEntity.status(404).body(new MensajeResponse("Membresía no encontrada."));
        }

        String metodoNormalizado = request.getMetodoPago().trim().toLowerCase();

        // 5. Registrar el pago
        Pago pago = new Pago();
        pago.setDni(request.getDni());
        pago.setMetodoPago(metodoNormalizado);
        pago.setMembresia(membresia);
        pago.setUsuario(usuario);
        pago.setMonto(membresia.getPrecio());

        if (metodoNormalizado.equals("tarjeta")) {
            pago.setEstado("verificado");
        } else {
            pago.setEstado("pendiente");
        }

        pagoService.registrarPago(pago);

        // 6. Enviar respuesta a Angular
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Pago procesado correctamente");
        response.put("estado", pago.getEstado());
        response.put("metodo", metodoNormalizado);

        return ResponseEntity.ok(response);
    }
}

// DTO para recibir el JSON desde Angular
class PagoRequest {
    private String dni;
    private String metodoPago;
    private Long membresiaId;

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public Long getMembresiaId() { return membresiaId; }
    public void setMembresiaId(Long membresiaId) { this.membresiaId = membresiaId; }
}