# Kata 5 — Balanceo dinámico con NGINX y Consul

**Ingeniería del Software II — ULPGC.** Implementación independiente de la Kata 5 conforme al **enunciado actualizado**: al menos dos upstreams dinámicos, dos réplicas por grupo, descubrimiento DNS SRV de Consul, comprobaciones de reparto/baja/alta sin recargar NGINX y **tres repeticiones reales** del despliegue. La base conceptual procede del ejemplo `kata5.zip` trabajado en clase, no de una modificación de la entrega anterior de Kata 4.

> **Estado de la entrega:** el código y los scripts de verificación se incluyen en el repositorio. Antes de entregar hay que **ejecutar Docker en el ordenador del estudiante**, realizar y versionar las tres repeticiones, grabar el vídeo de duración máxima de 7 minutos y sustituir el marcador del enlace real al vídeo. No se presentan pruebas de ejecución que aún no se hayan realizado.

**Vídeo explicativo (≤ 7 minutos):** `PENDIENTE: sustituir por enlace público real (YouTube) antes de entregar`. Guion: [`docs/guion-video.md`](docs/guion-video.md).

## 1. Qué se ha construido

Dos operaciones deterministas: **los primeros `n` números de Fibonacci** y **los números primos hasta un límite**. Se ejecutan dos réplicas iniciales por operación. Cada instancia se registra al arrancar en Consul con un identificador único, nombre de servicio, IP accesible desde la red Docker, puerto y HTTP `GET /health`. NGINX usa **dos upstreams independientes** y descubre las réplicas sanas mediante consultas **DNS SRV**; el cliente solo conoce `localhost:8080`.

```text
Cliente Java / Python / curl
           │ http://127.0.0.1:8080
           ▼
    NGINX 1.28.0 (puerto 80 en contenedor)
           │                                      Consul 1.20.3
           │              SRV + A, DNS:8600  ◀─── registro:8500
           │                                      ▲        ▲
           ├─ /api/v1/fibonacci ─ upstream fibonacci_pool  │
           │     ├─ fibonacci-1 :8080 ──────────────────────┤ HTTP /health
           │     ├─ fibonacci-2 :8080 ──────────────────────┤ (cada 2 s)
           │     └─ fibonacci-3 :8080 (alta posterior) ─────┤
           │                                                │
           └─ /api/v1/primes ─ upstream primes_pool          │
                 ├─ primes-1 :8080 ──────────────────────────┤
                 └─ primes-2 :8080 ──────────────────────────┘
```

NGINX y los cinco contenedores de infraestructura/servicio iniciales (1 Consul + 1 NGINX + 4 Java = **6 contenedores**) comparten la red privada Compose `kata5_kata5`. Las réplicas **no** publican sus puertos al anfitrión: son accesibles solo por la red Docker. `fibonacci-3` se añade explícitamente con el perfil `expansion` durante la prueba. Consul escucha la API HTTP en 8500 y DNS en 8600/UDP.

| Ruta pública | Upstream NGINX | Servicio DNS de Consul | Réplicas iniciales |
|---|---|---|---|
| `GET /api/v1/fibonacci?n=8` | `fibonacci_pool` | `fibonacci.service.consul` (`_fibonacci._tcp.service.consul`) | `fibonacci-1`, `fibonacci-2` |
| `GET /api/v1/primes?limit=20` | `primes_pool` | `primes.service.consul` (`_primes._tcp.service.consul`) | `primes-1`, `primes-2` |

**Descubrimiento dinámico real:** `infra/nginx.conf` define `zone` por upstream y un servidor `service.consul service=fibonacci resolve` / `service.consul service=primes resolve`, junto a `resolver consul:8600 valid=2s ipv6=off`. Cada servicio anuncia su **IP interna Docker**, no `localhost` ni `host.docker.internal`. Consul publica solo réplicas con salud `passing` (configuración `only_passing: true`). **No hay scripts que reescriban la configuración de NGINX ni comandos `nginx -s reload`**. Se debe dar tiempo a los *health checks* (2 s), el TTL DNS (2 s) y la caché del resolver de NGINX (2 s; los tiempos efectivos pueden variar).

## 2. Versiones y requisitos

| Dependencia | Versión fijada o requerida |
|---|---|
| JDK de compilación / ejecución | **21** (OpenJDK / Eclipse Temurin) |
| Maven | 3.9.x recomendada (dentro de imagen de construcción: **3.9.9**) |
| Javalin | **6.7.0** |
| Gson | **2.13.1** |
| JUnit 5 | **5.11.4** |
| NGINX Open Source | **1.28.0-alpine** (≥ 1.27.3, permite `resolve` en upstreams) |
| Consul | **hashicorp/consul:1.20.3** (modo `-dev`, solo laboratorio) |
| Docker | Docker Desktop/Engine reciente con **Compose v2** |
| Python | **3.9+**, librería estándar, para verificación y repetición automatizadas |

