package com.yakusabor.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakusabor.backend.models.Producto;
import com.yakusabor.backend.repositories.ProductoRepository;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public List<Producto> obtenerProductos() {
        // Devuelve todos los productos de la BD en formato JSON
        return productoRepository.findAll();
    }
}