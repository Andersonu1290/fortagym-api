package com.fortagym.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fortagym.model.PaseDiario;

public interface PaseDiarioRepository extends JpaRepository<PaseDiario, Long> {
}
