package com.yakusabor.backend.controllers;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/estado")
    public List<MesaEstadoResponse> obtenerEstadoMesas() {
        return mesaRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Mesa::getId))
                .map((mesa) -> new MesaEstadoResponse(
                        mesa.getId(),
                        mesa.getCodigo(),
                        mesa.getUbicacion(),
                        mesa.getEstado(),
                        "libre".equalsIgnoreCase(mesa.getEstado())
                ))
                .toList();
    }
}
