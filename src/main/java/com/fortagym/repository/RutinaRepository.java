package com.fortagym.repository;

import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    Optional<Rutina> findByUsuarioId(Long usuarioId);
    Optional<Rutina> findByUsuario(Usuario usuario);
    boolean existsByUsuarioId(Long usuarioId);

}
