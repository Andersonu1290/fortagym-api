package com.fortagym.controller;

import com.fortagym.model.Carrito;
import com.fortagym.model.CarritoId;
import com.fortagym.model.Producto;
import com.fortagym.model.Usuario;
import com.fortagym.repository.CarritoRepository;
import com.fortagym.repository.ProductoRepository;
import com.fortagym.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoRepository carritoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ProductoRepository productoRepo;

    // 🔐 Obtener usuario logueado desde JWT
    private Usuario getUsuarioLogueado() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ✅ Obtener MI carrito (Solo lectura)
    @GetMapping
    public List<Carrito> obtenerMiCarrito() {
        Usuario usuario = getUsuarioLogueado();
        return carritoRepo.findByUsuarioId(usuario.getId());
    }

    // ✅ Agregar producto al carrito (Operación de escritura)
    @Transactional
    @PostMapping("/add/{productoId}/{cantidad}")
    public ResponseEntity<?> agregarAlCarrito(
            @PathVariable Long productoId,
            @PathVariable Integer cantidad) {

        if (cantidad <= 0) {
            return ResponseEntity.badRequest()
                    .body("La cantidad debe ser mayor a 0");
        }

        Usuario usuario = getUsuarioLogueado();

        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getStock() == null || producto.getStock() <= 0) {
            return ResponseEntity.badRequest()
                    .body("Producto sin stock");
        }

        if (cantidad > producto.getStock()) {
            return ResponseEntity.badRequest()
                    .body("Stock insuficiente");
        }

        CarritoId carritoId = new CarritoId(usuario.getId(), productoId);

        Optional<Carrito> carritoExistente = carritoRepo.findById(carritoId);

        Carrito carrito;

        if (carritoExistente.isPresent()) {
            carrito = carritoExistente.get();
            int nuevaCantidad = carrito.getCantidad() + cantidad;

            if (nuevaCantidad > producto.getStock()) {
                return ResponseEntity.badRequest()
                        .body("No hay suficiente stock disponible");
            }

            carrito.setCantidad(nuevaCantidad);
        } else {
            carrito = new Carrito();
            carrito.setId(carritoId);
            carrito.setUsuario(usuario);
            carrito.setProducto(producto);
            carrito.setCantidad(cantidad);
        }

        carritoRepo.save(carrito);

        return ResponseEntity.ok("Producto agregado al carrito");
    }

    // ✅ Eliminar un producto específico (Operación de escritura)
    @Transactional
    @DeleteMapping("/eliminar/{productoId}")
    public ResponseEntity<?> eliminarDelCarrito(@PathVariable Long productoId) {

        Usuario usuario = getUsuarioLogueado();

        carritoRepo.deleteById(
                new CarritoId(usuario.getId(), productoId)
        );

        return ResponseEntity.ok("Producto eliminado");
    }

    // ✅ Limpiar carrito completo (Operación de escritura)
    @Transactional
    @DeleteMapping("/limpiar")
    public ResponseEntity<?> limpiarMiCarrito() {

        Usuario usuario = getUsuarioLogueado();

        carritoRepo.deleteByUsuarioId(usuario.getId());

        return ResponseEntity.ok("Carrito limpiado");
    }
}