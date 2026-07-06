package com.fortagym.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.model.CategoriaProducto;
import com.fortagym.model.Producto;

@DataJpaTest
@ActiveProfiles("test")
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void findByCategoria_and_buscarPorNombreJPQL_work() {
        Producto p1 = new Producto("Barra", CategoriaProducto.ACCESORIOS, 10.0, 5, "desc", "img1");
        Producto p2 = new Producto("Banda elastica", CategoriaProducto.ACCESORIOS, 5.0, 10, "desc", "img2");
        Producto p3 = new Producto("Camiseta", CategoriaProducto.ROPA, 20.0, 3, "desc", "img3");
        productoRepository.saveAll(List.of(p1,p2,p3));

        List<Producto> accesorios = productoRepository.findByCategoria(CategoriaProducto.ACCESORIOS);
        assertThat(accesorios).hasSize(2);

        List<Producto> buscado = productoRepository.buscarPorNombreJPQL("camis");
        assertThat(buscado).hasSize(1).extracting(Producto::getNombre).contains("Camiseta");

        // basic findById should work
        assertThat(productoRepository.findById(p1.getId())).isPresent();
    }
}
