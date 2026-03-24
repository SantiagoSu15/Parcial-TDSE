package org.example;

import org.springframework.stereotype.Service;

@Service
public class mathService {
    private String secuencia = "";

    public String collatz(int valor){

        while(valor !=1 && valor > 0){
            anadirNum(valor);
            if(valor % 2 == 0){
                valor = par(valor);
            }else{
                valor = impar(valor);
            }
            System.out.println(this.secuencia);
        }


        return  this.secuencia + "->" +1;
    }


    public void resetear(){
        this.secuencia = "";
    }

    private int impar(int n){
        return (3*n)+1;
    }
    private int par(int n){
        return n/2;
    }

    private void anadirNum(int n){
        if(this.secuencia.equals("")){
            this.secuencia =  this.secuencia + n;
        }else{
            this.secuencia =  this.secuencia+"->"+n;

        }

    }
    private String pasarAString(int v){
        return  String.valueOf(v);
    }
}
