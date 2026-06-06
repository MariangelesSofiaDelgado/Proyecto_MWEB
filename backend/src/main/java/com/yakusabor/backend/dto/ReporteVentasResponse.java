package com.yakusabor.backend.dto;

import java.math.BigDecimal;

public class ReporteVentasResponse {
    private String fecha; // Cambiado a String o puedes usar Object para mayor flexibilidad
    private Long numeroPedidos;
    private BigDecimal ingresos;

    public ReporteVentasResponse(String fecha, Long numeroPedidos, BigDecimal ingresos) {
        this.fecha = fecha;
        this.numeroPedidos = numeroPedidos;
        this.ingresos = ingresos != null ? ingresos : BigDecimal.ZERO;
    }

    // Getters y Setters
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Long getNumeroPedidos() { return numeroPedidos; }
    public void setNumeroPedidos(Long numeroPedidos) { this.numeroPedidos = numeroPedidos; }
    public BigDecimal getIngresos() { return ingresos; }
    public void setIngresos(BigDecimal ingresos) { this.ingresos = ingresos; }
}