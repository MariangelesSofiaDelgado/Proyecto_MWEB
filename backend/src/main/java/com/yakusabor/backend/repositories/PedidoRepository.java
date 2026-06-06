package com.yakusabor.backend.repositories;

import com.yakusabor.backend.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("SELECT FUNCTION('DATE', p.createdAt), COUNT(p), SUM(p.total) " +
           "FROM Pedido p " +
           "WHERE p.estado = 'facturado' " +
           "GROUP BY FUNCTION('DATE', p.createdAt) " +
           "ORDER BY FUNCTION('DATE', p.createdAt) DESC")
    List<Object[]> obtenerVentasDiariasRaw();
}