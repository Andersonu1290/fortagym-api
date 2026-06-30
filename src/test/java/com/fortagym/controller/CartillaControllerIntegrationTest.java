package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.io.ByteArrayInputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fortagym.config.TestBeansConfig;
import com.fortagym.model.DetalleRutina;
import com.fortagym.model.Nutricion;
import com.fortagym.model.Rol;
import com.fortagym.model.Rutina;
import com.fortagym.model.Usuario;
import com.fortagym.repository.DetalleRutinaRepository;
import com.fortagym.repository.NutricionRepository;
import com.fortagym.repository.RutinaRepository;
import com.fortagym.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
@EntityScan("com.fortagym.model")
@EnableJpaRepositories("com.fortagym.repository")
@Transactional
class CartillaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NutricionRepository nutricionRepository;

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private DetalleRutinaRepository detalleRutinaRepository;

    @BeforeEach
    void setUp() {
        detalleRutinaRepository.deleteAll();
        rutinaRepository.deleteAll();
        nutricionRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void obtenerCartillaYExportarExcel() throws Exception {

        Usuario usuario = new Usuario(
                "Carlos",
                "Urrutia",
                "44444444",
                "cartilla@test.com",
                "123456",
                Rol.USUARIO,
                null,
                null,
                null
        );
        usuarioRepository.save(usuario);

        Nutricion nutricion = new Nutricion(usuario, "Analisis corporal", "Sin observaciones");
        nutricionRepository.save(nutricion);

        Rutina rutina = new Rutina("Rutina de prueba", "Coach", usuario);

        DetalleRutina detalle = new DetalleRutina();
        detalle.setEjercicio("Curl");
        detalle.setSeriesReps("3x10");
        detalle.setDescanso("60s");
        detalle.setDias("Lunes");
        detalle.setRutina(rutina);

        rutina.getDetalles().add(detalle);

        rutinaRepository.save(rutina);

        // ===========================
        // API JSON
        // ===========================
        mockMvc.perform(get("/api/cartilla/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usuario.nombre").value("Carlos"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andExpect(jsonPath("$.nutricion.analisisCorporal").value("Analisis corporal"))
                .andExpect(jsonPath("$.rutina.nombreEntrenador").value("Coach"))
                .andExpect(jsonPath("$.detalles[0].ejercicio").value("Curl"));

        // ===========================
        // Exportar Excel
        // ===========================
        byte[] bytes = mockMvc.perform(get("/api/cartilla/exportar/{id}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(bytes.length).isGreaterThan(0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {

            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
            assertThat(workbook.getSheet("Nutrición")).isNotNull();
            assertThat(workbook.getSheet("Rutina")).isNotNull();
        }
    }

    @Test
    void obtenerCartillaUsuarioInexistente() throws Exception {

        mockMvc.perform(get("/api/cartilla/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void exportarCartillaUsuarioInexistente() throws Exception {

        mockMvc.perform(get("/api/cartilla/exportar/999999"))
                .andExpect(status().isNotFound());
    }

}