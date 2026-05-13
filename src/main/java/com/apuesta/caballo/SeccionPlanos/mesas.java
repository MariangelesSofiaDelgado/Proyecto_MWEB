package com.apuesta.caballo.SeccionPlanos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class mesas {

    // Define una ruta externa al JAR, basada en la carpeta de ejecución del programa
   private final String rutaArchivo = System.getProperty("user.dir") + File.separator + "mesas.txt";

    public String generarobject() { // Convención camelCase para métodos
        File archivoExterno = new File(rutaArchivo);

        if (archivoExterno.exists()) {
            return leerDesdeArchivo(archivoExterno);
        }

        // Lectura desde resources si no existe el archivo externo
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("mesas.txt")) {
            if (is == null) {
                return "Error: Archivo por defecto mesas.txt no encontrado en resources";
            }
            return leerDesdeStream(is);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String leerDesdeArchivo(File archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            return procesarBufferedReader(br);
        } catch (IOException e) {
            return "Error al leer archivo externo: " + e.getMessage();
        }
    }

    private String leerDesdeStream(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            return procesarBufferedReader(br);
        }
    }

    private String procesarBufferedReader(BufferedReader br) throws IOException {
        StringBuilder cadena = new StringBuilder();
        String linea;
        while ((linea = br.readLine()) != null) {
            cadena.append(linea); // Añadir .append("\n") si el archivo tiene múltiples líneas
        }
        return cadena.toString();
    }

    public void guardarEstadoMesas(int[] arreglo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arreglo.length; i++) {
            sb.append(arreglo[i]);
            if (i < arreglo.length - 1) {
                sb.append(".");
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            bw.write(sb.toString());
            System.out.println("Archivo actualizado en disco: " + sb.toString());
        } catch (IOException e) {
            System.err.println("Error al escribir: " + e.getMessage());
        }
    }
}