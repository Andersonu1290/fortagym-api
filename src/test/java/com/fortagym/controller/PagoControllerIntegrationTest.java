package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fortagym.config.TestBeansConfig;
import com.fortagym.config.WithMockCustomUser;
import com.fortagym.model.Membresia;
import com.fortagym.model.Pago;
import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.MembresiaRepository;
import com.fortagym.repository.PagoRepository;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
@Transactional
class PagoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @BeforeEach
void setupUsers() {

    if (usuarioRepository.findByEmail("pagador@test.com") == null) {
        Usuario u1 = new Usuario();
        u1.setNombre("Pagador");
        u1.setApellido("Test");
        u1.setEmail("pagador@test.com");
        u1.setPassword("123456"); // o hash, no importa en el test
        u1.setRol(Rol.USUARIO);
        usuarioRepository.save(u1);
    }

    if (usuarioRepository.findByEmail("pagador2@test.com") == null) {
        Usuario u2 = new Usuario();
        u2.setNombre("Pagador2");
        u2.setApellido("Test");
        u2.setEmail("pagador2@test.com");
        u2.setPassword("123456");
        u2.setRol(Rol.USUARIO);
        usuarioRepository.save(u2);
    }
}


@Test
@WithMockCustomUser(email = "pagador@test.com")
void confirmarPago_con_tarjeta_registra_pago_y_muestra_confirmacionTarjeta() throws Exception {

    Membresia m = new Membresia();
    m.setTipo("Gold");
    m.setDuracionMeses(1);
    m.setDescripcion("desc");
    m.setPrecio(100.0);
    membresiaRepository.save(m);

    mockMvc.perform(post("/pago/confirmar")
            .param("dni", "12345678")
            .param("metodoPago", "tarjeta")
            .param("membresiaId", m.getId().toString()))
        .andExpect(status().isOk());

    assertThat(pagoRepository.findAll()).hasSize(1);
    Pago pago = pagoRepository.findAll().get(0);
    assertThat(pago.getEstado().toLowerCase()).isEqualTo("verificado");
    assertThat(pago.getMonto()).isEqualTo(m.getPrecio());
}

@Test
@WithMockCustomUser(email = "pagador2@test.com")
void confirmarPago_presencial_registra_pago_estado_pendiente() throws Exception {

    Membresia m = new Membresia();
    m.setTipo("Silver");
    m.setDuracionMeses(1);
    m.setDescripcion("d");
    m.setPrecio(50.0);
    membresiaRepository.save(m);

    mockMvc.perform(post("/pago/confirmar")
            .param("dni", "87654321")
            .param("metodoPago", "presencial")
            .param("membresiaId", m.getId().toString()))
        .andExpect(status().isOk());

    assertThat(pagoRepository.findAll()).hasSize(1);
    assertThat(pagoRepository.findAll().get(0).getEstado().toLowerCase()).isEqualTo("pendiente");
}
}
