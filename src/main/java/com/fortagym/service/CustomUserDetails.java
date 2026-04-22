package com.fortagym.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fortagym.model.Usuario;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final Usuario usuario;

    public Long getId() {
    return usuario.getId(); // Asumiendo que internamente usas un objeto Usuario
    }


    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return usuario.getNombre();
    }

    public String getApellido() {
        return usuario.getApellido();
    }

    public String getNombreCompleto() {
        return usuario.getNombre() + " " + usuario.getApellido();
    }
    public Usuario getUsuario() {
    return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
    if (usuario.getRol() == null) {
        return Collections.emptyList();
    }
    return Collections.singletonList(() -> "ROLE_" + usuario.getRol().name());
    }


    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}