package com.yakusabor.backend.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.yakusabor.backend.dto.AuthResponse;
import com.yakusabor.backend.dto.LoginRequest;
import com.yakusabor.backend.dto.RegistroRequest;
import com.yakusabor.backend.models.Rol;
import com.yakusabor.backend.models.Usuario;
import com.yakusabor.backend.repositories.RolRepository;
import com.yakusabor.backend.repositories.UsuarioRepository;
import com.yakusabor.backend.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    // ==========================================
    // ENDPOINT DE LOGIN
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        
        // 1. Buscamos al usuario por correo
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

        // Si el usuario no existe, responde 401 Unauthorized
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOpt.get();

        // 2. Verificamos si la contraseña coincide 
        // BCrypt compara el texto plano ingresado con el hash guardado en la BD
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        // 3. Preparamos la respuesta que espera el frontend (ej. tu login.js)
        String rolNombre = usuario.getRol().getNombre(); // Devuelve 'Cliente', 'Administrador', etc.
        
        // Generamos el token JWT real
        String token = jwtUtil.generarToken(usuario.getEmail(), rolNombre);

        return ResponseEntity.ok(new AuthResponse(token, usuario.getNombre(), rolNombre)); // Responde 200 OK
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
            // Ahora guardamos el hash con BCrypt, nunca el texto plano
            nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));

            // 3. Buscamos el rol "Cliente" por nombre, para evitar errores de ID
            Optional<Rol> rolCliente = rolRepository.findByNombre("Cliente");
            if (rolCliente.isPresent()) {
                nuevoUsuario.setRol(rolCliente.get());
                
                // Guardamos en la base de datos
                usuarioRepository.save(nuevoUsuario);
                return ResponseEntity.ok("Usuario registrado exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error de configuración de roles en DB");
            }
        } catch (Exception e) {
            // Esto atrapará cualquier otro error e imprimirá el motivo real en la consola de Spring Boot
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al registrar: " + e.getMessage());
        }
    }
}