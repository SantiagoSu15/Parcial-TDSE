package org.example;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class Verificador {
    private List<Nodo> nodos;
    private Nodo maestro;

    private final HttpClient  client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    public Verificador(){
        this.nodos = new ArrayList<>();
        Nodo n1 = new Nodo("http://ec2-54-167-180-58.compute-1.amazonaws.com:8080/collat?value=1",true);
        Nodo n2 = new Nodo("http://ec2-52-90-176-132.compute-1.amazonaws.com:8080/collat?value=1",false);
        this.nodos.add(n1);
        this.nodos.add(n2);
        Optional<Nodo> master = Optional.of(nodos.stream().filter(n -> n.master).findFirst().get());
        this.maestro = master.orElse(null);
        Thread t = new Thread(this::verificar);
        t.setDaemon(true);
        t.start();
    }
    public String getMasterUrl(){
        return this.maestro.url;
    }
    public  void verificar(){
        while(true){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(maestro.url))
                    .GET()
                    .build();

            try{
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("sigue vivo: OK");
                } else {
                    cambiarMaestro();
                }
            } catch (Exception e) {
                System.out.println("Cambio de masestro");
                cambiarMaestro();
            }

            try{
                Thread.sleep(5000);
            }catch (Exception e){
                Thread.interrupted();
                break;

            }
        }
    }

    public void cambiarMaestro(){
        this.nodos.stream().filter(n->!n.url.equals(this.maestro.url)).findFirst()
                .ifPresent(nodo->{
                    this.maestro.master = false;
                    this.maestro = nodo;
                    this.maestro.master = true;
                }
        );
    }
}
