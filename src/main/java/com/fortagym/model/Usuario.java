package com.fortagym.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50)
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50)
    private String apellido;

    // 🚀 NUEVO: Campo DNI normalizado a la tabla de Usuario
    @Column(unique = true, length = 15)
    private String dni;

    @Email
    @NotBlank
    @Column(unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 🔥 ESTA ES LA CLAVE
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6)
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Rol rol;

    @Lob
    private byte[] fotoPerfil;

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Nutricion nutricion;
    
    @ManyToOne
    @JoinColumn(name = "membresia_activa_id")
    private Membresia membresiaActiva;

    @Column(name = "fecha_fin_membresia")
    private java.time.LocalDateTime fechaFinMembresia;

    public Usuario() {}

    public Usuario(String nombre, String apellido, String dni, String email, String password, Rol rol, byte[] fotoPerfil, Membresia membresiaActiva, java.time.LocalDateTime fechaFinMembresia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.fotoPerfil = fotoPerfil;
        this.membresiaActiva = membresiaActiva;
        this.fechaFinMembresia = fechaFinMembresia;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    // 🚀 NUEVO: Getter y Setter para el DNI
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public byte[] getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(byte[] fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public Nutricion getNutricion() { return nutricion; }
    public void setNutricion(Nutricion nutricion) { this.nutricion = nutricion; }

    public Membresia getMembresiaActiva() { return membresiaActiva; }
    public void setMembresiaActiva(Membresia membresiaActiva) { this.membresiaActiva = membresiaActiva; }

    public java.time.LocalDateTime getFechaFinMembresia() { return fechaFinMembresia; }
    public void setFechaFinMembresia(java.time.LocalDateTime fechaFinMembresia) { this.fechaFinMembresia = fechaFinMembresia; }    
}