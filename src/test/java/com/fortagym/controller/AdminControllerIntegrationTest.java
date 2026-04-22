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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fortagym.config.TestBeansConfig;
import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // ❗ Desactiva los filtros de seguridad
@ActiveProfiles("test")
@Import(TestBeansConfig.class)


@Transactional
@WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void cleanup() {
        usuarioRepository.deleteAll();
    }

    @Test
    void verUsuarios_get_muestra_lista() throws Exception {
        Usuario u1 = new Usuario("Alfredo","Baldeon","a@test.com","1234567", Rol.USUARIO, null);
        Usuario u2 = new Usuario("Ccarlos","Desmond","c@test.com","1234567", Rol.ADMIN, null);
        usuarioRepository.saveAll(List.of(u1,u2));

        mockMvc.perform(get("/admin"))
            .andExpect(status().isOk());
    }

    @Test
    void cambiarRol_y_eliminar_usuario_flow() throws Exception {
        Usuario u = new Usuario("Cambe","User","cambio@test.com","1234567", Rol.USUARIO, null);
        usuarioRepository.save(u);

        mockMvc.perform(post("/admin/cambiar-rol/{id}", u.getId())
                .param("rol", "ADMIN"))
            .andExpect(status().is3xxRedirection());

        Usuario saved = usuarioRepository.findById(u.getId()).orElseThrow();
        assertThat(saved.getRol()).isEqualTo(Rol.ADMIN);

        // eliminar
        mockMvc.perform(post("/admin/eliminar/{id}", u.getId()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));

        assertThat(usuarioRepository.existsById(u.getId())).isFalse();
    }
}