**Puertos publicados en el ordenador:** `127.0.0.1:8080/tcp` (NGINX), `127.0.0.1:8500/tcp` (API/UI Consul) y `127.0.0.1:8600/udp` (DNS Consul, para inspección). Todas las réplicas usan `8080/tcp` **dentro de sus contenedores independientes**. Se puede cambiar el puerto público de NGINX definiendo `PUBLIC_PORT` (ver apartado de Windows). Los `Dockerfile` realizan la compilación Maven en una fase separada y copian solo el JAR ejecutable a una imagen con JRE 21.

## 3. Estructura y responsabilidades

```text
kata5/
├── pom.xml                          # Maven multi-módulo
├── service/
│   ├── Dockerfile                   # compilación multi-stage Java 21
│   ├── pom.xml
│   └── src/main/java/es/ulpgc/kata5/
│       ├── Main.java                # composición de dependencias y arranque
│       ├── domain/                 # FibonacciSequence y PrimeNumbers (puros)
│       ├── application/            # CalculationUseCase y adaptadores de uso
│       └── infrastructure/
│           ├── http/HttpApi.java    # entrada HTTP Javalin + JSON Gson
│           └── consul/ConsulRegistrar.java # salida a Consul con sendAsync
├── client/
│   └── src/main/java/es/ulpgc/kata5/client/Main.java # HttpClient.sendAsync
├── infra/
│   ├── nginx.conf                    # dos upstreams SRV dinámicos
│   └── consul.json                   # DNS y salud
├── compose.yaml                      # NGINX + Consul + 4 réplicas (+ 1 opcional)
├── scripts/
│   ├── verify.py                     # test negro HTTP, 3 escenarios
│   ├── repeticiones.py               # 3 despliegues reales Windows/Linux
│   ├── repeticiones.sh               # envoltorio opcional Bash
│   └── test_verify.py                # unit tests del verificador simulado
└── docs/
    ├── guion-video.md
    ├── matriz-rubrica.md
    └── evidencias/                    # salidas reales generadas al ejecutar
```

Arquitectura ligera de **puertos y adaptadores**: el dominio no importa Javalin, Gson, HTTP, Docker ni Consul. El puerto `CalculationUseCase` separa los casos de uso del adaptador HTTP. `HttpApi` traduce/valida peticiones y prepara códigos y JSON; `ConsulRegistrar` aísla el registro y las llamadas de infraestructura; `Main` construye las dependencias. Cada pieza tiene una responsabilidad concreta, evitando herencia o abstracciones innecesarias. Las instancias de cada upstream ejecutan **el mismo código de cálculo**.

## 4. Compilar y ejecutar

Con **Docker Desktop iniciado** y desde la raíz del repositorio:

```bash
# Comprobación de Docker
 docker compose version

# Construir imágenes, arrancar Consul, NGINX y 2+2 réplicas
 docker compose up -d --build --wait

# Inspeccionar procesos, servicios y versiones
 docker compose ps
 docker compose exec -T nginx nginx -v
 docker compose exec -T nginx nginx -t
 docker compose exec -T consul consul catalog services
```

El primer `docker compose up` requiere conexión a Docker Hub y Maven Central para descargar las imágenes y las dependencias Java; tarda más que las ejecuciones posteriores. NGINX puede iniciarse antes de que Consul haya marcado las réplicas como sanas: `scripts/verify.py` espera el período de convergencia en lugar de dar por fallida la prueba inmediatamente.

En Windows **PowerShell**, los comandos `docker compose` son iguales. Para pruebas Python, usar `py -3 scripts/verify.py ...` si `python` no está en PATH. Para cambiar puerto público en PowerShell: `$env:PUBLIC_PORT = '8090'`; luego verificar con `--base http://127.0.0.1:8090`.

Para compilar/probar Java **fuera de Docker**, si Maven y JDK 21 están instalados:

```bash
mvn -B clean verify
mvn -B package
java -jar client/target/client-1.0.0.jar http://127.0.0.1:8080 20
```

Los JAR se generan localmente en `target/` y están excluidos por `.gitignore`: **nunca subir binarios al repositorio**.

## 5. Contrato HTTP y errores previstos

Consultar el balanceador, **no** las IP/puertos privados de las réplicas:

