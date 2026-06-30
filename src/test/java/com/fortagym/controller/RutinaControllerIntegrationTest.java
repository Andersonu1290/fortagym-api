package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.test.context.ActiveProfiles;
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
@AutoConfigureMockMvc(addFilters = false)
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

        Usuario usuario = new Usuario(
                "Rut",
                "Ulisa",
                "66666666",
                "rut@test.com",
                "123456",
                Rol.USUARIO,
                null,
                null,
                null);

        usuarioRepository.save(usuario);

        String json = """
        {
          "usuarioId": %d,
          "nombreEntrenador": "Trainer",
          "detalles": [
            {
              "ejercicio":"Sentadillas",
              "seriesReps":"3x12",
              "descanso":"60s",
              "dias":"Lunes"
            }
          ]
        }
        """.formatted(usuario.getId());

        mockMvc.perform(post("/api/rutinas/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        List<Rutina> rutinas = rutinaRepository.findAll();

        assertThat(rutinas).hasSize(1);

        Rutina rutina = rutinas.get(0);

        assertThat(rutina.getNombreEntrenador()).isEqualTo("Trainer");

        List<DetalleRutina> detalles = detalleRutinaRepository.findByRutina(rutina);

        assertThat(detalles).hasSize(1);
        assertThat(detalles.get(0).getEjercicio()).isEqualTo("Sentadillas");
        assertThat(detalles.get(0).getSeriesReps()).isEqualTo("3x12");
        assertThat(detalles.get(0).getDescanso()).isEqualTo("60s");
        assertThat(detalles.get(0).getDias()).isEqualTo("Lunes");
    }

    @Test
    void obtenerRutina_usuarioConRutina_devuelveDatos() throws Exception {

        Usuario usuario = new Usuario(
                "Esteban",
                "Dario",
                "77777777",
                "edit@test.com",
                "123456",
                Rol.USUARIO,
                null,
                null,
                null);

        usuarioRepository.save(usuario);

        Rutina rutina = new Rutina();
        rutina.setUsuario(usuario);
        rutina.setNombreEntrenador("Trainer");
        rutina.setObservaciones("Observaciones");

        rutinaRepository.save(rutina);

        DetalleRutina detalle = new DetalleRutina();
        detalle.setRutina(rutina);
        detalle.setEjercicio("Press");
        detalle.setSeriesReps("4x10");
        detalle.setDescanso("60s");
        detalle.setDias("Miércoles");

        detalleRutinaRepository.save(detalle);

        mockMvc.perform(get("/api/rutinas/usuario/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nombreEntrenador").value("Trainer"))
                .andExpect(jsonPath("$.detalles[0].ejercicio").value("Press"))
                .andExpect(jsonPath("$.detalles[0].seriesReps").value("4x10"))
                .andExpect(jsonPath("$.detalles[0].descanso").value("60s"))
                .andExpect(jsonPath("$.detalles[0].dias").value("Miércoles"));
    }

    @Test
    void obtenerRutina_usuarioSinRutina_devuelveMensaje() throws Exception {

        Usuario usuario = new Usuario(
                "Juan",
                "Perez",
                "88888888",
                "juan@test.com",
                "123456",
                Rol.USUARIO,
                null,
                null,
                null);

        usuarioRepository.save(usuario);

        mockMvc.perform(get("/api/rutinas/usuario/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sin rutina"));
    }

    @Test
    void obtenerRutina_usuarioInexistente_devuelve404() throws Exception {

        mockMvc.perform(get("/api/rutinas/usuario/999999"))
                .andExpect(status().isNotFound());
    }
}