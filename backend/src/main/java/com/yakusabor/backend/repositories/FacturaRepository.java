package com.yakusabor.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.yakusabor.backend.models.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {
}
