package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

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
import com.fortagym.model.Nutricion;
import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.NutricionRepository;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 🔥 FIX 403 / 401
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
@Transactional
class NutricionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NutricionRepository nutricionRepository;

    @BeforeEach
    void cleanup() {
        nutricionRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void guardarYObtenerNutricion() throws Exception {

        Usuario usuario = new Usuario(
                "Number1",
                "Ultimo",
                "55555555",
                "nutri@test.com",
                "123456",
                Rol.USUARIO,
                null,
                null,
                null);

        usuarioRepository.save(usuario);

        String json = """
        {
          "usuario": {
            "id": %d
          },
          "analisisCorporal": "masa",
          "observaciones": "sin observaciones"
        }
        """.formatted(usuario.getId());

        mockMvc.perform(post("/api/nutricion/guardar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        Optional<Nutricion> nutricion = nutricionRepository.findByUsuarioId(usuario.getId());

        assertThat(nutricion).isPresent();
        assertThat(nutricion.get().getAnalisisCorporal()).isEqualTo("masa");

        mockMvc.perform(get("/api/nutricion/usuario/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisCorporal").value("masa"))
                .andExpect(jsonPath("$.observaciones").value("sin observaciones"));
    }

    @Test
    void eliminarNutricion() throws Exception {

        Usuario usuario = new Usuario(
                "Juan",
                "Perez",
                "11111111",
                "juan@test.com",
                "123456",
                Rol.USUARIO,
                null,
                null,
                null);

        usuarioRepository.save(usuario);

        Nutricion nutricion = new Nutricion(usuario, "masa", "obs");
        nutricionRepository.save(nutricion);

        mockMvc.perform(delete("/api/nutricion/eliminar/usuario/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        assertThat(nutricionRepository.findByUsuarioId(usuario.getId())).isEmpty();
    }

    @Test
    void obtenerNutricionInexistente() throws Exception {

        mockMvc.perform(get("/api/nutricion/usuario/99999"))
                .andExpect(status().isNotFound());
    }
}