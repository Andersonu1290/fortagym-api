package com.fortagym.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "carrito_usuario")
public class Carrito {

    @EmbeddedId
    private CarritoId id;

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @MapsId("productoId")
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Integer cantidad;

    // Constructor vacío obligatorio para JPA
    public Carrito() {}

    // Getters y Setters
    public CarritoId getId() { return id; }
    public void setId(CarritoId id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    // Recomendado para entidades JPA: equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Carrito carrito = (Carrito) o;
        return Objects.equals(id, carrito.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
