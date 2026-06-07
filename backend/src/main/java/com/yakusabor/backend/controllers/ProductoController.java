package com.yakusabor.backend.controllers;
 
import java.util.List;
import java.util.Map;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.yakusabor.backend.models.Categoria;
import com.yakusabor.backend.models.Producto;
import com.yakusabor.backend.repositories.ProductoRepository;
 
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
 
    @Autowired
    private ProductoRepository productoRepository;
 
    // Repositorio de categorías necesario para crear/editar productos
    @Autowired
    private com.yakusabor.backend.repositories.CategoriaRepository categoriaRepository;
 
    // ── GET todos ─────────────────────────────────────
    @GetMapping
    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }
 
    // ── GET uno ───────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Integer id) {
        return productoRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
 
    // ── POST crear ────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Map<String, Object> body) {
        try {
            Producto p = buildProducto(new Producto(), body);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoRepository.save(p));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno: " + e.getMessage());
        }
    }
 
    // ── PUT editar ────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> editarProducto(@PathVariable Integer id,
                                             @RequestBody Map<String, Object> body) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        try {
            Producto p = buildProducto(opt.get(), body);
            return ResponseEntity.ok(productoRepository.save(p));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno: " + e.getMessage());
        }
    }
 
    // ── PUT disponibilidad (usado por GestionCocina) ──
    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<?> actualizarDisponibilidad(@PathVariable Integer id,
                                                       @RequestBody Map<String, Object> body) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
 
        Object val = body.get("disponible");
        if (val == null) return ResponseEntity.badRequest().body("Falta el campo 'disponible'.");
 
        Producto p = opt.get();
        p.setDisponible(Boolean.parseBoolean(String.valueOf(val)));
        return ResponseEntity.ok(productoRepository.save(p));
    }
 
    // ── DELETE ────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        if (!productoRepository.existsById(id)) return ResponseEntity.notFound().build();
        productoRepository.deleteById(id);
        return ResponseEntity.ok("Producto eliminado.");
    }
 
    // ── Helper: construye/actualiza un Producto desde el body del request ──
    private Producto buildProducto(Producto p, Map<String, Object> body) {
        String nombre = getString(body, "nombre");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
 
        Object precioRaw = body.get("precio");
        if (precioRaw == null) throw new IllegalArgumentException("El precio es obligatorio.");
        double precio;
        try { precio = Double.parseDouble(String.valueOf(precioRaw)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Precio inválido."); }
        if (precio < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
 
        String catNombre = getString(body, "categoriaNombre");
        if (catNombre == null || catNombre.isBlank()) throw new IllegalArgumentException("La categoría es obligatoria.");
 
        Categoria cat = categoriaRepository.findByNombre(catNombre)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + catNombre));
 
        p.setNombre(nombre.trim());
        p.setDescripcion(getString(body, "descripcion"));
        p.setPrecio(precio);
        p.setCategoria(cat);
 
        // Disponible: true por defecto al crear; se conserva el valor actual al editar
        if (body.containsKey("disponible")) {
            p.setDisponible(Boolean.parseBoolean(String.valueOf(body.get("disponible"))));
        } else if (p.getDisponible() == null) {
            p.setDisponible(true);
        }
 
        return p;
    }
 
    private String getString(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }
}