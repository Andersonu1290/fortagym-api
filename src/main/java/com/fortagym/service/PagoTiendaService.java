package com.fortagym.service;

import com.fortagym.dto.CheckoutRequestDTO;
import com.fortagym.model.*;
import com.fortagym.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fortagym.service.CustomUserDetails;


@Service
public class PagoTiendaService {

    @Autowired private PagoTiendaRepository pagoTiendaRepo;
    @Autowired private DetallePagoTiendaRepository detalleRepo;
    @Autowired private ProductoRepository productoRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    // @Transactional es clave: Si algo falla a la mitad, se cancela TODO (rollback). No habrá cobros fantasma.
    @Transactional 
    public PagoTienda procesarCheckout(CheckoutRequestDTO request) {
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = ((CustomUserDetails) principal).getUsername();
        Usuario usuario = usuarioRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetallePagoTienda> detalles = new ArrayList<>();

        // 1. Iterar carrito, verificar productos y calcular subtotal real desde BD
        for (CheckoutRequestDTO.ItemCarritoDTO item : request.getItems()) {
            Producto prod = productoRepo.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + item.getProductoId()));
            
            BigDecimal precioBD = BigDecimal.valueOf(prod.getPrecio());
            BigDecimal subItem = precioBD.multiply(BigDecimal.valueOf(item.getCantidad()));
            
            subtotal = subtotal.add(subItem);

            DetallePagoTienda det = new DetallePagoTienda();
            det.setProducto(prod);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precioBD);
            det.setSubtotalItem(subItem);
            
            detalles.add(det);

            if (prod.getStock() == null || prod.getStock() < item.getCantidad()) {
                throw new RuntimeException(
                    "Stock insuficiente para el producto: " + prod.getNombre()
                );
            }
            
            // Aquí iría tu lógica de restar STOCK si tu entidad Producto tiene el campo "stock"
            prod.setStock(prod.getStock() - item.getCantidad());
            productoRepo.save(prod);
        }

        // 2. Cálculos financieros (Descuento, Envío, IGV)
        BigDecimal costoEnvio = "express".equalsIgnoreCase(request.getMetodoEntrega()) ? new BigDecimal("15.00") : BigDecimal.ZERO;
        BigDecimal descuento = BigDecimal.ZERO;
        
        if ("FORTA10".equalsIgnoreCase(request.getCodigoCupon())) {
            descuento = subtotal.multiply(new BigDecimal("0.10"));
        }

        BigDecimal totalPagar = subtotal.subtract(descuento).add(costoEnvio);
        BigDecimal divisorIgv = new BigDecimal("1.18");
        BigDecimal igv = totalPagar.subtract(totalPagar.divide(divisorIgv, 2, RoundingMode.HALF_UP));

        // 3. Crear el Pago (Recibo)
        PagoTienda pago = new PagoTienda();
        // Generamos un número de orden único corto
        pago.setNumeroOrden("FG-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        pago.setUsuario(usuario);
        pago.setSubtotal(subtotal);
        pago.setCostoEnvio(costoEnvio);
        pago.setDescuento(descuento);
        pago.setIgv(igv);
        pago.setTotalPagado(totalPagar);
        pago.setCodigoCupon(request.getCodigoCupon());
        
        pago.setMetodoEntrega(request.getMetodoEntrega());
        pago.setDireccionEnvio(request.getDireccion());
        pago.setDepartamento(request.getDepartamento());
        pago.setProvincia(request.getProvincia());
        pago.setDistrito(request.getDistrito());
        pago.setCodigoPostal(request.getCodigoPostal());
        pago.setReferencia(request.getReferencia());
        
        pago.setMetodoPago(request.getMetodoPago());
        pago.setEstadoPago("PAGADO"); // Asumimos éxito por ahora

        PagoTienda pagoGuardado = pagoTiendaRepo.save(pago);

        // 4. Guardar los Detalles (Relacionados al pago generado)
        for (DetallePagoTienda det : detalles) {
            det.setPagoTienda(pagoGuardado);
            detalleRepo.save(det);
        }

        return pagoGuardado;
    }
}