package com.fortagym.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    List<Usuario> findByRol(Rol rol);
    boolean existsByEmail(String email);


}
