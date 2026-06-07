package com.yakusabor.backend.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakusabor.backend.models.Factura;
import com.yakusabor.backend.models.Pedido;
import com.yakusabor.backend.repositories.FacturaRepository;
import com.yakusabor.backend.repositories.PedidoRepository;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping
    public List<Factura> listar() {
        return facturaRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Integer pedidoId = body.get("pedidoId") instanceof Number ? ((Number) body.get("pedidoId")).intValue() : null;
            if (pedidoId == null) return ResponseEntity.badRequest().body("pedidoId es obligatorio");

            Pedido pedido = pedidoRepository.findById(pedidoId).orElse(null);
            if (pedido == null) return ResponseEntity.badRequest().body("Pedido no encontrado");

            Factura f = new Factura();
            f.setPedido(pedido);
            f.setTotal(pedido.getTotal());

            return ResponseEntity.status(HttpStatus.CREATED).body(facturaRepository.save(f));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear factura: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Integer id) {
        return facturaRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
