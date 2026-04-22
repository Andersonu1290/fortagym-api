package com.fortagym.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fortagym.model.DetalleRutina;
import com.fortagym.model.Rutina;

public interface DetalleRutinaRepository extends JpaRepository<DetalleRutina, Long> {
    List<DetalleRutina> findByRutina(Rutina rutina);
    void deleteByRutina(Rutina rutina);

}
