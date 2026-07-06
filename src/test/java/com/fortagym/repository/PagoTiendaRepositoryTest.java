package com.fortagym.repository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.model.PagoTienda;
import com.fortagym.model.Usuario;

@DataJpaTest
@ActiveProfiles("test")
class PagoTiendaRepositoryTest {

    @Autowired
    private PagoTiendaRepository pagoTiendaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByNumeroOrden_returns_saved() {
        Usuario u = new Usuario("P","T","12121212","pagador@test.com","passwd123", com.fortagym.model.Rol.USUARIO, null, null, null);
        usuarioRepository.save(u);

        PagoTienda pt = new PagoTienda();
        pt.setNumeroOrden("ORD-123");
        pt.setUsuario(u);
        pt.setTotalPagado(BigDecimal.valueOf(99.99));
        pagoTiendaRepository.save(pt);

        PagoTienda found = pagoTiendaRepository.findByNumeroOrden("ORD-123");
        assertThat(found).isNotNull();
        assertThat(found.getNumeroOrden()).isEqualTo("ORD-123");
    }
}
