package com.apuesta.caballo.listadeobjetos;

import java.util.ArrayList;
import java.util.List;

import com.apuesta.caballo.Objetos.estado;
import com.apuesta.caballo.Objetos.mesero;

public class cargarobjetomesero implements lista<mesero> {

List<mesero> listamesero = new ArrayList<>();
estado objMesero;

    @Override
    public void agregar(mesero objEstado) {
  this.listamesero.add(objEstado);
  
    }

    @Override
    public void actualizar(int index, mesero objestado) {
     this.listamesero.set(index, objestado);
    }

    @Override
    public void eliminarespecifico(int index) {
        this.listamesero.remove(index);
    }

    @Override
    public void eliminartodo() {
        this.listamesero.clear();
    }

    @Override
    public List<mesero> getlista() {
       return this.listamesero;
    }



    
}