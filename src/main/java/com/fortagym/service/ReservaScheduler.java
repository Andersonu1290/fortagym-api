package com.fortagym.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReservaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReservaScheduler.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Se ejecuta automáticamente cada 1 hora (Cron: segundo minuto hora dia mes año)
    @Scheduled(cron = "0 0 * * * *")
    public void actualizarReservasVencidas() {
        logger.info("⏳ Ejecutando limpieza automática de reservas vencidas...");

        // Busca las reservas confirmadas cuya fecha y hora ya pasaron y las marca como VENCIDO
        String sqlUpdateReservas = "UPDATE reservas_entrenamiento " +
                                   "SET estado = 'VENCIDO' " +
                                   "WHERE estado = 'CONFIRMADA' AND fecha_hora_sesion < NOW()";
        
        int reservasActualizadas = jdbcTemplate.update(sqlUpdateReservas);

        if (reservasActualizadas > 0) {
            logger.info("✅ Se han marcado {} reservas como VENCIDAS.", reservasActualizadas);
            
            // Opcional: Liberar de nuevo el horario del entrenador para que otros lo puedan usar otra semana
            // jdbcTemplate.update("UPDATE horarios_entrenador SET disponible = true WHERE id IN (SELECT horario_id FROM reservas_entrenamiento WHERE estado = 'VENCIDO')");
        }
    }
}