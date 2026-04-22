package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fortagym.config.TestBeansConfig;
import com.fortagym.model.Nutricion;
import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.NutricionRepository;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
 // ❗ Desactiva los filtros de seguridad
@ActiveProfiles("test")
@Import(TestBeansConfig.class)

@Transactional
@WithMockUser(username = "testuser", roles = {"USER"})
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
    void nuevaNutricion_guardar_y_ver() throws Exception {
        Usuario u = new Usuario("Number1","Ultimo","nutri@test.com","123456", Rol.USUARIO, null);
        usuarioRepository.save(u);

        mockMvc.perform(get("/nutricion/nuevo/{idUsuario}", u.getId()))
            .andExpect(status().isOk());

        mockMvc.perform(post("/nutricion/guardar")
        .with(csrf())
        .contentType("application/x-www-form-urlencoded")
        .param("usuario.id", u.getId().toString())
        .param("analisisCorporal", "masa")
        .param("observaciones", "sin observaciones"))
    .andExpect(status().is3xxRedirection());

        Optional<Nutricion> opt = nutricionRepository.findByUsuarioId(u.getId());
        assertThat(opt).isPresent();
        assertThat(opt.get().getAnalisisCorporal()).isEqualTo("masa");
    }
}
