package com.apuesta.caballo.ConexionaDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class probarconexion {
    private static final String USER = "root"; // Usuario por defecto en XAMPP
    private static final String PASS = "";     // Contraseña por defecto (vacía)
        private static final String URL = "jdbc:mysql://localhost:3306/roma";
    
    
    public static Connection conectar() {
        Connection conexion = null;
        try {
            // Cargar el driver JDBC 
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            
            // Establecer la conexión
            conexion = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("¡Conexión exitosa a MySQL!");
            
        } catch (ClassNotFoundException e) {
            System.out.println("Error: No se encontró el driver JDBC.");
        } catch (SQLException e) {
            System.out.println("Error al conectar: " + e.getMessage());
        }
        return conexion;
    }

 public static void main(String[] args) {
        conectar();
    }
    

}
