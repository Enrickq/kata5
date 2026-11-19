# Kata 5 · Balanceo dinámico con NGINX y Consul

Servicio de cálculo distribuido en dos grupos de réplicas: uno devuelve términos de la sucesión de Fibonacci y el otro, números primos. NGINX expone una ruta para cada grupo y distribuye las peticiones entre sus instancias. Consul registra los servicios, comprueba su estado y permite que NGINX descubra los destinos mediante DNS SRV.

La incorporación o caída de una réplica se refleja en el balanceador sin cambiar las URL del cliente, editar la configuración ni recargar NGINX.

## Vídeo de demostración

[Ver la demostración de la Kata 5 en YouTube](https://youtu.be/m_Y3TVgMWnk)

## Arquitectura

```text
                       Cliente HTTP
                            |
                     localhost:8080
                            |
                          NGINX <------ DNS SRV ------ Consul
                            |                          /health
                +-----------+-----------+
                |                       |
        fibonacci_pool              primes_pool
                |                       |
        +-------+-------+       +-------+-------+
        |               |       |               |
    fibonacci-1     fibonacci-2 primes-1      primes-2
                        
    fibonacci-3: réplica adicional para comprobar el alta dinámica
```

Cada grupo tiene un nombre de servicio propio en Consul. Las réplicas de un mismo grupo comparten ese nombre, pero se registran con identificadores individuales. NGINX utiliza `zone`, `resolver` y `server ... service=... resolve` para consultar los registros SRV de Consul y actualizar los destinos disponibles.

| Ruta pública | Upstream NGINX | Nombre en Consul | Réplicas iniciales |
| --- | --- | --- | --- |
| `GET /api/v1/fibonacci?n=8` | `fibonacci_pool` | `fibonacci` | `fibonacci-1`, `fibonacci-2` |
| `GET /api/v1/primes?limit=20` | `primes_pool` | `primes` | `primes-1`, `primes-2` |

`fibonacci-3` se incorpora durante la prueba de alta mediante el perfil `expansion` de Docker Compose. No sustituye la configuración de ninguno de los upstreams.

Todos los contenedores comparten la red privada `kata5_kata5`. Los servicios Java escuchan en el puerto `8080` de sus respectivos contenedores, sin publicar ese puerto en el equipo anfitrión. Solo NGINX recibe las peticiones externas del cliente.

## Requisitos y versiones

| Componente | Versión |
| --- | --- |
| Java | JDK 21 |
| Maven | 3.9.x (3.9.9 en la imagen de compilación) |
| Javalin | 6.7.0 |
| Gson | 2.13.1 |
| JUnit | 5.11.4 |
| NGINX | 1.28.0-alpine |
| Consul | 1.20.3 |
| Docker | Docker Engine/Desktop con Compose v2 |
| Python | 3.9 o superior, sin paquetes adicionales |

Los puertos publicados en `127.0.0.1` son:

| Puerto | Uso |
| --- | --- |
| `8080/tcp` | API pública de NGINX |
| `8500/tcp` | API HTTP e interfaz de Consul |
| `8600/udp` | Consultas DNS a Consul |

NGINX 1.28.0 permite el descubrimiento dinámico de servidores con `resolve` en su versión de código abierto. Consul se ejecuta en modo de desarrollo, apropiado para esta práctica, no como configuración de producción.

## Estructura del proyecto

```text
kata5/
├── pom.xml                         Proyecto Maven multimódulo
├── compose.yaml                    Servicios, red y réplicas
├── client/                         Cliente Java con HttpClient.sendAsync
├── service/
│   ├── Dockerfile                   Compilación y ejecución con Java 21
│   └── src/
│       ├── main/java/es/ulpgc/kata5/
│       │   ├── Main.java            Arranque y composición
│       │   ├── domain/              Algoritmos de Fibonacci y primos
│       │   ├── application/         Casos de uso y su interfaz
│       │   ├── config/              Configuración del servicio
│       │   └── infrastructure/
│       │       ├── http/            Adaptador HTTP con Javalin y Gson
│       │       └── consul/          Registro y baja en Consul
│       └── test/                    Pruebas JUnit
├── infra/
│   ├── nginx.conf                   Rutas y upstreams dinámicos
│   └── consul.json                  Configuración DNS
├── scripts/
│   ├── verify.py                    Verificación de los tres escenarios
│   ├── repeticiones.py              Tres despliegues completos
│   ├── repeticiones.sh              Entrada alternativa desde Bash
│   └── test_verify.py               Pruebas del verificador
└── docs/evidencias/                Registros de las tres ejecuciones
```

La lógica de cálculo no conoce Javalin, Gson ni Consul. Los casos de uso reciben los datos del dominio y el adaptador HTTP se ocupa de interpretar parámetros y construir respuestas. El registro en Consul está aislado en `ConsulRegistrar`, que utiliza peticiones HTTP asíncronas y reintentos cuando es necesario.

## Puesta en marcha

Los siguientes comandos se ejecutan **desde la raíz del repositorio**, donde se encuentra `compose.yaml`. En Windows se pueden utilizar directamente desde la terminal PowerShell de IntelliJ IDEA.

Con Docker Desktop iniciado:

```powershell
docker compose up -d --build --wait
docker compose ps
```

La primera ejecución descarga las imágenes y dependencias necesarias. Una vez iniciado el sistema, la API queda disponible en `http://127.0.0.1:8080` y la interfaz de Consul en `http://127.0.0.1:8500/ui/`.

Para comprobar la configuración y la versión activa de NGINX:

```powershell
docker compose exec -T nginx nginx -t
docker compose exec -T nginx nginx -v
```

El puerto público puede cambiarse, por ejemplo, a `8090` antes del arranque:

```powershell
$env:PUBLIC_PORT = "8090"
docker compose up -d --build --wait
```

En ese caso, las peticiones y el verificador deben apuntar a `http://127.0.0.1:8090`. Si se utiliza la configuración por defecto, el puerto es `8080`.

## API HTTP

### Fibonacci

```powershell
curl.exe -i "http://127.0.0.1:8080/api/v1/fibonacci?n=8"
```

Respuesta JSON:

```json
{"terms":8,"numbers":[0,1,1,2,3,5,8,13]}
```

### Números primos

```powershell
curl.exe -i "http://127.0.0.1:8080/api/v1/primes?limit=20"
```

Respuesta JSON:

```json
{"limit":20,"count":8,"numbers":[2,3,5,7,11,13,17,19]}
```

Ambas operaciones devuelven `200 OK`, `Content-Type: application/json` y las cabeceras `X-Service` y `X-Node`. Esta última identifica la réplica que atendió cada petición. NGINX añade `X-Upstream-Address` para facilitar el diagnóstico.

Los parámetros están validados: `n` admite valores de **1 a 90** y `limit` de **2 a 10000**. Los valores inexistentes, no numéricos o fuera de rango producen `400 Bad Request` con un mensaje JSON. Por ejemplo:

```powershell
curl.exe -i "http://127.0.0.1:8080/api/v1/fibonacci?n=0"
```

```json
{"error":"n debe estar entre 1 y 90"}
```

## Comprobación del balanceo

El script `scripts/verify.py` realiza peticiones a las dos rutas y revisa los códigos HTTP, el JSON, las cabeceras de identificación y las réplicas que han respondido. También prueba entradas incorrectas. Espera durante la convergencia de Consul y DNS, por lo que un cambio de estado no necesita reflejarse en la primera solicitud.

**1. Reparto inicial.** Deben aparecer `fibonacci-1`, `fibonacci-2`, `primes-1` y `primes-2`:

```powershell
py -3 scripts/verify.py --scenario baseline
```

**2. Baja de una réplica.** Se detiene `fibonacci-2`; Fibonacci sigue disponible por `fibonacci-1`, mientras que Primes mantiene sus dos instancias:

```powershell
$nginxAntes = docker compose ps -q nginx
docker compose stop fibonacci-2
py -3 scripts/verify.py --scenario down
```

**3. Alta de otra réplica.** Se incorpora `fibonacci-3` sin modificar `nginx.conf`:

```powershell
docker compose --profile expansion up -d fibonacci-3
py -3 scripts/verify.py --scenario up
$nginxAntes -eq (docker compose ps -q nginx)
```

La última expresión devuelve `True` si NGINX conserva el mismo contenedor desde el inicio de la prueba.

**4. Restauración.** Se retira la tercera réplica y vuelve a arrancar la segunda:

```powershell
docker compose --profile expansion stop fibonacci-3
docker compose start fibonacci-2
py -3 scripts/verify.py --scenario baseline
```

La salud de las instancias también puede consultarse en Consul:

```powershell
curl.exe "http://127.0.0.1:8500/v1/health/service/fibonacci?passing=1"
curl.exe "http://127.0.0.1:8500/v1/health/service/primes?passing=1"
```

## Repeticiones y resultados

Se realizaron **tres despliegues completos** con `scripts/repeticiones.py`. En cada uno se reinició el entorno y se verificaron el reparto inicial, la baja de `fibonacci-2`, el alta de `fibonacci-3`, la continuidad del otro grupo y la restauración. La configuración de NGINX permaneció sin cambios durante cada ciclo; el script comprobó además que su identificador de contenedor no cambiaba.

No se introdujeron variaciones entre repeticiones: se mantuvieron los mismos servicios, rutas, réplicas y condiciones de prueba para poder comparar los resultados.

| Repetición | Resultado | Registro |
| --- | --- | --- |
| 1 | PASS | [repeticion-1.md](docs/evidencias/repeticion-1.md) |
| 2 | PASS | [repeticion-2.md](docs/evidencias/repeticion-2.md) |
| 3 | PASS | [repeticion-3.md](docs/evidencias/repeticion-3.md) |

Los registros contienen los comandos ejecutados, la salida de las verificaciones y la identificación de los contenedores. Cada repetición quedó recogida también en un commit independiente.

Para reproducir los tres ciclos desde cero en Windows:

```powershell
py -3 scripts/repeticiones.py
```

El comando elimina y vuelve a crear el despliegue entre iteraciones; por ello conviene usarlo en un entorno de pruebas. Si alguna comprobación falla, el script se interrumpe en lugar de marcar la repetición como correcta.

## Pruebas y compilación local

Con JDK 21 y Maven instalados:

```powershell
mvn -B clean test
mvn -B package
```

Las pruebas JUnit cubren los cálculos y la validación de configuración. El verificador HTTP dispone además de pruebas con un servidor simulado:

```powershell
py -3 -m unittest discover -s scripts -p "test_*.py" -v
```

El cliente Java puede ejecutarse después de compilar el proyecto, con NGINX levantado:

```powershell
java -jar client/target/client-1.0.0.jar http://127.0.0.1:8080 20
```

Envía varias solicitudes asíncronas a ambas rutas y muestra el nodo y la respuesta de cada una. Los directorios `target/` y los archivos compilados están excluidos del repositorio mediante `.gitignore`.

Para consultar posibles problemas de arranque:

```powershell
docker compose logs nginx
docker compose logs consul
docker compose logs fibonacci-1
```

Para detener todo el entorno, incluida la réplica opcional:

```powershell
docker compose --profile expansion down
```

## Documentación técnica

- [NGINX: configuración de upstreams](https://nginx.org/en/docs/http/ngx_http_upstream_module.html)
- [Consul: descubrimiento de servicios por DNS](https://developer.hashicorp.com/consul/docs/discover/service/static)
- [Javalin](https://javalin.io/documentation)
