package com.fortagym.service;

import com.fortagym.model.Membresia;
import com.fortagym.model.Pago;
import com.fortagym.repository.MembresiaRepository;
import com.fortagym.repository.PagoRepository;
import com.fortagym.service.PagoService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MembresiaRepository membresiaRepository;

    @InjectMocks
    private PagoService pagoService;

    @Test
    void registrarPago_deberiaGuardarCorrectamente() {
        Pago pago = new Pago();
        pago.setMonto(50.0);

        Pago saved = new Pago();
        saved.setId(1L);
        saved.setMonto(50.0);
        saved.setFechaPago(LocalDateTime.now());

        when(pagoRepository.save(pago)).thenReturn(saved);

        Pago guardado = pagoService.registrarPago(pago);

        assertNotNull(guardado.getId());
        assertNotNull(guardado.getFechaPago());
    }

    @Test
    void obtenerMembresiaPorId_deberiaRetornarMembresia() {
        Membresia m = new Membresia();
        m.setId(1L);
        m.setTipo("Mensual");
        m.setPrecio(100.0);

        when(membresiaRepository.findById(1L))
                .thenReturn(Optional.of(m));

        Membresia encontrada = pagoService.obtenerMembresiaPorId(1L);

        assertNotNull(encontrada);
        assertEquals("Mensual", encontrada.getTipo());
    }
}
