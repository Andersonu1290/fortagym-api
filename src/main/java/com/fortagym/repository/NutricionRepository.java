package com.fortagym.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fortagym.model.Nutricion;
import com.fortagym.model.Usuario;

@Repository
public interface NutricionRepository extends JpaRepository<Nutricion, Long> {

    Optional<Nutricion> findByUsuario(Usuario usuario);

    boolean existsByUsuarioId(Long usuarioId);

    Optional<Nutricion> findByUsuarioId(Long usuarioId);
}
