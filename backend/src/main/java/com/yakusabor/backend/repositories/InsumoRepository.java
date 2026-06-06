package com.yakusabor.backend.repositories;

import com.yakusabor.backend.models.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Integer> {
}