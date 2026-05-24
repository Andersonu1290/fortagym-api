package com.fortagym.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fortagym.model.Usuario;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return usuario.getId();
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
        String rolActual = usuario.getRol().name();
        // 🔥 ESTO ES LO QUE VERÁS EN LA CONSOLA DE JAVA (IntelliJ/Eclipse)
        System.out.println("DEBUG: El usuario tiene el rol: '" + rolActual + "'");
        
        return Collections.singletonList(new SimpleGrantedAuthority(rolActual));
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