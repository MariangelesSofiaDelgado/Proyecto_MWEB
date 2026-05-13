package com.apuesta.caballo.CargaDeLaBD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.apuesta.caballo.Objetos.estado;
import com.apuesta.caballo.listadeobjetos.cargaobjetoestado;

public class generarestado implements generaldb<cargaobjetoestado> {

    @Override
    public cargaobjetoestado Entregar() {
        cargaobjetoestado crg = new cargaobjetoestado();
        int contador = 0;
        String sql = "select * from estado";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql);) {
            while (rs.next()) {
                estado e = new estado();
                e.setFecha(rs.getNString("fecha"));
                e.setIdestado(rs.getInt("idestado"));
                e.setIdpedido(rs.getInt("idpedido"));
                crg.agregar(e);
            }

        } catch (SQLException e) {
            System.out.println("Error al carar: " + e.getMessage());
        }
        return crg;
    }

    @Override
    public void encargar(cargaobjetoestado cost) {

        List<estado> lista = cost.getlista();
        String sql = "INSERT INTO estado ( fecha, idpedido) VALUES (?, ?)";

        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql)) {
            for (estado est : lista) {
               
                p.setString(2, est.getFecha());
                p.setInt(3, est.getIdpedido());
                p.addBatch();//agregar al paquete
            }

            p.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());

        }

    }

    @Override
    public void refactor(cargaobjetoestado cost) {
        List<estado> lista = cost.getlista();
        String sql = "update estado set fecha=?, idpedido=? where idestado = ?";
       
        try (Connection c = DriverManager.getConnection(URL, USER, PASS); PreparedStatement p = c.prepareStatement(sql)) {
            for (estado est : lista) {
                p.setString(1, est.getFecha());
                p.setInt(2, est.getIdpedido());
                p.setInt(3, est.getIdestado());
                p.addBatch();
            }
            p.executeBatch();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

}
