package com.yakusabor.backend.controllers;

import com.yakusabor.backend.repositories.PedidoRepository;
import com.yakusabor.backend.dto.ReporteVentasResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
public class ReporteController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping("/ventas-diarias")
    public ResponseEntity<List<ReporteVentasResponse>> getVentasDiarias() {
        List<Object[]> resultados = pedidoRepository.obtenerVentasDiariasRaw();
        List<ReporteVentasResponse> reporte = new ArrayList<>();

        for (Object[] fila : resultados) {
            // Evaluamos y convertimos cada posición del Object[] con seguridad
            String fecha = fila[0] != null ? fila[0].toString() : "Sin fecha";
            Long numeroPedidos = fila[1] != null ? ((Number) fila[1]).longValue() : 0L;
            
            BigDecimal ingresos = BigDecimal.ZERO;
            if (fila[2] != null) {
                if (fila[2] instanceof BigDecimal) {
                    ingresos = (BigDecimal) fila[2];
                } else {
                    ingresos = new BigDecimal(fila[2].toString());
                }
            }

            reporte.add(new ReporteVentasResponse(fecha, numeroPedidos, ingresos));
        }

        return ResponseEntity.ok(reporte);
    }
}