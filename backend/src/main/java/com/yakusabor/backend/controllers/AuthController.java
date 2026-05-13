package com.yakusabor.backend.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yakusabor.backend.dto.AuthResponse;
import com.yakusabor.backend.dto.LoginRequest;
import com.yakusabor.backend.dto.RegistroRequest;
import com.yakusabor.backend.models.Rol;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.repositories.RolRepository;
import com.yakusabor.backend.repositories.UsuarioRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    // ==========================================
    // ENDPOINT DE LOGIN
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. Buscamos al usuario por correo
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

        // 2. Verificamos si existe y si la contraseña coincide 
        // (Nota: En producción debes usar BCrypt. Por ahora, para probar rápido, comparamos texto plano)
        if (usuarioOpt.isPresent() && usuarioOpt.get().getPassword().equals(request.getPassword())) {

            Usuario usuario = usuarioOpt.get();

            // 3. Preparamos la respuesta que espera tu login.js
            // Como aún no tenemos JWT configurado, enviaremos un token simulado
            AuthResponse respuesta = new AuthResponse(
                    "token_simulado_12345",
                    usuario.getNombre(),
                    usuario.getRol().getNombre() // Devuelve 'Cliente', 'Administrador', etc.
            );

            return ResponseEntity.ok(respuesta); // Responde 200 OK
        }

        // Si la contraseña está mal o el usuario no existe, responde 401 Unauthorized
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
    }

    // ==========================================
    // ENDPOINT DE REGISTRO
    // ==========================================
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest request) {

        try {
            // 1. Verificamos que el correo no esté ocupado
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("El correo ya está en uso");
            }

            // 2. Creamos el nuevo usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(request.getNombre());
            nuevoUsuario.setEmail(request.getEmail());
            nuevoUsuario.setPassword(request.getPassword()); // En producción, hashear con BCrypt

            // 3. Buscamos el rol "Cliente" por nombre, para evitar errores de ID
            Optional<Rol> rolCliente = rolRepository.findByNombre("Cliente");
            if (rolCliente.isPresent()) {
                nuevoUsuario.setRol(rolCliente.get());

                // Guardamos en la base de datos
                usuarioRepository.save(nuevoUsuario);
                return ResponseEntity.ok("Usuario registrado exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error de configuración de roles en DB");
            }
        } catch (Exception e) {
            // Esto atrapará cualquier otro error e imprimirá el motivo real en la consola de Spring Boot
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar: " + e.getMessage());
        }

    }
}