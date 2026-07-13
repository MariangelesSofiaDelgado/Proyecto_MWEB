// MesaController.java
package com.yakusabor.backend.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yakusabor.backend.dto.AsignarMeseroRequest;
import com.yakusabor.backend.dto.MesaEstadoResponse;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.services.AuthService;
import com.yakusabor.backend.services.MesaService;
import com.yakusabor.backend.services.PedidoService;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    @Autowired private MesaService mesaService;
    @Autowired private PedidoService pedidoService;
    @Autowired private AuthService authService;

    @GetMapping("/{id}/cuenta")
public ResponseEntity<?> obtenerCuenta(@PathVariable Integer id) {
    try {
        return ResponseEntity.ok(pedidoService.obtenerCuentaMesa(id));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

    // Historial completo de pedidos de la mesa (cualquier estado) — para "ver los pedidos que se hizo en esa mesa"
    @GetMapping("/{id}/pedidos")
    public ResponseEntity<?> obtenerPedidosDeMesa(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(pedidoService.listarPedidosPorMesa(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/estado")
    public List<MesaEstadoResponse> obtenerEstadoMesas() {
        return mesaService.obtenerEstadoMesas();
    }

    @PostMapping
    public ResponseEntity<?> crearMesa(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(mesaService.crearMesa(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(mesaService.actualizarEstado(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin asigna un mozo a una mesa, o un mozo elige/libera la mesa que atiende.
    // Body: { "meseroId": 3 }  ó  { "meseroId": null } para liberar.
    @PutMapping("/{id}/asignar")
    public ResponseEntity<?> asignarMesero(@PathVariable Integer id,
                                            @RequestBody AsignarMeseroRequest body,
                                            Principal principal) {
        try {
            Usuario actor = authService.obtenerUsuarioActual(principal);
            return ResponseEntity.ok(mesaService.asignarMesero(id, body.getMeseroId(), actor));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMesa(@PathVariable Integer id) {
        try {
            mesaService.eliminarMesa(id);
            return ResponseEntity.ok("Mesa eliminada.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
