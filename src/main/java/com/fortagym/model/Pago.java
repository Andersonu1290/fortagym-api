package com.fortagym.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "tarjeta|presencial", flags = Pattern.Flag.CASE_INSENSITIVE, message = "El método de pago debe ser 'tarjeta' o 'presencial'")
    @Column(name = "metodo_pago", length = 20, nullable = false)
    private String metodoPago;

    // 🚀 CAMBIO APLICADO: Reemplazo de DNI por numeroOperacion
    @NotBlank(message = "El número de operación o comprobante es obligatorio")
    @Column(name = "numero_operacion", length = 50, nullable = false)
    private String numeroOperacion;

    @NotNull(message = "El monto no puede estar vacío")
    @Positive(message = "El monto debe ser mayor a 0")
    @Column(nullable = false)
    private Double monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @NotBlank(message = "El estado del pago es obligatorio")
    @Pattern(regexp = "pendiente|verificado|pagado", flags = Pattern.Flag.CASE_INSENSITIVE, message = "El estado debe ser pendiente, verificado o pagado")
    @Column(length = 20, nullable = false)
    private String estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "membresia_id", nullable = false)
    private Membresia membresia;

    public Pago() {
        this.fechaPago = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    // 🚀 CAMBIO APLICADO: Nuevos Getters y Setters
    public String getNumeroOperacion() {
        return numeroOperacion;
    }

    public void setNumeroOperacion(String numeroOperacion) {
        this.numeroOperacion = numeroOperacion;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Membresia getMembresia() {
        return membresia;
    }

    public void setMembresia(Membresia membresia) {
        this.membresia = membresia;
    }
}