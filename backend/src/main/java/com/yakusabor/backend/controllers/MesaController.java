package com.yakusabor.backend.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    // ── GET /api/mesas/estado ─────────────────────────────────────────────────
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

    // ── POST /api/mesas ───────────────────────────────────────────────────────
    // Crea una nueva mesa. Body: { "codigo": "A1", "ubicacion": "interior" }
    @PostMapping
    @Transactional
    public ResponseEntity<?> crearMesa(@RequestBody Map<String, String> body) {
        String codigo    = body.getOrDefault("codigo",    "").trim();
        String ubicacion = body.getOrDefault("ubicacion", "interior").trim();

        if (codigo.isEmpty()) {
            return ResponseEntity.badRequest().body("El campo 'codigo' es obligatorio.");
        }

        // Verificar que no exista ya una mesa con ese código
        boolean existe = mesaRepository.findAll()
                .stream()
                .anyMatch(m -> m.getCodigo().equalsIgnoreCase(codigo));
        if (existe) {
            return ResponseEntity.badRequest()
                    .body("Ya existe una mesa con el código: " + codigo);
        }

        Mesa nueva = new Mesa();
        nueva.setCodigo(codigo.toUpperCase());
        nueva.setUbicacion(ubicacion.toLowerCase());
        nueva.setEstado("libre");

        Mesa guardada = mesaRepository.save(nueva);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MesaEstadoResponse(
                guardada.getId(),
                guardada.getCodigo(),
                guardada.getUbicacion(),
                guardada.getEstado(),
                true
        ));
    }

    // ── PUT /api/mesas/{id}/estado ────────────────────────────────────────────
    // Cambia el estado de una mesa. Body: { "estado": "libre|ocupada|reservada|fuera_de_servicio" }
    @PutMapping("/{id}/estado")
    @Transactional
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        Optional<Mesa> opt = mesaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String nuevoEstado = body.getOrDefault("estado", "").trim().toLowerCase();

        List<String> estadosValidos = List.of("libre", "ocupada", "reservada", "fuera_servicio");
        if (!estadosValidos.contains(nuevoEstado)) {
            return ResponseEntity.badRequest()
                    .body("Estado inválido. Permitidos: " + estadosValidos);
        }

        Mesa mesa = opt.get();
        mesa.setEstado(nuevoEstado);
        Mesa guardada = mesaRepository.save(mesa);

        return ResponseEntity.ok(new MesaEstadoResponse(
                guardada.getId(),
                guardada.getCodigo(),
                guardada.getUbicacion(),
                guardada.getEstado(),
                "libre".equalsIgnoreCase(guardada.getEstado())
        ));
    }

    // ── DELETE /api/mesas/{id} ────────────────────────────────────────────────
    // Elimina una mesa por su ID.
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> eliminarMesa(@PathVariable Integer id) {
        if (!mesaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        mesaRepository.deleteById(id);
        return ResponseEntity.ok("Mesa eliminada.");
    }
}