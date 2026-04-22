package com.fortagym.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "membresias")
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El tipo de membresía es obligatorio")
    @Size(max = 50, message = "El tipo no debe superar los 50 caracteres")
    private String tipo;

    @Min(value = 1, message = "La duración mínima es de 1 mes")
    @Max(value = 5, message = "La duración máxima es de 5 meses")
    @Column(name = "duracion_meses")
    private int duracionMeses;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no debe superar 500 caracteres")
    private String descripcion;

    @Positive(message = "El precio debe ser mayor a 0")
    private double precio;

    @Size(max = 255, message = "La URL de la imagen no debe superar los 255 caracteres")
    @Pattern(
        regexp = "^[\\w\\-./]+\\.(png|jpg|jpeg|webp)$",
        message = "Debe ser una imagen válida (png, jpg, jpeg, webp)"
    )
    @Column(name = "imagen_url")
    private String imagenUrl;

    public Membresia() {}

    public Membresia(String tipo, int duracionMeses, String descripcion, double precio, String imagenUrl) {
        this.tipo = tipo;
        this.duracionMeses = duracionMeses;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getDuracionMeses() {
        return duracionMeses;
    }

    public void setDuracionMeses(int duracionMeses) {
        this.duracionMeses = duracionMeses;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
