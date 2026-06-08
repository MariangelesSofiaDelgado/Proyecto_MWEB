package com.yakusabor.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    public AuthResponse(String token2, String nombre2, String rolNombre, Integer id) {
        this.token = token2;
        this.nombre = nombre2;
        this.rol = rolNombre;
    }
    private String token;
    private String nombre;
    private String rol;
}