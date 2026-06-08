package com.yakusabor.backend.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.yakusabor.backend.models.Rol;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.repositories.PedidoRepository;
import com.yakusabor.backend.repositories.RolRepository;
import com.yakusabor.backend.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired private UsuarioRepository  usuarioRepository;
    @Autowired private RolRepository      rolRepository;
    @Autowired private PedidoRepository   pedidoRepository;
    @Autowired private PasswordEncoder    passwordEncoder;

    // ══════════════════════════════════════════════════════
    //  GET /api/usuarios/meseros
    //  Devuelve todos los usuarios con rol "Mesero" más
    //  sus estadísticas de ventas calculadas desde pedidos.
    // ══════════════════════════════════════════════════════
    @GetMapping("/meseros")
    public ResponseEntity<?> listarMeseros() {
        try {
            // Obtener rol Mesero
            Optional<Rol> rolOpt = rolRepository.findByNombre("Mesero");
            if (rolOpt.isEmpty()) {
                return ResponseEntity.ok(List.of()); // sin rol Mesero registrado aún
            }

            Rol rolMesero = rolOpt.get();

            // Todos los usuarios con ese rol
            List<Usuario> meseros = usuarioRepository.findAll()
                    .stream()
                    .filter(u -> u.getRol() != null && u.getRol().getId().equals(rolMesero.getId()))
                    .collect(Collectors.toList());

            // Todos los pedidos para calcular ventas por mesero
            var pedidos = pedidoRepository.findAll();

            // Construir respuesta enriquecida
            List<Map<String, Object>> resultado = meseros.stream().map(m -> {
                // Pedidos que atendió este mesero (no cancelados)
                var pedidosMesero = pedidos.stream()
                        .filter(p -> p.getMesero() != null
                                && p.getMesero().getId().equals(m.getId())
                                && !"cancelado".equalsIgnoreCase(p.getEstado()))
                        .collect(Collectors.toList());

                // Ventas totales de hoy
                var hoy = java.time.LocalDate.now();
                double ventasHoy = pedidosMesero.stream()
                        .filter(p -> p.getCreatedAt() != null
                                && p.getCreatedAt().toLocalDate().equals(hoy))
                        .mapToDouble(p -> p.getTotal() != null ? p.getTotal().doubleValue() : 0)
                        .sum();

                // Mesas atendidas hoy (IDs únicos de mesa)
                long mesasHoy = pedidosMesero.stream()
                        .filter(p -> p.getCreatedAt() != null
                                && p.getCreatedAt().toLocalDate().equals(hoy)
                                && p.getMesa() != null)
                        .map(p -> p.getMesa().getId())
                        .distinct()
                        .count();

                // Promedio general (todas las ventas / número de pedidos)
                double totalGeneral = pedidosMesero.stream()
                        .mapToDouble(p -> p.getTotal() != null ? p.getTotal().doubleValue() : 0)
                        .sum();
                double promedio = pedidosMesero.isEmpty() ? 0
                        : totalGeneral / pedidosMesero.size();

                return Map.<String, Object>of(
                        "id",         m.getId(),
                        "nombre",     m.getNombre(),
                        "email",      m.getEmail(),
                        "activo",     m.getActivo() != null ? m.getActivo() : true,
                        "turno",      "—",           // sin campo turno en el modelo actual
                        "ventasDia",  ventasHoy,
                        "mesas",      (int) mesasHoy,
                        "promedio",   promedio
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar meseros: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  POST /api/usuarios/meseros
    //  Crea un usuario con rol Mesero.
    //  Body: { "nombre": "...", "email": "...", "password": "..." }
    // ══════════════════════════════════════════════════════
    @PostMapping("/meseros")
    @Transactional
    public ResponseEntity<?> crearMesero(@RequestBody Map<String, String> body) {
        try {
            String nombre   = body.getOrDefault("nombre",   "").trim();
            String email    = body.getOrDefault("email",    "").trim();
            String password = body.getOrDefault("password", "").trim();

            if (nombre.isEmpty())   return ResponseEntity.badRequest().body("El nombre es obligatorio.");
            if (email.isEmpty())    return ResponseEntity.badRequest().body("El correo es obligatorio.");
            if (password.isEmpty()) return ResponseEntity.badRequest().body("La contraseña es obligatoria.");

            if (usuarioRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("El correo ya está en uso.");
            }

            Optional<Rol> rolOpt = rolRepository.findByNombre("Mesero");
            if (rolOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El rol 'Mesero' no existe en la base de datos.");
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setEmail(email);
            nuevo.setPassword(passwordEncoder.encode(password));
            nuevo.setRol(rolOpt.get());
            nuevo.setActivo(true);

            Usuario guardado = usuarioRepository.save(nuevo);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id",      guardado.getId(),
                    "nombre",  guardado.getNombre(),
                    "email",   guardado.getEmail(),
                    "activo",  true,
                    "turno",   "—",
                    "ventasDia", 0,
                    "mesas",   0,
                    "promedio", 0
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear mesero: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  DELETE /api/usuarios/meseros/{id}
    //  Elimina un mesero por su ID (solo si es rol Mesero).
    // ══════════════════════════════════════════════════════
    @DeleteMapping("/meseros/{id}")
    @Transactional
    public ResponseEntity<?> eliminarMesero(@PathVariable Integer id) {
        try {
            Optional<Usuario> opt = usuarioRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Usuario u = opt.get();
            String rolNombre = u.getRol() != null ? u.getRol().getNombre() : "";
            if (!"Mesero".equalsIgnoreCase(rolNombre)) {
                return ResponseEntity.badRequest().body("Solo se pueden eliminar usuarios con rol Mesero.");
            }

            // Desvincular pedidos: poner mesero_id = null para no perder el historial
            pedidoRepository.findAll().stream()
                    .filter(p -> p.getMesero() != null && p.getMesero().getId().equals(id))
                    .forEach(p -> { p.setMesero(null); pedidoRepository.save(p); });

            usuarioRepository.deleteById(id);
            return ResponseEntity.ok("Mesero eliminado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar mesero: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  PUT /api/usuarios/meseros/{id}/activo
    //  Activa o desactiva un mesero.
    //  Body: { "activo": true/false }
    // ══════════════════════════════════════════════════════
    @PutMapping("/meseros/{id}/activo")
    @Transactional
    public ResponseEntity<?> toggleActivo(@PathVariable Integer id,
                                          @RequestBody Map<String, Object> body) {
        try {
            Optional<Usuario> opt = usuarioRepository.findById(id);
            if (opt.isEmpty()) return ResponseEntity.notFound().build();

            Usuario u = opt.get();
            Object val = body.get("activo");
            if (val == null) return ResponseEntity.badRequest().body("Falta el campo 'activo'.");

            u.setActivo(Boolean.parseBoolean(String.valueOf(val)));
            usuarioRepository.save(u);

            return ResponseEntity.ok(Map.of("id", id, "activo", u.getActivo()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}