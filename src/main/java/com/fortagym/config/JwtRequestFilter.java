package com.fortagym.config;

import com.fortagym.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 🔥 EXCLUSIÓN DE FILTRADO INTEGRAL PARA SWAGGER UI
     * Le indica a Spring Security qué rutas específicas NO deben ser evaluadas por este filtro.
     * Al usar términos amplios sin barras fijas, garantizamos que las consultas de metadatos 
     * internos (como swagger-config o webjars) pasen directo libremente.
     */
@Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Bloqueo expansivo para que el filtro JWT ignore cualquier archivo estático o JSON de Swagger
        return path.contains("api-docs") 
            || path.contains("swagger-ui") 
            || path.contains("swagger-resources") 
            || path.contains("swagger-config")
            || path.contains("webjars");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        // 1. Revisamos si Angular nos está enviar el token en la "Cabecera" de la petición
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Por estándar mundial, los tokens JWT se envían con la palabra "Bearer " antes.
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Quitamos la palabra "Bearer " para quedarnos solo con el token
            try {
                username = jwtUtil.extractUsername(jwt); // Sacamos el correo del usuario
            } catch (Exception e) {
                // Si el token está corrupto, limpiamos el contexto
                SecurityContextHolder.clearContext();
                logger.error("Token inválido o expirado");
            }
        }

        // 2. Si el token tiene un usuario y aún no ha sido autenticado en este ciclo...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Buscamos al usuario en la base de datos
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 3. Validamos matemáticamente que el token no haya sido alterado por un hacker
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 4. Si todo es correcto, le decimos a Spring Security: "Déjalo pasar, es quien dice ser"
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // Si la validación falla, limpiamos el contexto
                SecurityContextHolder.clearContext();
            }
        }
        
        // 5. Dejamos que la petición siga su camino hacia el controlador correspondiente
        chain.doFilter(request, response);
    }
}