```bash
curl -i 'http://127.0.0.1:8080/api/v1/fibonacci?n=8'
# HTTP/1.1 200 OK
# X-Node: fibonacci-1   (o fibonacci-2, según balanceo)
# X-Service: fibonacci
# Content-Type: application/json
# {"terms":8,"numbers":[0,1,1,2,3,5,8,13]}

curl -i 'http://127.0.0.1:8080/api/v1/primes?limit=20'
# HTTP/1.1 200 OK
# X-Node: primes-1       (o primes-2)
# X-Service: primes
# {"limit":20,"count":8,"numbers":[2,3,5,7,11,13,17,19]}

curl -i 'http://127.0.0.1:8080/api/v1/fibonacci?n=0'
# HTTP/1.1 400 Bad Request; JSON: {"error":"n debe estar entre 1 y 90"}

curl -i 'http://127.0.0.1:8080/api/v1/primes?limit=hola'
# HTTP/1.1 400 Bad Request; JSON: {"error":"El parámetro limit debe ser un entero positivo"}
```

Estas son **respuestas esperadas según el código**, no capturas de una prueba Docker realizada en este documento. `n` debe estar en `1..90`; `limit`, en `2..10000`. Se rechazan valores faltantes, inválidos, excesivos o fuera de rango. Las rutas no definidas producen `404`. La cabecera `X-Node` permite distinguir réplicas; `X-Service` confirma el grupo, y `X-Upstream-Address` facilita diagnosticar el backend real elegido por NGINX.

## 6. Verificación manual completa (una repetición)

Usa la **misma URL pública** antes y después de modificar réplicas.

**A. Reparto normal:** comprobar que aparecen los cuatro nodos y que los cálculos son idénticos en cada grupo:

```bash
python3 scripts/verify.py --scenario baseline
# Windows: py -3 scripts/verify.py --scenario baseline
```

El verificador realiza 24 solicitudes por ruta en cada intento, valida JSON, `X-Node`, `X-Service`, resultados, entrada inválida y la presencia de **ambas réplicas de cada grupo**. Si NGINX aún conserva una IP antigua o Consul sigue actualizando salud, espera y reintenta hasta 100 segundos antes de fallar claramente.

**B. Baja de una réplica Fibonacci:**

```bash
# Guarda antes el ID del contenedor NGINX (debe ser idéntico al final)
docker compose ps -q nginx

docker compose stop fibonacci-2
python3 scripts/verify.py --scenario down
# Solo fibonacci-1 debe contestar Fibonacci; primes-1 y primes-2 siguen disponibles.
```

**C. Alta de una réplica NUEVA (no basta reactivar la anterior):**

```bash
docker compose --profile expansion up -d fibonacci-3
python3 scripts/verify.py --scenario up
# Deben contestar fibonacci-1 y fibonacci-3; primes-1 y primes-2 siguen disponibles.

docker compose ps -q nginx
# Comprueba el mismo ID antes/después. No se ha ejecutado nginx -s reload.
```

**D. Restaurar antes de repetir:**

```bash
docker compose --profile expansion stop fibonacci-3
docker compose start fibonacci-2
python3 scripts/verify.py --scenario baseline
```

Consultar Consul (su UI se abre en `http://127.0.0.1:8500`) y los registros reales:

```bash
docker compose exec -T consul consul catalog services
# HTTP devuelve el estado y las direcciones de las instancias que están pasando checks:
curl 'http://127.0.0.1:8500/v1/health/service/fibonacci?passing=1'
curl 'http://127.0.0.1:8500/v1/health/service/primes?passing=1'
# Si tienes dig en tu equipo/WSL: consultar directamente el DNS SRV publicado por Consul:
dig @127.0.0.1 -p 8600 -t SRV fibonacci.service.consul
dig @127.0.0.1 -p 8600 -t SRV primes.service.consul
```

**Diagnóstico si falla algo:** `docker compose logs consul`, `docker compose logs nginx`, `docker compose logs fibonacci-1`; `docker compose exec -T nginx nginx -t`. No confundas una propagación DNS de unos segundos con un fallo permanente. Comprueba que se usa NGINX ≥ 1.27.3, Docker está en modo *Linux containers*, el puerto público no está ocupado y Consul ve las IP internas, no `localhost`.

## 7. TRES repeticiones deliberadas y evidencia reproducible

El profesor exige **tres ejecuciones completas**, cada una con (1) despliegue, (2) reparto, (3) baja, (4) incorporación de réplica y (5) restauración. No es suficiente ejecutar tres veces la consulta `curl` ni compartir el mismo registro de una prueba.

En **PowerShell** (Windows):

```powershell
py -3 scripts/repeticiones.py
```

En Linux, macOS o WSL:

```bash
python3 scripts/repeticiones.py
# o: bash scripts/repeticiones.sh
```

El programa detiene el despliegue anterior entre rondas y ejecuta **3 ciclos completos**, comprueba que el identificador del contenedor NGINX no cambia **dentro** de cada ciclo y valida las dos rutas en todos los escenarios. Escribe únicamente resultados **observados**, con fecha UTC y comandos, en:

- `docs/evidencias/repeticion-1.md`
- `docs/evidencias/repeticion-2.md`
- `docs/evidencias/repeticion-3.md`

