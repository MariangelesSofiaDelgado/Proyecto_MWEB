package com.yakusabor.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakusabor.backend.dto.ReservaRequest;
import com.yakusabor.backend.dto.ReservaResponse;
import com.yakusabor.backend.models.Mesa;
import com.yakusabor.backend.repositories.MesaRepository;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private MesaRepository mesaRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> reservarMesa(@RequestBody ReservaRequest request) {
        if (request.getMesaId() == null) {
            return ResponseEntity.badRequest().body("Debes seleccionar una mesa.");
        }

        Mesa mesa = mesaRepository.findById(request.getMesaId()).orElse(null);
        if (mesa == null) {
            return ResponseEntity.badRequest().body("La mesa seleccionada no existe.");
        }

        if (!"libre".equalsIgnoreCase(mesa.getEstado())) {
            return ResponseEntity.badRequest().body("La mesa " + mesa.getCodigo() + " no está disponible.");
        }

        mesa.setEstado("reservada");
        Mesa mesaReservada = mesaRepository.save(mesa);

        return ResponseEntity.ok(new ReservaResponse(
                mesaReservada.getId(),
                mesaReservada.getCodigo(),
                mesaReservada.getEstado(),
                "Reserva confirmada correctamente."
        ));
    }
}
