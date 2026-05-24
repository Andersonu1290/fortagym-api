package com.fortagym.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CarritoId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long usuarioId;
    private Long productoId;

    public CarritoId() {}

    public CarritoId(Long usuarioId, Long productoId) {
        this.usuarioId = usuarioId;
        this.productoId = productoId;
    }

    // Getters y Setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    // Equals y HashCode (Necesarios para el manejo de llaves compuestas en JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarritoId carritoId = (CarritoId) o;
        return Objects.equals(usuarioId, carritoId.usuarioId) && 
               Objects.equals(productoId, carritoId.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, productoId);
    }
}
