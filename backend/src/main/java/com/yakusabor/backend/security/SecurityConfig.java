package com.yakusabor.backend.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Habilitamos CORS usando la configuración de abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Deshabilita CSRF ya que usaremos JWT y el frontend corre en otro puerto/origen
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configura temporalmente para permitir todas las peticiones
                // Esto quita la pantalla de login por defecto de Spring para poder crear el registro
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        // Se construye y retorna una sola vez al final
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite que cualquier frontend (Live Server, archivos locales, Angular/React) se conecte
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}