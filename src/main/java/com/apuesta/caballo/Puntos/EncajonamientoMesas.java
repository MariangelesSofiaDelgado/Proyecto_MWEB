package com.apuesta.caballo.Puntos;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apuesta.caballo.SeccionPlanos.actualizarmesa;

@RestController
@RequestMapping("/mesa")
public class EncajonamientoMesas {

    @CrossOrigin(origins = "*")
    @GetMapping("/saludo")
    public String getMethodName() {
        return "Todo bien";
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/actualizar/{index}/{comprobante}")
    public void setmesa(@PathVariable int index, @PathVariable boolean comprobante) {
        actualizarmesa m = new actualizarmesa();
       int[] pequenno = m.Actualizar(index, comprobante);
m.general(pequenno);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/actualizar")
    public int[] setmesa() {
        actualizarmesa m = new actualizarmesa();
        return m.getLista();

    }

}
