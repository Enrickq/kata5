# Guion para vídeo explicativo de Kata 5

**Duración objetivo:** 6:20–6:40 (máximo absoluto 7:00). Grabar pantalla **tras realizar las repeticiones en Docker**. No leer salidas simuladas ni afirmar que se ha probado algo que no se ejecutó.

| Tiempo | Pantalla | Qué explicar y demostrar |
|---|---|---|
| 00:00–00:35 | README / esquema | Objetivo: 2 upstreams y 2 réplicas por upstream, cliente con URL estable, descubrimiento Consul DNS SRV. |
| 00:35–01:20 | `compose.yaml` | NGINX + Consul + 4 Java, quinta réplica solo `expansion`, red Docker y puertos internos/externos. |
| 01:20–02:00 | `infra/nginx.conf` | Mostrar **dos** upstreams y rutas; explicar `zone`, `resolver consul:8600`, `service=... resolve`, TTL DNS, sin reload. |
| 02:00–02:35 | `HttpApi`, `ConsulRegistrar` y dominio | Cabeceras `X-Node`, HTTP `/health`, ID y Name en Consul, validación de entradas y separación dominio/infraestructura. Mencionar `sendAsync`. |
| 02:35–03:15 | Terminal: baseline | `docker compose ps` y `py -3 scripts/verify.py --scenario baseline` mostrando 2 Fibonacci y 2 primos y resultados idénticos. |
| 03:15–04:10 | Terminal: baja | Mostrar NGINX ID, detener `fibonacci-2`, verificar escenario `down` y que los primos no se afectan. |
| 04:10–05:00 | Terminal: alta | `docker compose --profile expansion up -d fibonacci-3`, verificar `up`, mostrar nuevo nodo y comparar ID de NGINX; sin cambio de configuración. |
| 05:00–05:40 | Terminal/Consul/UI | Comprobar Consul health y SRV, explicar retrasos por checks y TTL. Un caso HTTP 400 y su JSON. |
| 05:40–06:20 | Git log / `docs/evidencias` | Mostrar **3 archivos reales** y 3 commits de repetición, decir que se rehízo despliegue/reparto/baja/alta tres veces. Mostrar `mvn test` si fue ejecutado. |
| 06:20–06:40 | README | Resumir diseño, comandos y reproducibilidad. Recalcar que Kata 4 y 3 no se modifican. |

## Grabación y edición

1. Abrir IntelliJ, Docker Desktop y una terminal con el proyecto en su raíz. Usar tamaño de letra legible.
2. Ejecutar **primero** `py -3 scripts/repeticiones.py` (o `python3 ...`) para generar evidencias reales. Crear **tres commits reales** y comprobar la rama `main`.
3. Para grabar los pasos manuales, partir de `docker compose --profile expansion down -v --remove-orphans` y luego `docker compose up -d --build --wait`; dejar los comandos preparados para no perder tiempo.
4. Capturar claramente el resultado `PASS` con `X-Node` por servicio. No usar capturas de consola inventadas.
5. Acortar tiempos muertos de build/descarga/espera, pero no ocultar fallos ni hacer pasar cortes editados por ejecuciones continuas si ello induce a error.
6. Subir vídeo **público o no listado accesible** según las normas del profesor, comprobar que se reproduce, y pegar el enlace real en el README.
7. Revisar duración final **≤ 7:00**, audio entendible y comandos legibles. La pantalla del vídeo debe coincidir con el contenido real de GitHub.

### Frase técnica breve para explicar en la defensa

> "En NGINX, el enrutamiento por ruta elige entre `fibonacci_pool` y `primes_pool`. Dentro de cada upstream, `service=... resolve` consulta registros SRV en el DNS de Consul, que incluyen el destino y puerto de cada réplica sana. Consul verifica `/health`; al detener una réplica, el DNS deja de anunciarla y NGINX la retira cuando vence su caché. El cliente no cambia su URL. Al añadir `fibonacci-3`, aparece automáticamente en el grupo correcto sin recargar NGINX."
