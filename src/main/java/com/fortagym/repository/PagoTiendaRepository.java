package com.fortagym.repository;

import com.fortagym.model.PagoTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoTiendaRepository extends JpaRepository<PagoTienda, Long> {
    PagoTienda findByNumeroOrden(String numeroOrden);
}