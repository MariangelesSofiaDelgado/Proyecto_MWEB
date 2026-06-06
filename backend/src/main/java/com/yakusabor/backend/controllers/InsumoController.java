package com.yakusabor.backend.controllers;

import com.yakusabor.backend.models.Insumo;
import com.yakusabor.backend.repositories.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/insumos")
public class InsumoController {

    @Autowired
    private InsumoRepository insumoRepository;

    @GetMapping
    public List<Insumo> listarInsumos() {
        return insumoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Insumo> crearInsumo(@RequestBody Insumo insumo) {
        if (insumo.getStockActual() == null || insumo.getStockMinimo() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(insumoRepository.save(insumo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Insumo> actualizarInsumo(@PathVariable Integer id, @RequestBody Insumo datosActualizados) {
        return insumoRepository.findById(id).map(insumo -> {
            insumo.setNombre(datosActualizados.getNombre());
            insumo.setStockActual(datosActualizados.getStockActual());
            insumo.setStockMinimo(datosActualizados.getStockMinimo());
            insumo.setUnidadMedida(datosActualizados.getUnidadMedida());
            return ResponseEntity.ok(insumoRepository.save(insumo));
        }).orElse(ResponseEntity.notFound().build());
    }
}