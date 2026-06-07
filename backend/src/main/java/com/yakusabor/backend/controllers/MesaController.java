package com.yakusabor.backend.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakusabor.backend.dto.MesaEstadoResponse;
import com.yakusabor.backend.models.Mesa;
import com.yakusabor.backend.repositories.MesaRepository;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    @Autowired
    private MesaRepository mesaRepository;

    // ── GET /api/mesas/estado ──────────────────────────────────────────────────
    @GetMapping("/estado")
    public List<MesaEstadoResponse> obtenerEstadoMesas() {
        return mesaRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Mesa::getId))
                .map(mesa -> new MesaEstadoResponse(
                        mesa.getId(),
                        mesa.getCodigo(),
                        mesa.getUbicacion(),
                        mesa.getEstado(),
                        "libre".equalsIgnoreCase(mesa.getEstado())
                ))
                .toList();
    }

    // ── PUT /api/mesas/{id}/estado ─────────────────────────────────────────────
    // Permite actualizar el estado de una mesa desde el frontend de Gestión de Sala.
    // Estados válidos: libre | ocupada | reservada | fuera_de_servicio
    @PutMapping("/{id}/estado")
    @Transactional
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        Optional<Mesa> opt = mesaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            return ResponseEntity.badRequest().body("El campo 'estado' es obligatorio.");
        }

        // Normalizar entrada
        nuevoEstado = nuevoEstado.trim().toLowerCase();

        List<String> estadosValidos = List.of("libre", "ocupada", "reservada", "fuera_de_servicio");
        if (!estadosValidos.contains(nuevoEstado)) {
            return ResponseEntity.badRequest()
                    .body("Estado inválido. Valores permitidos: " + estadosValidos);
        }

        Mesa mesa = opt.get();
        mesa.setEstado(nuevoEstado);
        Mesa mesaActualizada = mesaRepository.save(mesa);

        return ResponseEntity.ok(new MesaEstadoResponse(
                mesaActualizada.getId(),
                mesaActualizada.getCodigo(),
                mesaActualizada.getUbicacion(),
                mesaActualizada.getEstado(),
                "libre".equalsIgnoreCase(mesaActualizada.getEstado())
        ));
    }
}