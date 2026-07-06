package com.fortagym.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.model.Nutricion;
import com.fortagym.model.Usuario;

@DataJpaTest
@ActiveProfiles("test")
class NutricionRepositoryTest {

    @Autowired
    private NutricionRepository nutricionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void save_findAndDeleteDirectly_work() {
        Usuario u = new Usuario("N","U","99999999","nutrirepo@test.com","passwd123", com.fortagym.model.Rol.USUARIO, null, null, null);
        usuarioRepository.save(u);

        Nutricion n = new Nutricion(u, "masa", "obs");
        nutricionRepository.save(n);

        Optional<Nutricion> opt = nutricionRepository.findByUsuarioId(u.getId());
        assertThat(opt).isPresent();
        assertThat(nutricionRepository.existsByUsuarioId(u.getId())).isTrue();

        // ejecutar query modificante
        nutricionRepository.eliminarDirectamentePorUsuarioId(u.getId());
        assertThat(nutricionRepository.findByUsuarioId(u.getId())).isEmpty();
    }
}
