package com.apuesta.caballo.listadeobjetos;

import java.util.List;

public interface lista<T> {
 void  agregar(T objEstado);

 void actualizar(int index, T objestado);

 void eliminarespecifico(int index);


 void eliminartodo();


 List<T> getlista();


    
}