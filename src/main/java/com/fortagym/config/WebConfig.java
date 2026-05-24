package com.fortagym.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🚀 Inyectamos la variable dinámica
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea las imágenes subidas para que sean accesibles vía URL de forma dinámica
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
                
        // Las imágenes estáticas de tu carpeta img (este se queda igual porque va dentro del .jar)
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
    }
}