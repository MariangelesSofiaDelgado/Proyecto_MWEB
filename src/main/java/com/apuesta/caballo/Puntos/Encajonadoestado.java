package com.apuesta.caballo.Puntos;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/estado")
public class Encajonadoestado {
 @CrossOrigin(origins = "*") 
    @GetMapping("/saludo")
    public String getMethodName() {
        return "Todo bien";
    }
    





    
    
}