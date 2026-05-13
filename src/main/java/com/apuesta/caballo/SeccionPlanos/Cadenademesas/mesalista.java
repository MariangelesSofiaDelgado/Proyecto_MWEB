package com.apuesta.caballo.SeccionPlanos.Cadenademesas;

import com.apuesta.caballo.SeccionPlanos.mesas;

public class mesalista {
    
    mesas m = new mesas();
    String mensaje = m.generarobject();

    public int[] getmesalista(){
String[] partes = mensaje.split("\\."); // Dividimos por el punto
int[] holio = new int[partes.length];   // Creamos el arreglo del mismo tamaño

for (int i = 0; i < partes.length; i++) {
    holio[i] = Integer.parseInt(partes[i]); // Convertimos cada parte
}

return holio;
}







}
