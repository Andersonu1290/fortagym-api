package com.fortagym.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (usuario == null) {
            throw new UsernameNotFoundException("❌ Usuario no encontrado con el email: " + email);
        }

        return new CustomUserDetails(usuario);
    }
}
