package com.fortagym.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortagym.config.JwtUtil;
import com.fortagym.config.TestBeansConfig;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // Configuración local corregida sin clases internas erróneas
    @TestConfiguration
    static class LocalTestConfig {
        @Bean
        @Primary
        public AuthenticationManager testAuthenticationManager() {
            return mock(AuthenticationManager.class);
        }
        
        @Bean
        @Primary
        public UserDetailsService testUserDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        @Primary
        public JwtUtil testJwtUtil() {
            return mock(JwtUtil.class);
        }
    }

    @Test
    void login_DeberiaRetornarToken_CuandoCredencialesSonCorrectas() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@fortagym.com");
        request.setPassword("password123");

        org.springframework.security.core.userdetails.UserDetails userDetails = 
            new User("admin@fortagym.com", "password123", new ArrayList<>());
            
        when(userDetailsService.loadUserByUsername("admin@fortagym.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("token-matematico-falso-jwt");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-matematico-falso-jwt"));
    }

    @Test
    void login_DeberiaRetornar401_CuandoCredencialesSonIncorectas() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("usuario@mal.com");
        request.setPassword("claveIncorrecta");

        doThrow(new BadCredentialsException("❌ Credenciales incorrectas."))
            .when(authenticationManager).authenticate(any());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("❌ Credenciales incorrectas."));
    }
}
