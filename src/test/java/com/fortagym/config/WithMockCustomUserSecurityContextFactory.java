package com.fortagym.config;

import com.fortagym.model.Rol;
import com.fortagym.model.Usuario;
import com.fortagym.repository.UsuarioRepository;
import com.fortagym.service.CustomUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String email = annotation.email();

        // Buscar usuario o crearlo
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            usuario = new Usuario();
            usuario.setNombre("Mock");
            usuario.setApellido("User");
            usuario.setEmail(email);
            usuario.setPassword("123456");
            usuario.setRol(Rol.USUARIO);

            usuario = usuarioRepository.save(usuario);
        }

        CustomUserDetails cud = new CustomUserDetails(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                cud,
                null,
                cud.getAuthorities()
        );

        context.setAuthentication(auth);
        return context;
    }
}
