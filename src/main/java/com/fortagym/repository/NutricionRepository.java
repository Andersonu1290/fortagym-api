package com.fortagym.repository;

import com.fortagym.model.Nutricion;
import com.fortagym.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NutricionRepository extends JpaRepository<Nutricion, Long> {
    
    Optional<Nutricion> findByUsuario(Usuario usuario);
    boolean existsByUsuarioId(Long id);
    Optional<Nutricion> findByUsuarioId(Long usuarioId);

    // 🔥 LA OPCIÓN NUCLEAR: Borrado directo a la Base de Datos
    @Modifying
    @Transactional
    @Query("DELETE FROM Nutricion n WHERE n.usuario.id = :usuarioId")
    void eliminarDirectamentePorUsuarioId(@Param("usuarioId") Long usuarioId);
}