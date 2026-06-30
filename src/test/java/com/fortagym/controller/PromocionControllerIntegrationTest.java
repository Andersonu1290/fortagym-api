package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.web.servlet.MockMvc;

import com.fortagym.config.TestBeansConfig;
import com.fortagym.repository.PromocionRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
@Transactional
class PromocionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromocionRepository promocionRepository;

    @BeforeEach
    void cleanup() throws Exception {

        promocionRepository.deleteAll();

        Path uploads = Path.of(System.getProperty("user.dir"), "uploads");

        if (Files.exists(uploads)) {
            Files.walk(uploads)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }

    @Test
    void subirPromocion_y_eliminar_flow() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagen.jpg",
                "image/jpeg",
                new byte[] {1, 2, 3}
        );

        mockMvc.perform(
                multipart("/api/admin/promociones/subir")
                        .file(file)
                        .param("nombre", "Promo Test")
                        .param("titulo", "Título de prueba")
                        .param("descripcion", "Descripción de prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        assertThat(promocionRepository.findAll()).hasSize(1);

        Long id = promocionRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/admin/promociones/eliminar/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Eliminado correctamente"));

        assertThat(promocionRepository.findAll()).isEmpty();
    }

    @Test
    void listarPromociones_get_ok() throws Exception {

        mockMvc.perform(get("/api/admin/promociones"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void obtenerUltimaPromocion_sinDatos_devuelve404() throws Exception {

        mockMvc.perform(get("/api/admin/promociones/ultima"))
                .andExpect(status().isNotFound());
    }
}