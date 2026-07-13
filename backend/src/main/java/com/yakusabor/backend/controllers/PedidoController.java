// PedidoController.java
package com.yakusabor.backend.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yakusabor.backend.dto.ActualizarEstadoPedidoRequest;
import com.yakusabor.backend.dto.PedidoDashboardResponse;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.services.AuthService;
import com.yakusabor.backend.services.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired private PedidoService pedidoService;
    @Autowired private AuthService authService;

    @GetMapping
    public List<PedidoDashboardResponse> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<?> listarPedidosPorMesa(@PathVariable Integer mesaId) {
        try {
            return ResponseEntity.ok(pedidoService.listarPedidosPorMesa(mesaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody Map<String, Object> request, Principal principal) {
        try {
            Usuario actor = resolverActorOpcional(principal);
            return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request, actor));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar el pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id,
                                               @RequestBody ActualizarEstadoPedidoRequest request,
                                               Principal principal) {
        try {
            Usuario actor = resolverActorOpcional(principal);
            return ResponseEntity.ok(pedidoService.actualizarEstado(id, request.getEstado(), actor));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al actualizar el pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{pedidoId}/detalles/{detalleId}/estado")
    public ResponseEntity<?> actualizarEstadoDetalle(@PathVariable Integer pedidoId,
                                                       @PathVariable Integer detalleId,
                                                       @RequestBody ActualizarEstadoPedidoRequest request) {
        try {
            return ResponseEntity.ok(pedidoService.actualizarEstadoDetalle(pedidoId, detalleId, request.getEstado()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    private Usuario resolverActorOpcional(Principal principal) {
        if (principal == null || principal.getName() == null) return null;
        try {
            return authService.obtenerUsuarioActual(principal);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
