

# Parcial TDSE

---

## Arquitectura
Hay 2 carpetas, el proxy y el math services


**Estructura del Proxy**
```
src/main/java/org/example/
├── main.java          
├── nodo.java               
├── proxyController.java      
├── proxyService/
├── Verificador/
└── inde.xhtml

```

**Estructura del mathService**
```
src/main/java/org/example/
├── main.java          
├── mathController.java               
├── mathService.java      
└── 

```

### Flujo 
```
Alguien hace una peticion al ec2 del proxy con el puerto 8080
este devuelve el static/index.html
ingresa un numero y le dal alboton
    │
    ▼
el proxy revisa cada 5 segundos los nodos que pueden estar vivos
coje el nodo maestro  y le manda la soli a el
    │
    ▼
EL nodo maestro hosteado en EC2 revisa la soli y la devuelve al proxy, el proxy la devuelve al html y el html la muestra en pantalla
```

---

## Requisitos

- **Java 17**
- **Maven 3.6** o superior

Verificar instalación:
```bash
java -version
mvn -version
```

---




---

## Uso 

### Proxycontroller
Anotar la clase con `@RestController` y sus métodos con `@GetMapping`:
```java
@RestController
@RequestMapping("/")
public class proxyController {
    @Autowired
    private proxyService proxyService;
    @CrossOrigin(origins = "*")
    @GetMapping("/proxy/**")
    public String proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI().replace("/proxy", "");
        String query = request.getQueryString();

        if (query != null) {
            path += "?" + query;
        }

        return proxyService.llamarActivo(path);
    }
}
```

El proxy con /** intercept las solcitudes hechas al /proxy/{otro}
se obtiene el path y las query y las manda al service

### Servicio 
```java
   private final Verificador verificador;

    public proxyService(Verificador verificador) {
        this.verificador = verificador;
    }
```
Tiene un verificador inyectado para estar revisando la disponibilidad de los servicios mas adelante se explica

Para consumir servicios rest se usa el codigo dado por el profesor con la diferencia que 

```java
  String baseUrl = verificador.getMasterUrl();
        String base = baseUrl.split(":8080")[0] + ":8080";
        String targetUrl = base + path;
```

la url se toma del nodo maestro del verificador, y se le agrega el path.

```java
   try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream())
                );

                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                return response.toString();
            } else {
                return "Error: " + responseCode;
            }

        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
```

### Verificador 

```java
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
```



El verificador cuenta con una lista de **Nodos** estos nodos:

```java
public class Nodo {
    public String url;
    public boolean master;

    public Nodo(String u, boolean m){
        this.master = m;
        this.url = u;
    }
}

```

Cada nodo guarda la url de la instancia del ec2 (dns publico) y un atributo diciendo si es maestro o no


En verificacion se crea un hilo aparte y se anota como daemon (se muere apenas se cierre la app)
Se agregan los nodos, y se pone 1 commo maestro (atributo en true) que usara para redirigir en el proxy

el metodo **Verificar** estara enviando solicitud al nodo maestro cada 5 segundos para verificar si esta activo, y si devuelve un 200 todo ok y no cambiara de lo contrario llamara al metodo 
cambiarMaestro()
este metodo hace un stream y toma el primer nodo que tenga url diferente al maestro que acaba de caer, si encuentra alguno cambia su atributo a maestro (lo asciende) y el verificador lo toma como nodo maestro 
si ese otro vuelve a caer vuelve a buscar en su lista de nodos.

### Math Service


## Controlador 
El controlador recibe parametros 1 numero

```
@CrossOrigin(origins = "*")
    @GetMapping("/collat")
    public String  getCollatz(@RequestParam(defaultValue = "1") String value){
```

le delega la funcion al service

```
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
```


el metodo principal es collatz 
este revisa si el numero es mayor a 0 y diferente de 1
y se aplican las reglas y se va creando un string de la secuencia

este string lo devuelve al controller que lo enviara al proxy 


# Desplegue

Se realiza mvn clean package para obtener el .jar del servicio rest
(math) y (proxy)
Se realiza una conexion sftp y con put se sube dicho jar a cada instancia EC2

* 2 intancias EC2 para mathService
* 1 instancia EC2 para proxy

para prender el jar se realiza el comando java -jar nombreDelJar.jar

donde nombre del jar es el nombre del jar dado por maven 

### Fotos




### video





## Construido con

- **Java** — Lenguaje principal
- **Maven** — Gestión de dependencias y build
- **AWS** - Servidor de aplicaciones
---

## Autor

* **Santiago Suarez** — [SantiagoSu15](https://github.com/SantiagoSu15)

