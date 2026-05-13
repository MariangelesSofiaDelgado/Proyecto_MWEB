package com.apuesta.caballo.listadeobjetos;
import java.util.ArrayList;
import java.util.List;

import com.apuesta.caballo.Objetos.estado;
public class cargaobjetoestado implements lista<estado> {
List<estado> listaestado = new ArrayList<>();
estado objestado;




@Override
public void  agregar(estado objEstado){
this.listaestado.add(objEstado);
}

@Override
public void actualizar(int index, estado objestado){
this.listaestado.set(index, objestado);

}

@Override
public void eliminarespecifico(int index){
  
    this.listaestado.remove(index);
}

@Override
public void eliminartodo(){
this.listaestado.clear();

}

@Override
public List<estado> getlista(){

    return this.listaestado;

}


}