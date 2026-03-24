package org.example;

public class Nodo {
    public String url;
    public boolean master;

    public Nodo(String u, boolean m){
        this.master = m;
        this.url = u;
    }
}
