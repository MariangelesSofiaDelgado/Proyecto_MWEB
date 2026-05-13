package com.apuesta.caballo.Objetos;

public class estado {

    private int idestado;
    private String fecha;
    private int idpedido;

    public estado() {
    }

    public estado(int idestado, String fecha, int idpedido) {
        this.idestado = idestado;
        this.fecha = fecha;
        this.idpedido = idpedido;
    }

    public String getFecha() {
        return fecha;
    }

    public int getIdestado() {
        return idestado;
    }

    public int getIdpedido() {
        return idpedido;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setIdestado(int idestado) {
        this.idestado = idestado;
    }

    public void setIdpedido(int idpedido) {
        this.idpedido = idpedido;
    }

}
