package com.apuesta.caballo.listadeobjetos;

import java.util.ArrayList;
import java.util.List;

import com.apuesta.caballo.Objetos.cocina;

public class cargarobjetococina implements lista<cocina> {

    List<cocina> listacocina = new ArrayList<>();

    @Override
    public void agregar(cocina objEstado) {
        this.listacocina.add(objEstado);

    }

    @Override
    public void actualizar(int index, cocina objestado) {
        this.listacocina.set(index, objestado);
    }

    @Override
    public void eliminarespecifico(int index) {
        this.listacocina.remove(index);

    }

    @Override
    public void eliminartodo() {
        this.listacocina.clear();

    }

    @Override
    public List<cocina> getlista() {
        return this.listacocina;
    }

}
