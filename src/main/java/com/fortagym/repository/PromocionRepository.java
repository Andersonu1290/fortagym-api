package com.fortagym.repository;

import com.fortagym.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    Promocion findTopByOrderByFechaSubidaDesc(); // última promoción
}
