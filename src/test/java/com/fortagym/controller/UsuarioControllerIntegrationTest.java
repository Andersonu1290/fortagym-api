package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanup() {
        usuarioRepository.deleteAll();
    }

    @Test
    void registro_post_crea_usuario_y_redirige() throws Exception {
        mockMvc.perform(post("/registro")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nombre", "Galo")
                .param("apellido", "Perez")
                .param("email", "nuevo@test.com")
                .param("password", "123456"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        Usuario u = usuarioRepository.findByEmail("nuevo@test.com");
        assertThat(u).isNotNull();
        assertThat(passwordEncoder.matches("123456", u.getPassword())).isTrue();
    }

    @Test
    void subirFotoPerfil_y_mostrarFoto_flow() throws Exception {
        // Crear usuario
        Usuario u = new Usuario("A","B","foto@test.com", passwordEncoder.encode("pwd"), Rol.USUARIO, null);
        usuarioRepository.save(u);

        MockMultipartFile img = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1,2,3,4});

        // subir foto (simulamos principal con el email)
        Principal principal = () -> "foto@test.com";

        mockMvc.perform(multipart("/usuario/foto")
                    .file(img)
                    .principal(principal))
            .andExpect(status().is3xxRedirection());

        Usuario saved = usuarioRepository.findByEmail("foto@test.com");
        assertThat(saved.getFotoPerfil()).isNotNull();

        // obtener foto
        mockMvc.perform(get("/usuario/foto/{id}", saved.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/jpeg"))
            .andExpect(content().bytes(saved.getFotoPerfil()));
    }

    @Test
    void actualizarUsuario_post_actualiza_nombres_y_password() throws Exception {
        Usuario u = new Usuario("X","Y","change@test.com", passwordEncoder.encode("old"), Rol.USUARIO, null);
        usuarioRepository.save(u);

        Principal principal = () -> "change@test.com";

        mockMvc.perform(post("/usuario/cambio")
                    .principal(principal)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("nombre", "Nuevo")
                    .param("apellido", "Apellido")
                    .param("password", "newpass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/usuario"));

        Usuario saved = usuarioRepository.findByEmail("change@test.com");
        assertThat(saved.getNombre()).isEqualTo("Nuevo");
        assertThat(passwordEncoder.matches("newpass", saved.getPassword())).isTrue();
    }
}
