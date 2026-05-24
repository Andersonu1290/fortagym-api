package com.fortagym.repository;

import com.fortagym.model.Carrito;
import com.fortagym.model.CarritoId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarritoRepository extends JpaRepository<Carrito, CarritoId> {

    // Obtener carrito de un usuario
    List<Carrito> findByUsuarioId(Long usuarioId);

    // Eliminar todo el carrito de un usuario
    void deleteByUsuarioId(Long usuarioId);
}