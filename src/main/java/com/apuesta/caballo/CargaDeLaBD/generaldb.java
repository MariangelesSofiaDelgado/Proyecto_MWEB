package com.apuesta.caballo.CargaDeLaBD;
public interface generaldb<T> {
String URL = "jdbc:mysql://localhost:3306/roma";
   String USER = "root";
        String PASS = "";



    public T Entregar();

    public void encargar(T cost);

    public void refactor(T cost);

}
