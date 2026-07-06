package com.fortagym.repository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void derivedAndCustomQueries_work() {
        Usuario a = new Usuario("Ana","Perez","11111111","ana@test.com","passwd123", Rol.USUARIO, null, null, null);
        Usuario b = new Usuario("Bruno","Gomez","22222222","bruno@test.com","passwd123", Rol.ADMIN, null, null, null);
        usuarioRepository.saveAll(List.of(a,b));

        assertThat(usuarioRepository.findByEmail("ana@test.com")).isPresent();
        assertThat(usuarioRepository.findByDni("22222222")).isPresent();
        assertThat(usuarioRepository.existsByEmail("bruno@test.com")).isTrue();

        List<Usuario> admins = usuarioRepository.findByRol(Rol.ADMIN);
        assertThat(admins).hasSize(1).allMatch(u -> u.getRol()==Rol.ADMIN);

        List<Usuario> jpql = usuarioRepository.buscarUsuariosPorRolJPQL(Rol.USUARIO);
        assertThat(jpql).hasSize(1).extracting(Usuario::getEmail).contains("ana@test.com");

        List<Usuario> filtro = usuarioRepository.buscarPorNombreOApellidoJPQL("brun");
        assertThat(filtro).hasSize(1).extracting(Usuario::getEmail).contains("bruno@test.com");
    }
}
