package com.fortagym.service;

import com.fortagym.model.Producto;
import com.fortagym.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> obtenerEliminados() {
        return productoRepository.findByActivoFalse();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado"));

        producto.setActivo(false);

        productoRepository.save(producto);
    }

    public void restaurarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        producto.setActivo(true);
        productoRepository.save(producto);
    }
    
    public void eliminarProductoDefinitivo(Long id) {
        productoRepository.deleteById(id);
    }
}