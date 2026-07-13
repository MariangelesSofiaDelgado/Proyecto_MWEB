package com.yakusabor.backend.controllers;

import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.services.AuthService;
import com.yakusabor.backend.services.PedidoService;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired private PedidoService pedidoService;
    @Autowired private AuthService authService;

    @PostMapping
    public ResponseEntity<?> generarFactura(@RequestBody Map<String, Object> body, Principal principal) {
        try {
            Integer mesaId = Integer.parseInt(String.valueOf(body.get("mesaId")));
            Usuario actor = authService.obtenerUsuarioActual(principal);
            return ResponseEntity.ok(pedidoService.generarFactura(mesaId, actor));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }
}
