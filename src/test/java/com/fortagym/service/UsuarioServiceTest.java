package com.fortagym.service;

import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import com.fortagym.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrar_deberiaRegistrarUsuarioCodificandoPassword() {
        Usuario u = new Usuario();
        u.setPassword("1234");

        when(encoder.encode("1234")).thenReturn("CODIFICADA");
        when(usuarioRepository.save(any())).thenAnswer(inv -> {
            Usuario usr = inv.getArgument(0);
            usr.setId(1L);
            return usr;
        });

        Usuario guardado = usuarioService.registrar(u);

        assertNotNull(guardado.getId());
        assertEquals("CODIFICADA", guardado.getPassword());
        verify(encoder).encode("1234");
    }

    @Test
    void validarLogin_deberiaRetornarTrue() {
        Usuario u = new Usuario();
        u.setEmail("maria@test.com");
        u.setPassword("ENCODED");

        when(usuarioRepository.findByEmail("maria@test.com")).thenReturn((u));
        when(encoder.matches("abcd", "ENCODED")).thenReturn(true);

        assertTrue(usuarioService.validarLogin("maria@test.com", "abcd"));
    }

    @Test
    void buscarPorEmail_deberiaRetornarUsuario() {
        Usuario u = new Usuario();
        u.setNombre("Leo");

        when(usuarioRepository.findByEmail("leo@test.com")).thenReturn((u));

        Usuario encontrado = usuarioService.buscarPorEmail("leo@test.com");

        assertNotNull(encontrado);
        assertEquals("Leo", encontrado.getNombre());
    }

    @Test
    void findById_deberiaRetornarUsuario() {
        Usuario u = new Usuario();
        u.setNombre("Pablo");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        Usuario encontrado = usuarioService.findById(1L);

        assertNotNull(encontrado);
        assertEquals("Pablo", encontrado.getNombre());
    }

    @Test
    void actualizarPassword_deberiaActualizarCorrectamente() {
        Usuario u = new Usuario();
        u.setPassword("OLD");

        when(encoder.encode("nueva123")).thenReturn("ENCODED");

        usuarioService.actualizarPassword(u, "nueva123");

        assertEquals("ENCODED", u.getPassword());
        verify(encoder).encode("nueva123");
    }

    @Test
    void obtenerSoloUsuarios_deberiaRetornarSoloUsuariosRolUsuario() {
    Usuario admin = new Usuario();
    admin.setRol(Rol.ADMIN);

    Usuario user = new Usuario();
    user.setRol(Rol.USUARIO);
    user.setNombre("Daniel");

    when(usuarioRepository.findByRol(Rol.USUARIO))
            .thenReturn(List.of(user));

    List<Usuario> usuarios = usuarioService.obtenerSoloUsuarios();

    assertEquals(1, usuarios.size());
    assertEquals("Daniel", usuarios.get(0).getNombre());
    }

}
