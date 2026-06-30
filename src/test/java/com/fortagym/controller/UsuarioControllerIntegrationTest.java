package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortagym.config.TestBeansConfig;
import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Desactiva los filtros de seguridad
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

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanup() {
        usuarioRepository.deleteAll();
    }

    @Test
    void registro_post_crea_usuario_y_retorna_201() throws Exception {
        // En lugar de usar ObjectMapper sobre el objeto (que lo ignora), usamos un JSON plano
        String json = """
            {
                "nombre": "Galo",
                "apellido": "Perez",
                "email": "nuevo@test.com",
                "password": "123456",
                "rol": "USUARIO"
            }
            """;

        mockMvc.perform(post("/api/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)) // Pasamos el JSON directo
            .andExpect(status().isCreated());

        Usuario u = usuarioRepository.findByEmail("nuevo@test.com").orElse(null);
        assertThat(u).isNotNull();
        // Verificamos que el password se guardó encriptado
        assertThat(passwordEncoder.matches("123456", u.getPassword())).isTrue();
    }

    @Test
    void subirFotoPerfil_y_mostrarFoto_flow() throws Exception {
        Usuario u = new Usuario("A", "B", "88888888", "foto@test.com", passwordEncoder.encode("pwd"), Rol.USUARIO, null, null, null);
        usuarioRepository.save(u);

        MockMultipartFile img = new MockMultipartFile("foto", "foto.jpg", "image/jpeg", new byte[]{1,2,3,4});
        Principal principal = () -> "foto@test.com";

        // Subir foto al nuevo endpoint REST
        mockMvc.perform(multipart("/api/usuarios/perfil/foto")
                    .file(img)
                    .principal(principal))
            .andExpect(status().isOk()); // ⬅️ Esperamos 200 OK

        Usuario saved = usuarioRepository.findByEmail("foto@test.com").orElse(null);
        assertThat(saved.getFotoPerfil()).isNotNull();

        // Obtener foto desde el nuevo endpoint
        mockMvc.perform(get("/api/usuarios/foto/{id}", saved.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(saved.getFotoPerfil()));
    }

    @Test
    void actualizarUsuario_put_actualiza_nombres_y_password() throws Exception {
        Usuario u = new Usuario("X", "Y", "99999999", "change@test.com", passwordEncoder.encode("old"), Rol.USUARIO, null, null, null);
        usuarioRepository.save(u);

        Principal principal = () -> "change@test.com";

        Map<String, String> payload = new HashMap<>();
        payload.put("nombre", "Nuevo");
        payload.put("apellido", "Apellido");
        payload.put("password", "newpass");

        // Usamos PUT al nuevo endpoint enviando JSON
        mockMvc.perform(put("/api/usuarios/perfil")
                    .principal(principal)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk()); // ⬅️ Esperamos 200 OK

        Usuario saved = usuarioRepository.findByEmail("change@test.com").orElse(null);
        assertThat(saved.getNombre()).isEqualTo("Nuevo");
        assertThat(passwordEncoder.matches("newpass", saved.getPassword())).isTrue();
    }
}