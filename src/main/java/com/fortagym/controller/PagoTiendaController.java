package com.fortagym.controller;

import com.fortagym.dto.CheckoutRequestDTO;
import com.fortagym.model.PagoTienda;
import com.fortagym.service.PagoTiendaService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tienda")

public class PagoTiendaController {

    @Autowired
    private PagoTiendaService pagoTiendaService;

    @PostMapping("/checkout")
    public ResponseEntity<?> procesarCompra(@Valid @RequestBody CheckoutRequestDTO request) {
        try {
            PagoTienda orden = pagoTiendaService.procesarCheckout(request);
            return ResponseEntity.ok(orden);
        } catch (Exception e) {
            // Si falta stock o el producto no existe, devolvemos error 400
            return ResponseEntity.badRequest().body(new MensajeResponse(e.getMessage()));
        }
    }
}