Si alguna comprobación falla, el script interrumpe la ejecución y registra un **FALLO**, sin fabricar un PASS. Los tres ciclos usan la misma topología y nombres, sin variación opcional (el enunciado permite, pero no exige, introducirla). Con `--port 8090` se puede elegir otro puerto público al lanzar **una ejecución de las tres repeticiones**.

Después de ejecutar realmente las tres repeticiones, **registra el proceso en commits identificables**. Ejemplo (no ejecutar hasta que existan las evidencias reales):

```bash
git add docs/evidencias/repeticion-1.md
git commit -m "test: kata5 repeticion 1 balanceo baja alta"
git add docs/evidencias/repeticion-2.md
git commit -m "test: kata5 repeticion 2 balanceo baja alta"
git add docs/evidencias/repeticion-3.md
git commit -m "test: kata5 repeticion 3 balanceo baja alta"
```

La historia de desarrollo ya contiene commits de estructura, implementación, verificadores y documentación; **no sustituye** estos tres commits de las repeticiones ejecutadas. Es conveniente grabar fragmentos de al menos una ejecución completa para el vídeo y enseñar el historial real de los tres ciclos.

## 8. Pruebas automatizadas y evidencias

```bash
# JUnit 5: lógica de dominio pura y configuración de infraestructura
mvn -B clean test

# Pruebas unitarias del verificador con un servidor HTTP simulado:
python3 -m unittest discover -s scripts -p 'test_*.py' -v

# Verificación de integración REAL (requiere Docker)
python3 scripts/verify.py --scenario baseline
python3 scripts/repeticiones.py
```

**Diferencia fundamental:** las pruebas JUnit y las del servidor simulado verifican algoritmos e instrumentos de prueba; **no demuestran** que NGINX/Consul funcionen juntos. La prueba de integración real es `verify.py` contra NGINX durante `repeticiones.py`, y sus tres evidencias documentadas deben adjuntarse al repositorio después de ejecutarlas en Docker. El caso de corte de todas las réplicas del mismo grupo no es requisito funcional; si ocurre, el proxy no puede responder exitosamente (es normal recibir `502/503`).

## 9. Detener, limpiar y volver a comenzar

```bash
# Apagar también la réplica opcional del perfil expansion
 docker compose --profile expansion down -v --remove-orphans
```

No guardar archivos `target`, `.jar`, `.class`, `.idea`, credenciales ni rutas personales. `.gitignore` los excluye. El repositorio debe llamarse **`kata5`**, ser **público** y tener su versión final evaluable en la rama **`main`**.

Al crear el repositorio vacío en GitHub, SIN inicializarlo con otro README (si ya tienes este proyecto local):

```bash
git remote add origin https://github.com/Enrickq/kata5.git
git branch -M main
git push -u origin main
```

Si el repositorio ya contiene un remoto, comprueba `git remote -v` antes de añadirlo. No reutilices ni sobrescribas `kata4` ni `kata3`; son entregas independientes.

## 10. Vídeo (máximo 7 minutos) y criterios de entrega

Graba la pantalla explicando **por qué** hay dos upstreams, `zone`, registros DNS SRV, `resolve` y `resolver`, qué hace Consul con `/health`, cómo se identifica la réplica por `X-Node`, qué cambia sin tocar NGINX, y cómo quedan demostradas las **tres repeticiones** en Git. Enseña las verificaciones reales, no solo una explicación verbal. Guion completo: [`docs/guion-video.md`](docs/guion-video.md). Al subir el vídeo a YouTube, reemplaza el marcador al inicio de este README por su URL real y comprueba que se puede abrir.

Matriz requisito → archivo → prueba: [`docs/matriz-rubrica.md`](docs/matriz-rubrica.md).

Checklist final: dominio aislado ✅; dos rutas/upstreams configurados ✅; 2+2 réplicas definidas ✅; Consul + health + DNS SRV configurados ✅; cambio dinámico sin edición de NGINX implementado ✅; cliente Java y script de prueba ✅; tres ejecuciones y evidencias reales **pendientes hasta ejecutarse**; vídeo público **pendiente**; publicación GitHub **pendiente**. Los apartados marcados como configurados no constituyen una certificación de ejecución real del stack.

## Referencias técnicas

- [NGINX: módulo upstream HTTP (`server service=... resolve`, `zone`, `resolver`)](https://nginx.org/en/docs/http/ngx_http_upstream_module.html)
- [Consul: consulta DNS y registros SRV](https://developer.hashicorp.com/consul/docs/discover/service/static)
- [Consul: configuración de `only_passing` y TTL](https://developer.hashicorp.com/consul/docs/reference/agent/configuration-file/dns)
- [Javalin: documentación](https://javalin.io/documentation)
