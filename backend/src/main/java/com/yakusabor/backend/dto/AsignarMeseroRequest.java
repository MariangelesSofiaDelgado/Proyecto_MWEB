package com.yakusabor.backend.dto;

import lombok.Data;

@Data
public class AsignarMeseroRequest {
    // Id del mozo a asignar. Si viene null, la mesa queda sin mozo asignado.
    private Integer meseroId;
}
