package com.fortagym.service;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fortagym.model.Membresia;
import com.fortagym.model.Pago;
import com.fortagym.repository.MembresiaRepository;
import com.fortagym.repository.PagoRepository;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    public Membresia obtenerMembresiaPorId(Long id) {
    return membresiaRepository.findById(id).orElse(null);
    }

    public Pago registrarPago(Pago pago) {
        pago.setFechaPago(LocalDateTime.now());
        return pagoRepository.save(pago);
    }

}
