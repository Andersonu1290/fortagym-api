package com.fortagym.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class ConsejoService {

    private final List<String> consejos = List.of(
            "Toma al menos 2 litros de agua al día 💧",
            "Evita saltarte comidas para mantener tu metabolismo activo ⚡",
            "Consume proteína en cada comida para favorecer el crecimiento muscular 💪",
            "Duerme mínimo 7 horas para mejorar tu recuperación 😴",
            "Incluye frutas y verduras para mantener tu salud digestiva 🍏🥦",
            "Come carbohidratos complejos antes de entrenar para tener energía 🚀",
            "No olvides calentar antes del ejercicio y estirar al terminar 🧘",
            "Controla las porciones, no te prives, aprende a balancear 🍽️",
            "Incluye grasas saludables: palta, frutos secos, aceite de oliva 🥑",
            "Haz ejercicio al menos 4 veces por semana para mantener tu progreso 🏋️‍♂️"
    );

    public String obtenerConsejo() {
        Random random = new Random();
        return consejos.get(random.nextInt(consejos.size()));
    }
}
