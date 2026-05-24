package com.fortagym.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Consultas automáticas de Spring Data (Derived Queries)
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByRol(Rol rol);

    boolean existsByEmail(String email);

    // =====================================================================
    // CONSULTAS JPQL
    // =====================================================================

    // 1. Consulta JPQL simple: Buscar usuarios por su rol
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol")
    List<Usuario> buscarUsuariosPorRolJPQL(@Param("rol") Rol rol);

    // 2. Consulta JPQL avanzada: Buscar usuarios por coincidencia en nombre o apellido
    @Query("""
        SELECT u 
        FROM Usuario u 
        WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) 
           OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :filtro, '%'))
    """)
    List<Usuario> buscarPorNombreOApellidoJPQL(@Param("filtro") String filtro);

}