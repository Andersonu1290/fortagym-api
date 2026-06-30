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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fortagym.config.TestBeansConfig;
import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Desactiva los filtros de seguridad
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
        Usuario u1 = new Usuario("Alfredo", "Baldeon", "11111111", "a@test.com", "1234567", Rol.USUARIO, null, null, null);
        Usuario u2 = new Usuario("Ccarlos", "Desmond", "22222222", "c@test.com", "1234567", Rol.ADMIN, null, null, null);
        usuarioRepository.saveAll(List.of(u1, u2));

        // ⬅️ Actualizado al nuevo path REST
        mockMvc.perform(get("/api/admin/usuarios"))
            .andExpect(status().isOk());
    }

    @Test
    void cambiarRol_y_eliminar_usuario_flow() throws Exception {
        Usuario u = new Usuario("Cambe", "User", "33333333", "cambio@test.com", "1234567", Rol.USUARIO, null, null, null);
        usuarioRepository.save(u);

        // ⬅️ Usamos PUT en lugar de POST para la actualización
        mockMvc.perform(put("/api/admin/cambiar-rol/{id}", u.getId())
                .param("rol", "ADMIN"))
            .andExpect(status().isOk()); // ⬅️ Espera 200 OK

        Usuario saved = usuarioRepository.findById(u.getId()).orElseThrow();
        assertThat(saved.getRol()).isEqualTo(Rol.ADMIN);

        // ⬅️ Usamos DELETE en lugar de POST para eliminar
        mockMvc.perform(delete("/api/admin/eliminar/{id}", u.getId()))
            .andExpect(status().isOk()); // ⬅️ Espera 200 OK

        assertThat(usuarioRepository.existsById(u.getId())).isFalse();
    }
}