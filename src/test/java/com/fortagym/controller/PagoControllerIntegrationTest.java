package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortagym.config.TestBeansConfig;
import com.fortagym.model.Membresia;
import com.fortagym.model.Pago;
import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.MembresiaRepository;
import com.fortagym.repository.PagoRepository;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc // Elimina 'addFilters = false' si quieres probar tu seguridad real
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
@Transactional
@WithMockUser(username = "pagador@test.com", roles = {"USUARIO"})
class PagoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MembresiaRepository membresiaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setupUsers() {
        pagoRepository.deleteAll();
        membresiaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario u1 = new Usuario("Pagador", "Test", "12345678", "pagador@test.com", "123456", Rol.USUARIO, null, null, null);
        usuarioRepository.save(u1);
    }

    @Test
    void confirmarPago_conTarjeta_registraPagoVerificado() throws Exception {
        Membresia membresia = new Membresia();
        membresia.setTipo("Gold");
        membresia.setDuracionMeses(1);
        membresia.setDescripcion("desc");
        membresia.setPrecio(100.0);
        membresiaRepository.save(membresia);

        // Crear el objeto Request directamente para que sea consistente con tu API
        String jsonRequest = """
                {
                  "numeroOperacion": "OP-123456",
                  "metodoPago": "tarjeta",
                  "membresiaId": %d
                }
                """.formatted(membresia.getId());

        mockMvc.perform(post("/api/pagos/confirmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pago procesado correctamente"))
                .andExpect(jsonPath("$.estado").value("verificado"))
                .andExpect(jsonPath("$.metodo").value("tarjeta"));

        List<Pago> pagos = pagoRepository.findAll();
        assertThat(pagos).hasSize(1);
        assertThat(pagos.get(0).getEstado()).isEqualToIgnoringCase("verificado");
    }

    @Test
    void confirmarPago_presencial_registraPagoPendiente() throws Exception {
        Membresia membresia = new Membresia();
        membresia.setTipo("Silver");
        membresia.setDuracionMeses(1);
        membresia.setDescripcion("desc");
        membresia.setPrecio(50.0);
        membresiaRepository.save(membresia);

        String jsonRequest = """
                {
                  "numeroOperacion": "OP-987654",
                  "metodoPago": "presencial",
                  "membresiaId": %d
                }
                """.formatted(membresia.getId());

        mockMvc.perform(post("/api/pagos/confirmar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pago procesado correctamente"))
                .andExpect(jsonPath("$.estado").value("pendiente"))
                .andExpect(jsonPath("$.metodo").value("presencial"));

        List<Pago> pagos = pagoRepository.findAll();
        assertThat(pagos).hasSize(1);
        assertThat(pagos.get(0).getEstado()).isEqualToIgnoringCase("pendiente");
    }
}