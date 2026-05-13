package com.apuesta.caballo.CargaDeLaBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.apuesta.caballo.Objetos.cocina;
import com.apuesta.caballo.listadeobjetos.cargarobjetococina;

public class generarcocina implements generaldb<cargarobjetococina> {

    @Override
    public cargarobjetococina Entregar() {
        cargarobjetococina cos = new cargarobjetococina();
        int contador = 0;
        String sql = "select * from cocina";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql);) {
            while (rs.next()) {
                cocina cosinita = new cocina();
                cosinita.setIdpedido(rs.getInt("idpedido"));
                cosinita.setIdplato(rs.getInt("Idplato"));
                cosinita.setNombredeplato(rs.getString("nombreplato"));
                cosinita.setMesaalaquepertenece(rs.getInt("mesaalaquepertenece"));
                cosinita.setExtras(rs.getString("extra"));
                cosinita.setIdmesero(rs.getInt("idmesero"));
                cos.agregar(cosinita);
            }

        } catch (SQLException e) {
            System.out.println("Error al carar: " + e.getMessage());
        }
        return cos;

    }

    @Override
    public void encargar(cargarobjetococina cost) {
        List<cocina> lista = cost.getlista();
        String sql = "INSERT INTO mesero (idpedido, Idplato, nombreplato, mesaalaquepertenece, extra, idmesero) VALUES (?, ?, ? , ? , ? , ?)";

        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql)) {
            for (cocina cos : lista) {
                p.setInt(1, cos.getIdpedido());
                p.setInt(2, cos.getIdplato());
                p.setString(3, cos.getNombredeplato());
                p.setInt(4, cos.getMesaalaquepertenece());
                p.setString(5, cos.getExtras());
                p.setInt(6, cos.getIdmesero());
                p.addBatch();//agregar al paquete
            }
            p.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void refactor(cargarobjetococina cost) {
      List<cocina> lista = cost.getlista();
        String sql = "update cocina set Idplato = ?, nombreplato = ? , mesaalaquepertenece = ? , extra = ?, idmesero = ? where idpedido = ?";

        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql)) {
            for (cocina cos : lista) {
                
                p.setInt(1, cos.getIdplato());
                p.setString(2, cos.getNombredeplato());
                p.setInt(3, cos.getMesaalaquepertenece());
                p.setString(4, cos.getExtras());
                p.setInt(5, cos.getIdmesero());
                p.setInt(6, cos.getIdpedido());
                p.addBatch();//agregar al paquete
            }
            p.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
                 }

}
