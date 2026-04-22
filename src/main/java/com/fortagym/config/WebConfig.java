package com.fortagym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Mapea las imágenes subidas para que sean accesibles vía URL
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:src/main/resources/static/uploads/");
            
    // También mapeamos las imágenes estáticas de tu carpeta img
    registry.addResourceHandler("/img/**")
            .addResourceLocations("classpath:/static/img/");
    }
}
