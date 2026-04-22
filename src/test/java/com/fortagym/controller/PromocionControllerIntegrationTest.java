package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fortagym.config.TestBeansConfig;
import com.fortagym.repository.PromocionRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // ❗ Desactiva los filtros de seguridad
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
        // limpiar carpeta uploads si existe
        Path uploads = Path.of(System.getProperty("user.dir"), "uploads");
        if (Files.exists(uploads)) {
            Files.walk(uploads).sorted((a,b) -> b.compareTo(a)).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception e) {}
            });
        }
    }

    @Test
    void subirPromocion_y_eliminar_flow() throws Exception {
        MockMultipartFile file = new MockMultipartFile("imagen", "img.jpg", "image/jpeg", new byte[]{1,2,3});
        mockMvc.perform(multipart("/admin/promociones/subir")
                .file(file)
                .param("nombre", "PromoTest"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/promociones"));

        assertThat(promocionRepository.findAll()).hasSize(1);

        Long id = promocionRepository.findAll().get(0).getId();

        mockMvc.perform(post("/admin/promociones/eliminar/{id}", id))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/promociones"));

        assertThat(promocionRepository.findAll()).isEmpty();
    }

    @Test
    void mostrarPromociones_get_ok() throws Exception {
        mockMvc.perform(get("/admin/promociones"))
            .andExpect(status().isOk());
    }
}
