package com.yakusabor.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "mesas")
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 20)
    private String ubicacion;

    @Column(nullable = false, length = 20)
    private String estado = "libre";

    // Mozo actualmente responsable de esta mesa.
    // null = nadie la está atendiendo todavía (mesa "libre para tomar").
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mesero_id")
    private Usuario mesero;
}
