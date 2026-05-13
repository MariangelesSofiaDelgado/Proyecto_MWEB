package com.apuesta.caballo.CargaDeLaBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.apuesta.caballo.Objetos.mesero;
import com.apuesta.caballo.listadeobjetos.cargarobjetomesero;

public class generarmesero implements generaldb<cargarobjetomesero> {

    @Override
    public cargarobjetomesero Entregar() {
        cargarobjetomesero mse = new cargarobjetomesero();
        int contador = 0;
        String sql = "select * from mesero";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql);) {
            while (rs.next()) {
                mesero el = new mesero();
                el.setIdmesero(rs.getInt("idmesero"));
                el.setNombre(rs.getString("nombre"));
                mse.agregar(el);
            }

        } catch (SQLException e) {
            System.out.println("Error al carar: " + e.getMessage());
        }
        return mse;
    }

    @Override
    public void encargar(cargarobjetomesero cost) {
        List<mesero> lista = cost.getlista();
        String sql = "INSERT INTO mesero (idmesero, nombre) VALUES (?, ?)";

        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql)) {
            for (mesero mes : lista) {
                p.setInt(1, mes.getIdmesero());
                p.setString(2, mes.getNombre());

                p.addBatch();//agregar al paquete
            }

            p.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());

        }
    }

    @Override
    public void refactor(cargarobjetomesero cost) {
        List<mesero> lista = cost.getlista();
        String sql = "update mesero set nombre= ? where idmesero = ?";

        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql)) {
            for (mesero mes : lista) {
                p.setString(1, mes.getNombre());
                p.setInt(2, mes.getIdmesero());

                p.addBatch();//agregar al paquete
            }

            p.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());

        }

    }

}
