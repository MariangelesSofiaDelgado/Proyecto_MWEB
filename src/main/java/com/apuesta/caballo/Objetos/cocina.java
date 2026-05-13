package com.apuesta.caballo.Objetos;

public class cocina {
    private int idpedido;
    private int idplato;
    private String nombredeplato;
    private int mesaalaquepertenece;
    private String extras;
    private int idmesero;

    public cocina() {
    }

    public cocina(String extras, int idmesero, int idpedido, int idplato, int mesaalaquepertenece, String nombredeplato) {
        this.extras = extras;
        this.idmesero = idmesero;
        this.idpedido = idpedido;
        this.idplato = idplato;
        this.mesaalaquepertenece = mesaalaquepertenece;
        this.nombredeplato = nombredeplato;
    }

    public int getIdpedido() {
        return idpedido;
    }

    public void setIdpedido(int idpedido) {
        this.idpedido = idpedido;
    }

    public int getIdplato() {
        return idplato;
    }

    public void setIdplato(int idplato) {
        this.idplato = idplato;
    }

    public String getNombredeplato() {
        return nombredeplato;
    }

    public void setNombredeplato(String nombredeplato) {
        this.nombredeplato = nombredeplato;
    }

    public int getMesaalaquepertenece() {
        return mesaalaquepertenece;
    }

    public void setMesaalaquepertenece(int mesaalaquepertenece) {
        this.mesaalaquepertenece = mesaalaquepertenece;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public int getIdmesero() {
        return idmesero;
    }

    public void setIdmesero(int idmesero) {
        this.idmesero = idmesero;
    }




}
