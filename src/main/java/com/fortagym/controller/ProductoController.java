package com.fortagym.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional; // 🔥 Import clave

import com.fortagym.model.CategoriaProducto;
import com.fortagym.model.Producto;
import com.fortagym.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @PostMapping("/guardar")
    @Transactional // 🔥 Otorga permisos de escritura en la BD para crear/editar
    public ResponseEntity<?> guardarProducto(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("categoria") String categoria,
            @RequestParam("precio") Double precio,
            @RequestParam("stock") Integer stock,
            @RequestParam("descripcion") String descripcion,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "img", required = false) String imgUrl) {
        
        try {
            System.out.println("DEBUG: Guardando producto: " + nombre + " | Categoria: " + categoria);
            
            Producto producto = (id != null) ? productoService.obtenerPorId(id) : new Producto();
            if (producto == null) return ResponseEntity.notFound().build();

            producto.setNombre(nombre);
            // Conversión segura de Enum
            try {
                producto.setCategoria(CategoriaProducto.valueOf(categoria.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Categoría inválida"));
            }
            
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setDescripcion(descripcion);

            // Lógica de imagen idéntica a Promociones
            if (file != null && !file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                producto.setImg("/uploads/" + fileName);
            } else if (imgUrl != null && !imgUrl.isEmpty()) {
                producto.setImg(imgUrl);
            }

            return ResponseEntity.ok(productoService.guardarProducto(producto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @Transactional // 🔥 Otorga permisos de escritura en la BD para borrar
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            Producto p = productoService.obtenerPorId(id);
            if (p != null && p.getImg() != null && p.getImg().startsWith("/uploads/")) {
                File archivo = new File(uploadDir + p.getImg().replace("/uploads/", ""));
                if (archivo.exists()) archivo.delete();
            }
            productoService.eliminarProducto(id);
            return ResponseEntity.ok(Collections.singletonMap("mensaje", "Producto eliminado"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}