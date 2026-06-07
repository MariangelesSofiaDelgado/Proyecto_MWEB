package com.yakusabor.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.yakusabor.backend.models.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
}
