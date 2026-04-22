package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fortagym.config.TestBeansConfig;
import com.fortagym.model.DetalleRutina;
import com.fortagym.model.Rol;
import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;
import com.fortagym.repository.DetalleRutinaRepository;
import com.fortagym.repository.RutinaRepository;
import com.fortagym.repository.UsuarioRepository;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // ❗ Desactiva los filtros de seguridad
@ActiveProfiles("test")
@Import(TestBeansConfig.class)


@Transactional
class RutinaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private DetalleRutinaRepository detalleRutinaRepository;

    @BeforeEach
    void setup() {
        detalleRutinaRepository.deleteAll();
        rutinaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void guardarRutina_crea_rutina_y_detalles() throws Exception {
        Usuario u = new Usuario("Rut","Ulisa","rut@test.com","123456", Rol.USUARIO, null);
        usuarioRepository.save(u);

        mockMvc.perform(post("/rutina/guardar")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("usuarioId", u.getId().toString())
                .param("observaciones", "obs")
                .param("nombreEntrenador", "Trainer")
                .param("ejercicio", "Sentadillas")
                .param("seriesReps", "3x12")
                .param("descanso", "60s")
                .param("dias", "Lunes"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/rutina/usuarios"));

        List<Rutina> rutinas = rutinaRepository.findAll();
        assertThat(rutinas).hasSize(1);
        Rutina r = rutinas.get(0);
        assertThat(r.getNombreEntrenador()).isEqualTo("Trainer");
        List<DetalleRutina> detalles = detalleRutinaRepository.findByRutina(r);
        assertThat(detalles).isNotEmpty();
        assertThat(detalles.get(0).getEjercicio()).isEqualTo("Sentadillas");
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = {"USER"})
    void editarRutina_get_devuelve_rutina_existente() throws Exception {
        Usuario u = new Usuario("Esteban","Dario","edit@test.com","123456", Rol.USUARIO, null);
        usuarioRepository.save(u);

        Rutina r = new Rutina("obs","T", u);
        DetalleRutina d = new DetalleRutina();
        d.setEjercicio("Press");
        d.setSeriesReps("4x10");
        d.setDescanso("60s");
        d.setDias("Miercoles");
        d.setRutina(r);
        r.getDetalles().add(d);
        rutinaRepository.save(r);

        mockMvc.perform(get("/rutina/editar/{usuarioId}", u.getId()))
            .andExpect(status().isOk());
    }
}
