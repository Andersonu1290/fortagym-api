package com.fortagym.repository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.model.Promocion;

@DataJpaTest
@ActiveProfiles("test")
class PromocionRepositoryTest {

    @Autowired
    private PromocionRepository promocionRepository;

    @Test
    void findTopByOrderByFechaSubidaDesc_returns_latest() {
        Promocion p1 = new Promocion("old","/img/old.jpg");
        p1.setFechaSubida(LocalDateTime.now().minusDays(2));
        Promocion p2 = new Promocion("new","/img/new.jpg");
        p2.setFechaSubida(LocalDateTime.now());
        promocionRepository.save(p1);
        promocionRepository.save(p2);

        Promocion latest = promocionRepository.findTopByOrderByFechaSubidaDesc();
        assertThat(latest).isNotNull();
        assertThat(latest.getNombre()).isEqualTo("new");
    }
}
