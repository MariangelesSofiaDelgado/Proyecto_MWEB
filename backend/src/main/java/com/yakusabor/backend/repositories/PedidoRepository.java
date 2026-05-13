package com.yakusabor.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yakusabor.backend.models.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
}
