package com.fortagym.repository;

import com.fortagym.model.CategoriaProducto;
import com.fortagym.model.Producto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Producto> findById(Long id);
    
    // Método automático de Spring Data
    List<Producto> findByCategoria(CategoriaProducto categoria);

    // Búsqueda personalizada usando JPQL para el buscador del administrador
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    List<Producto> buscarPorNombreJPQL(@Param("filtro") String filtro);
}