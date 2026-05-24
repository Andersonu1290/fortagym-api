package com.fortagym.repository;

import com.fortagym.model.DetallePagoTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePagoTiendaRepository extends JpaRepository<DetallePagoTienda, Long> {
}