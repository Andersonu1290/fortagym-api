package com.fortagym.service;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import com.fortagym.service.CustomUserDetailsService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_Found() {
        Usuario u = new Usuario();
        u.setEmail("x@y.com");
        u.setPassword("p");
        when(usuarioRepository.findByEmail("x@y.com")).thenReturn(u);

        UserDetails ud = service.loadUserByUsername("x@y.com");
        assertNotNull(ud);
        assertEquals("x@y.com", ud.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound_Throws() {
        when(usuarioRepository.findByEmail("nope")).thenReturn(null);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nope"));
    }
}
