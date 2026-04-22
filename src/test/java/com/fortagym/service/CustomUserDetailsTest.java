package com.fortagym.service;

import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.service.CustomUserDetails;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void authoritiesContainRolePrefix() {
        Usuario u = new Usuario();
        u.setEmail("a@b");
        u.setPassword("p");
        u.setRol(Rol.USUARIO);

        CustomUserDetails details = new CustomUserDetails(u);
        var auth = details.getAuthorities();
        assertFalse(auth.isEmpty());
        assertTrue(auth.iterator().next().getAuthority().contains("ROLE_"));
    }
}
