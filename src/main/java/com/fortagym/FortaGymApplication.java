package com.fortagym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;
import jakarta.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class FortaGymApplication {

    public static void main(String[] args) {
        SpringApplication.run(FortaGymApplication.class, args);
    }

    // ==========================================
    // FORZAR ZONA HORARIA A PERÚ PARA TODO EL SISTEMA
    // ==========================================
    @PostConstruct
    public void init() {
        // Esto obliga a Render (y a cualquier otro servidor) a usar la hora de Lima
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        System.out.println("✅ Zona horaria del sistema configurada a: " + TimeZone.getDefault().getID());
    }

}