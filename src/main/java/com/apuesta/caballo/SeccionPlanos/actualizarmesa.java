package com.apuesta.caballo.SeccionPlanos;

import com.apuesta.caballo.SeccionPlanos.Cadenademesas.mesalista;

public class actualizarmesa {

    int[] lista;

    public actualizarmesa() {
        mesalista m = new mesalista();
        this.lista = m.getmesalista();

    }

    public int[] Actualizar(int index, boolean comprobante) {
        int[] lista2 = this.lista.clone();
        if (comprobante) {
            lista2[index] = 1;
        } else {
            lista2[index] = 0;
        }

        return lista2;
    }

    public int[] Cerrartodo() {
        int[] lista2 = this.lista.clone();
        for (int i = 0; i < lista2.length; i++) {
            lista2[i] = 0;

        }
        return lista2;

    }

    public int[] AbrirTodo() {
  int[] lista2 = this.lista.clone();
        for (int i = 0; i < lista2.length; i++) {
            lista2[i] = 1;

        }
        return lista2;


    }

    public void general(int[] lista) {

        this.lista = lista.clone();
        mesas m = new mesas();
        m.guardarEstadoMesas(lista);
    }

    public int[] getLista() {
        return lista;
    }





}
