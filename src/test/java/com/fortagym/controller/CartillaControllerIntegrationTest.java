package com.fortagym.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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
@AutoConfigureMockMvc(addFilters = false)  // ❗ Desactiva los filtros de seguridad
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
    void cleanup() {
        detalleRutinaRepository.deleteAll();
        rutinaRepository.deleteAll();
        nutricionRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void verCartilla_y_exportarExcel_flow() throws Exception {
        Usuario u = new Usuario("C","U","cartilla@test.com","123456", Rol.USUARIO, null);
        usuarioRepository.save(u);

        Nutricion n = new Nutricion(u, "analisis", "obs");
        nutricionRepository.save(n);

        Rutina r = new Rutina("obs","Coach",u);
        DetalleRutina d = new DetalleRutina();
        d.setEjercicio("Curl"); d.setSeriesReps("3x10"); d.setDescanso("60s"); d.setDias("Lun");
        d.setRutina(r); r.getDetalles().add(d);
        rutinaRepository.save(r);

        mockMvc.perform(get("/cartilla/{idUsuario}", u.getId()))
            .andExpect(status().isOk());

        // exportar excel
        byte[] bytes = mockMvc.perform(get("/cartilla/exportar/{usuarioId}", u.getId()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();

        assertThat(bytes.length).isGreaterThan(0);

        // opcional: validar que el contenido sea un archivo Excel (intentar abrir con POI)
        try (var wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertThat(wb.getNumberOfSheets()).isGreaterThanOrEqualTo(1);
        }
    }
}
