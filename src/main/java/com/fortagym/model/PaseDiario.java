package com.fortagym.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "pase_diario")
public class PaseDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La duración del pase es obligatoria")
    @Pattern(
        regexp = "1 día|3 días|7 días",
        message = "La duración debe ser 1 día, 3 días o 7 días"
    )
    @Column(nullable = false, length = 20)
    private String duracion;

    @NotBlank(message = "El tipo de acceso es obligatorio")
    @Column(nullable = false, length = 50)
    private String acceso;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    @Column(nullable = false)
    private Double precio;

    @NotBlank(message = "La imagen es obligatoria")
    @Column(name = "imagen_url", nullable = false, length = 255)
    private String imagenUrl;

    // === Constructores ===
    public PaseDiario() {}

    public PaseDiario(String duracion, String acceso, Double precio, String imagenUrl) {
        this.duracion = duracion;
        this.acceso = acceso;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
    }

    // === Getters y Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getAcceso() { return acceso; }
    public void setAcceso(String acceso) { this.acceso = acceso; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
