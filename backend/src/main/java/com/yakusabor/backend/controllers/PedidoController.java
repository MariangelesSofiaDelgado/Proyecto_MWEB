package com.yakusabor.backend.controllers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakusabor.backend.dto.ActualizarEstadoPedidoRequest;
import com.yakusabor.backend.dto.PedidoDashboardResponse;
import com.yakusabor.backend.dto.PedidoDetalleResponse;
import com.yakusabor.backend.dto.PedidoResponse;
import com.yakusabor.backend.models.Insumo;
import com.yakusabor.backend.models.Mesa;
import com.yakusabor.backend.models.Pedido;
import com.yakusabor.backend.models.PedidoDetalle;
import com.yakusabor.backend.models.Producto;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.repositories.InsumoRepository;
import com.yakusabor.backend.repositories.MesaRepository;
import com.yakusabor.backend.repositories.PedidoRepository;
import com.yakusabor.backend.repositories.ProductoRepository;
import com.yakusabor.backend.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<PedidoDashboardResponse> listarPedidos() {
        return pedidoRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Pedido::getCreatedAt).reversed())
                .map(this::mapPedidoDashboard)
                .toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> crearPedido(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, Object>> items = getItems(request);
            if (items.isEmpty()) {
                return ResponseEntity.badRequest().body("El pedido debe tener al menos un producto.");
            }

            String tipo = normalizarTipo(getString(request, "tipoPedido", "tipo_pedido", "tipo"));
            Pedido pedido = new Pedido();
            pedido.setTipo(tipo);
            pedido.setEstado("nuevo");

            if ("presencial".equals(tipo)) {
                Integer mesaId = getInteger(request, "mesaId", "mesa_id");
                if (mesaId == null) {
                    return ResponseEntity.badRequest().body("Debes seleccionar una mesa para pedidos presenciales.");
                }

                Mesa mesa = mesaRepository.findById(mesaId)
                        .orElseThrow(() -> new IllegalArgumentException("La mesa seleccionada no existe."));
                pedido.setMesa(mesa);
            } else {
                String direccion = getString(request, "direccion", "direccionDelivery", "direccion_delivery");
                if (direccion.isEmpty()) {
                    return ResponseEntity.badRequest().body("Debes ingresar una dirección para delivery.");
                }
                pedido.setDireccionDelivery(direccion);
            }

            Integer meseroId = getInteger(request, "meseroId", "mesero_id");
            if (meseroId != null) {
                Usuario mesero = usuarioRepository.findById(meseroId)
                        .orElseThrow(() -> new IllegalArgumentException("El mesero seleccionado no existe."));
                pedido.setMesero(mesero);
            }

            BigDecimal totalCalculado = BigDecimal.ZERO;
            for (Map<String, Object> itemRequest : items) {
                Integer productoId = getInteger(itemRequest, "productoId", "producto_id");
                if (productoId == null) {
                    return ResponseEntity.badRequest().body("Cada detalle debe incluir un producto.");
                }

                Integer cantidadValue = getInteger(itemRequest, "cantidad");
                int cantidad = cantidadValue == null ? 1 : cantidadValue;
                if (cantidad <= 0) {
                    return ResponseEntity.badRequest().body("La cantidad debe ser mayor que cero.");
                }

                Producto producto = productoRepository.findById(productoId)
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

                if (!Boolean.TRUE.equals(producto.getDisponible())) {
                    return ResponseEntity.badRequest().body("El producto no está disponible: " + producto.getNombre());
                }

                BigDecimal precioUnitario = getBigDecimal(itemRequest, "precioUnitario", "precio_unitario");
                precioUnitario = precioUnitario != null
                        ? precioUnitario
                        : BigDecimal.valueOf(producto.getPrecio());

                PedidoDetalle detalle = new PedidoDetalle();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precioUnitario);
                detalle.setNotas(getString(itemRequest, "notas"));
                pedido.agregarDetalle(detalle);

                totalCalculado = totalCalculado.add(precioUnitario.multiply(BigDecimal.valueOf(cantidad)));
            }

            pedido.setTotal(totalCalculado);
            Pedido pedidoGuardado = pedidoRepository.save(pedido);

            PedidoResponse response = new PedidoResponse(
                    pedidoGuardado.getId(),
                    pedidoGuardado.getTipo(),
                    pedidoGuardado.getEstado(),
                    pedidoGuardado.getTotal(),
                    pedidoGuardado.getCreatedAt(),
                    "Pedido registrado correctamente.");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar el pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    @Transactional
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody ActualizarEstadoPedidoRequest request) {
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("El pedido seleccionado no existe."));

            String nuevoEstado = normalizarEstado(request.getEstado());
            pedido.setEstado(nuevoEstado);

            // Descontar stock cuando el pedido cambia a en_preparacion o listo
            if ("en_preparacion".equals(nuevoEstado) || "listo".equals(nuevoEstado)) {
                descontarStockPorPedido(id);
            }

            if (pedido.getMesa() != null && ("entregado".equals(nuevoEstado) || "cancelado".equals(nuevoEstado))) {
                pedido.getMesa().setEstado("libre");
            }

            Pedido pedidoActualizado = pedidoRepository.save(pedido);
            return ResponseEntity.ok(mapPedidoDashboard(pedidoActualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al actualizar el pedido: " + e.getMessage());
        }
    }

    // ==========================================
    // NUEVO ENDPOINT INTEGRADO
    // ==========================================
    @PutMapping("/{pedidoId}/detalles/{detalleId}/estado")
    @Transactional
    public ResponseEntity<?> actualizarEstadoDetalle(
            @PathVariable Integer pedidoId,
            @PathVariable Integer detalleId,
            @RequestBody ActualizarEstadoPedidoRequest request) {
        try {
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

            PedidoDetalle detalle = pedido.getDetalles().stream()
                    .filter(d -> d.getId().equals(detalleId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado."));

            List<String> estadosPermitidos = List.of("pendiente", "en_preparacion", "listo", "entregado");
            String nuevoEstado = request.getEstado().trim().toLowerCase();

            if (!estadosPermitidos.contains(nuevoEstado)) {
                return ResponseEntity.badRequest().body("Estado inválido.");
            }

            detalle.setEstadoDetalle(nuevoEstado);
            pedidoRepository.save(pedido);

            return ResponseEntity
                    .ok(Map.of("mensaje", "Estado actualizado", "detalleId", detalleId, "estado", nuevoEstado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    // ==========================================
    // DESCUENTO DE STOCK
    // ==========================================

    /**
     * Descuenta el stock de insumos basado en los productos del pedido.
     * Consulta la tabla intermedia producto_insumo para obtener los insumos
     * asociados a cada producto y descuenta la cantidad total según la cantidad pedida.
     * 
     * @param pedidoId ID del pedido
     */
    @Transactional
    public void descontarStockPorPedido(Integer pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            return;
        }

        // Iterar sobre cada detalle del pedido
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            int cantidadPedida = detalle.getCantidad();

            // Consultar los insumos vinculados al producto desde la tabla intermedia
            List<Object[]> insumosAsociados = productoRepository.findInsumosAsociadosByProductoId(producto.getId());

            // Por cada insumo asociado, descontar el stock
            for (Object[] row : insumosAsociados) {
                Integer insumoId = ((Number) row[0]).intValue();
                BigDecimal cantidadUsada = new BigDecimal(row[1].toString());

                // Obtener el insumo de la BD
                Insumo insumo = insumoRepository.findById(insumoId).orElse(null);
                if (insumo != null) {
                    // Calcular cantidad total a descontar = cantidad_usada * cantidad_pedida
                    BigDecimal cantidadTotalADescontar = cantidadUsada.multiply(BigDecimal.valueOf(cantidadPedida));

                    // Restar del stock actual
                    BigDecimal nuevoStock = insumo.getStockActual().subtract(cantidadTotalADescontar);
                    insumo.setStockActual(nuevoStock);

                    // Guardar cambios
                    insumoRepository.save(insumo);
                }
            }
        }
    }

    // ==========================================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ==========================================

    private String normalizarTipo(String tipoPedido) {
        String tipo = tipoPedido == null ? "presencial" : tipoPedido.trim().toLowerCase();
        if (!"presencial".equals(tipo) && !"delivery".equals(tipo)) {
            throw new IllegalArgumentException("Tipo de pedido inválido.");
        }
        return tipo;
    }

    private String normalizarEstado(String estadoPedido) {
        String estado = estadoPedido == null ? "" : estadoPedido.trim().toLowerCase();
        List<String> estadosPermitidos = List.of(
                "nuevo",
                "en_preparacion",
                "listo",
                "entregado",
                "facturado",
                "cancelado");

        if (!estadosPermitidos.contains(estado)) {
            throw new IllegalArgumentException("Estado de pedido inválido.");
        }

        return estado;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getItems(Map<String, Object> request) {
        Object value = request.get("items");
        if (value instanceof List<?>) {
            return (List<Map<String, Object>>) value;
        }
        return Collections.emptyList();
    }

    private String getString(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private Integer getInteger(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return Integer.parseInt(String.valueOf(value).trim());
            }
        }
        return null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof Number number) {
                return BigDecimal.valueOf(number.doubleValue());
            }
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return new BigDecimal(String.valueOf(value).trim());
            }
        }
        return null;
    }

    private PedidoDashboardResponse mapPedidoDashboard(Pedido pedido) {
        List<PedidoDetalleResponse> detalles = pedido.getDetalles()
                .stream()
                .map((detalle) -> new PedidoDetalleResponse(
                        detalle.getProducto().getId(),
                        detalle.getProducto().getNombre(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getNotas(),
                        detalle.getId(),
                        detalle.getEstadoDetalle()))
                .toList();

        Mesa mesa = pedido.getMesa();
        return new PedidoDashboardResponse(
                pedido.getId(),
                pedido.getTipo(),
                pedido.getEstado(),
                pedido.getTotal(),
                pedido.getCreatedAt(),
                mesa != null ? mesa.getId() : null,
                mesa != null ? mesa.getCodigo() : null,
                pedido.getDireccionDelivery(),
                detalles);
    }
}