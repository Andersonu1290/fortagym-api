package com.fortagym.service;

import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;
import com.fortagym.repository.RutinaRepository;
import com.fortagym.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutinaServiceTest {

    @Mock
    private RutinaRepository rutinaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RutinaService rutinaService;

    @Test
    void guardar_deberiaCrearRutina() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Rutina r = new Rutina();
        r.setUsuario(usuario);

        Rutina saved = new Rutina();
        saved.setId(100L);
        saved.setUsuario(usuario);

        when(rutinaRepository.save(r)).thenReturn(saved);

        Rutina guardada = rutinaService.guardar(r);

        assertNotNull(guardada.getId());
        assertEquals(1L, guardada.getUsuario().getId());
    }

    @Test
    void buscarPorUsuario_deberiaRetornarRutina() {
        Usuario u = new Usuario();
        u.setId(1L);

        Rutina r = new Rutina();
        r.setUsuario(u);

        when(rutinaRepository.findByUsuario(u))
                .thenReturn(Optional.of(r));

        Optional<Rutina> encontrada = rutinaService.buscarPorUsuario(u);

        assertTrue(encontrada.isPresent());
        assertEquals(1L, encontrada.get().getUsuario().getId());
    }

    @Test
    void eliminar_deberiaBorrarRutina() {
        Long id = 10L;

        doNothing().when(rutinaRepository).deleteById(id);

        rutinaService.eliminar(id);

        verify(rutinaRepository, times(1)).deleteById(id);
    }
}
