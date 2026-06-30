package com.fortagym.controller;

import com.fortagym.model.Usuario;
import com.fortagym.model.Nutricion;
import com.fortagym.model.Rutina;
import com.fortagym.model.DetalleRutina;
import com.fortagym.repository.UsuarioRepository;
import com.fortagym.repository.NutricionRepository;
import com.fortagym.repository.RutinaRepository;
import com.fortagym.repository.DetalleRutinaRepository;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController 
@RequestMapping("/api/cartilla")
@Transactional(readOnly = true) // 🔥 Optimización: Solo lectura para este controlador
public class CartillaController {

    private static final Logger logger = LoggerFactory.getLogger(CartillaController.class);

    private final UsuarioRepository usuarioRepository;
    private final NutricionRepository nutricionRepository;
    private final RutinaRepository rutinaRepository;
    private final DetalleRutinaRepository detalleRutinaRepository;

    public CartillaController(
            UsuarioRepository usuarioRepository,
            NutricionRepository nutricionRepository,
            RutinaRepository rutinaRepository,
            DetalleRutinaRepository detalleRutinaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.nutricionRepository = nutricionRepository;
        this.rutinaRepository = rutinaRepository;
        this.detalleRutinaRepository = detalleRutinaRepository;
    }

    // ==============================
    // 1. OBTENER CARTILLA EN JSON
    // ==============================
    @GetMapping("/{idUsuario}")
    public ResponseEntity<?> verCartilla(@PathVariable Long idUsuario) {
        logger.info("📄 Solicitando cartilla digital (JSON) del usuario con ID {}", idUsuario);

        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensajeResponse("Usuario no encontrado"));
        }

        // ❌ BORRADO: ya no necesitamos usuario.setPassword(null); Gracias a @JsonIgnore

        Nutricion nutricion = nutricionRepository.findByUsuario(usuario).orElse(null);
        Rutina rutina = rutinaRepository.findByUsuario(usuario).orElse(null);
        List<DetalleRutina> detalles = rutina != null ? detalleRutinaRepository.findByRutina(rutina) : Collections.emptyList();

        Map<String, Object> response = new HashMap<>();
        response.put("usuario", usuario);
        response.put("nutricion", nutricion);
        response.put("rutina", rutina);
        response.put("detalles", detalles);

        return ResponseEntity.ok(response);
    }

    // ==============================
    // 2. EXPORTAR EXCEL (Vía REST)
    // ==============================
    @GetMapping("/exportar/{usuarioId}")
    public ResponseEntity<byte[]> exportarCartillaExcel(@PathVariable Long usuarioId) {
        logger.info("📊 Solicitando exportación Excel del usuario ID {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Nutricion nutricion = nutricionRepository.findByUsuario(usuario).orElse(null);
        Rutina rutina = rutinaRepository.findByUsuario(usuario).orElse(null);
        List<DetalleRutina> detalles = rutina != null ? detalleRutinaRepository.findByRutina(rutina) : Collections.emptyList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // --- HOJA 1: NUTRICIÓN ---
            Sheet sheetNutri = workbook.createSheet("Nutrición");
            Row titulo = sheetNutri.createRow(0);
            titulo.createCell(0).setCellValue("Cartilla Nutricional");

            if (nutricion != null) {
                sheetNutri.createRow(2).createCell(0).setCellValue("Análisis Corporal");
                sheetNutri.getRow(2).createCell(1).setCellValue(nutricion.getAnalisisCorporal());
                sheetNutri.createRow(3).createCell(0).setCellValue("Fecha de Registro");
                sheetNutri.getRow(3).createCell(1).setCellValue(nutricion.getFechaRegistro().toString());
                sheetNutri.createRow(4).createCell(0).setCellValue("Observaciones");
                sheetNutri.getRow(4).createCell(1).setCellValue(nutricion.getObservaciones());
            }

            // --- HOJA 2: RUTINA ---
            Sheet sheetRutina = workbook.createSheet("Rutina");
            Row tituloR = sheetRutina.createRow(0);
            tituloR.createCell(0).setCellValue("Rutina de Entrenamiento");

            if (rutina != null) {
                sheetRutina.createRow(2).createCell(0).setCellValue("Entrenador");
                sheetRutina.getRow(2).createCell(1).setCellValue(rutina.getNombreEntrenador());

                Row head = sheetRutina.createRow(4);
                head.createCell(0).setCellValue("Ejercicio");
                head.createCell(1).setCellValue("Series / Reps");
                head.createCell(2).setCellValue("Descanso");
                head.createCell(3).setCellValue("Días");

                int rowIdx = 5;
                for (DetalleRutina d : detalles) {
                    Row row = sheetRutina.createRow(rowIdx++);
                    row.createCell(0).setCellValue(d.getEjercicio());
                    row.createCell(1).setCellValue(d.getSeriesReps());
                    row.createCell(2).setCellValue(d.getDescanso());
                    row.createCell(3).setCellValue(d.getDias());
                }
            }

            workbook.write(out);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "cartilla_" + usuario.getNombre() + ".xlsx");

            return ResponseEntity.ok().headers(headers).body(out.toByteArray());

        } catch (IOException e) {
            logger.error("❌ Error al generar el Excel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}