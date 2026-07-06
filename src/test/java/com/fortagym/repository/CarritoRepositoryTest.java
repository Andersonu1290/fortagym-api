package com.fortagym.repository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.model.Carrito;
import com.fortagym.model.CarritoId;
import com.fortagym.model.Producto;
import com.fortagym.model.Usuario;

@DataJpaTest
@ActiveProfiles("test")
class CarritoRepositoryTest {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void findByUsuarioId_and_deleteByUsuarioId_work() {
        Usuario u = new Usuario("C","U","31313131","cuser@test.com","passwd123", com.fortagym.model.Rol.USUARIO, null, null, null);
        usuarioRepository.save(u);

        Producto p = new Producto("Item", com.fortagym.model.CategoriaProducto.ROPA, 10.0, 1, "d", "i");
        productoRepository.save(p);

        Carrito c = new Carrito();
        CarritoId id = new CarritoId(u.getId(), p.getId());
        c.setId(id);
        c.setUsuario(u);
        c.setProducto(p);
        c.setCantidad(2);
        carritoRepository.save(c);

        List<Carrito> found = carritoRepository.findByUsuarioId(u.getId());
        assertThat(found).hasSize(1);

        carritoRepository.deleteByUsuarioId(u.getId());
        assertThat(carritoRepository.findByUsuarioId(u.getId())).isEmpty();
    }
}
