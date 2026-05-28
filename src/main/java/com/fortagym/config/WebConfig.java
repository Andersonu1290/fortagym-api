package com.fortagym.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🚀 Inyectamos la variable dinámica
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Transformamos tu carpeta "uploads" en una ruta absoluta exacta de tu disco duro
        Path uploadPath = Paths.get(uploadDir);
        String rutaAbsoluta = uploadPath.toFile().getAbsolutePath();

        // 2. Garantizamos que el formato sea el correcto para el servidor web añadiendo un "/" o "\"
        if (!rutaAbsoluta.endsWith("/") && !rutaAbsoluta.endsWith("\\")) {
            rutaAbsoluta += "/";
        }

        // 3. Mapea las imágenes subidas para que sean accesibles vía URL de forma dinámica
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + rutaAbsoluta);
                
        System.out.println("DEBUG: Sirviendo imágenes dinámicas desde la ruta absoluta -> " + rutaAbsoluta);
                
        // 4. Las imágenes estáticas de tu carpeta img (este se queda igual porque va dentro del .jar)
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");
    }
}