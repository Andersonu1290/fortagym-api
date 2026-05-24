package com.fortagym.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promocion")
public class Promocion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String imagen; // ruta del archivo
    private LocalDateTime fechaSubida = LocalDateTime.now();

    // 🆕 NUEVOS CAMPOS
    private String titulo;

    @Column(length = 1000) // Evita que se quede corto si escribes un texto largo
    private String descripcion;

    public Promocion() {}

    public Promocion(String nombre, String imagen) {
        this.nombre = nombre;
        this.imagen = imagen;
    }

    // 🆕 NUEVO CONSTRUCTOR
    public Promocion(String nombre, String imagen, String titulo, String descripcion) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }

    // 🆕 GETTERS Y SETTERS NUEVOS CORREGIDOS
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
