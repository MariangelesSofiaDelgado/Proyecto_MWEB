package com.yakusabor.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yakusabor.backend.models.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Spring Data JPA ya incluye el método findAll() por defecto

    // Consulta nativa para obtener los insumos vinculados a un producto
    // Retorna: [insumo_id, cantidad_usada]
    @Query(
        value = "SELECT insumo_id, cantidad_usada FROM producto_insumo WHERE producto_id = :productoId",
        nativeQuery = true
    )
    List<Object[]> findInsumosAsociadosByProductoId(@Param("productoId") Integer productoId);
}