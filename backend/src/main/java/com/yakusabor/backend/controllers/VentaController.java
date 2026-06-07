package com.yakusabor.backend.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakusabor.backend.dto.VentaMeseroResponse;
import com.yakusabor.backend.models.Venta;
import com.yakusabor.backend.repositories.VentaRepository;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @GetMapping("/por-mesero")
    public List<VentaMeseroResponse> reporteVentasPorMesero() {
        var ventas = ventaRepository.findAll();

        var resumen = ventas.stream()
                .filter(v -> v.getMesero() != null)
                .collect(Collectors.groupingBy(
                        v -> new MeseroTurnoKey(v.getMesero().getId(), v.getMesero().getNombre(), v.getTurno()),
                        Collectors.counting()));

        return resumen.entrySet().stream()
                .map(entry -> new VentaMeseroResponse(
                        entry.getKey().meseroId(),
                        entry.getKey().meseroNombre(),
                        entry.getKey().turno(),
                        entry.getValue()))
                .sorted(Comparator.comparing(VentaMeseroResponse::meseroNombre).thenComparing(VentaMeseroResponse::turno))
                .toList();
    }

    private record MeseroTurnoKey(Integer meseroId, String meseroNombre, String turno) {}
}
