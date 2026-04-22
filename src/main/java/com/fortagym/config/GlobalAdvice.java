package com.fortagym.config;

import com.fortagym.service.ConsejoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalAdvice {

    @Autowired
    private ConsejoService consejoService;

    @ModelAttribute("consejoAleatorio")
    public String consejoAleatorio() {
        return consejoService.obtenerConsejo();
    }
}
