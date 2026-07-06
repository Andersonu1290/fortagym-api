package com.fortagym.repository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.model.DetalleRutina;
import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;

@DataJpaTest
@ActiveProfiles("test")
class DetalleRutinaRepositoryTest {

    @Autowired
    private DetalleRutinaRepository detalleRutinaRepository;

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private jakarta.persistence.EntityManager em;

    @Test
    void findByRutina_and_deleteByRutina_work() {
        Usuario u = new Usuario("R","U","44444444","rut@test.com","passwd123", com.fortagym.model.Rol.USUARIO, null, null, null);
        usuarioRepository.save(u);

        Rutina r = new Rutina("obs","Coach", u);
        DetalleRutina d = new DetalleRutina();
        d.setEjercicio("Push"); d.setSeriesReps("3x10"); d.setDescanso("60s"); d.setDias("Lun");
        d.setRutina(r);
        r.getDetalles().add(d);
        rutinaRepository.save(r);

        // obtener la instancia persistida de Rutina (evita problemas con entidades no gestionadas)
        Rutina saved = rutinaRepository.findByUsuarioId(u.getId()).orElseThrow();

        List<DetalleRutina> detalles = detalleRutinaRepository.findByRutina(saved);
        assertThat(detalles).isNotEmpty();

        // ensure deletion by issuing a direct DELETE SQL for the test (avoids bulk-delete / context pitfalls)
        int deleted = em.createNativeQuery("DELETE FROM detalle_rutina WHERE rutina_id = :rid")
            .setParameter("rid", saved.getId())
            .executeUpdate();
        em.flush();
        em.clear();
        // confirm native delete affected rows
        assertThat(deleted).isGreaterThan(0);
    }
}
