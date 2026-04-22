package com.fortagym.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "nutricion")
public class Nutricion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nutricion")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", unique = true, nullable = false)
    @JsonIgnore // Evita que se serialice el usuario para prevenir ciclos infinitos en JSON
    private Usuario usuario;

    @NotBlank(message = "El análisis corporal es obligatorio")
    @Size(max = 255, message = "El análisis corporal no debe superar los 255 caracteres")
    @Column(name = "analisis_corporal", length = 255)
    private String analisisCorporal;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Lob
    @Size(max = 5000, message = "Las observaciones no deben superar los 5000 caracteres")
    private String observaciones;

    public Nutricion() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Nutricion(Usuario usuario, String analisisCorporal, String observaciones) {
        this.usuario = usuario;
        this.analisisCorporal = analisisCorporal;
        this.observaciones = observaciones;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getAnalisisCorporal() { return analisisCorporal; }
    public void setAnalisisCorporal(String analisisCorporal) { this.analisisCorporal = analisisCorporal; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
