package com.yakusabor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MesaEstadoResponse {
    private Integer id;
    private String codigo;
    private String ubicacion;
    private String estado;
    private Boolean libre;

    // Mozo actualmente asignado a la mesa (null si nadie la está atendiendo)
    private Integer meseroId;
    private String meseroNombre;
}
