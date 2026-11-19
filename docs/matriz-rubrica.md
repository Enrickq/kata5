# Kata 5 · Requisitos y evidencias

La versión final evaluable está en la rama `main`. El [README](../README.md) contiene las instrucciones para compilar, arrancar y verificar el proyecto. El [vídeo de demostración](https://youtu.be/m_Y3TVgMWnk) dura 6 minutos y 35 segundos.

## Arquitectura y funcionamiento

| Requisito | Implementación | Evidencia |
|---|---|---|
| Dos grupos de servicios con rutas públicas independientes | [nginx.conf](../infra/nginx.conf) dirige `/api/v1/fibonacci` a `fibonacci_pool` y `/api/v1/primes` a `primes_pool`. | [Configuración de NGINX en el vídeo · 01:05](https://youtu.be/m_Y3TVgMWnk?t=65) |
| Al menos dos réplicas por grupo | [compose.yaml](../compose.yaml) define `fibonacci-1`, `fibonacci-2`, `primes-1` y `primes-2`. | En la [prueba inicial · 02:25](https://youtu.be/m_Y3TVgMWnk?t=145) aparecen las cuatro réplicas atendiendo peticiones. |
| Descubrimiento dinámico con NGINX y Consul | Se utiliza NGINX `1.28.0-alpine`. Cada upstream tiene su propia `zone` y consulta los registros DNS SRV de Consul mediante `resolver`, `service` y `resolve`. | [Configuración de NGINX](../infra/nginx.conf) y [configuración DNS de Consul](../infra/consul.json). Los registros de las repeticiones incluyen `nginx -t` y `nginx -v`. |
| Registro y comprobación de salud | Cada instancia registra en Consul su identificador, dirección y puerto. Consul comprueba el endpoint `/health`. | [Registro de instancias en Java](../service/src/main/java/es/ulpgc/kata5/infrastructure/consul/ConsulRegistrar.java) y [vista de servicios en Consul · 04:35](https://youtu.be/m_Y3TVgMWnk?t=275). |
| Baja de una réplica | Se detiene `fibonacci-2`. Las peticiones a Fibonacci continúan a través de `fibonacci-1` y Primes conserva sus dos réplicas. | [Prueba de baja · 03:05](https://youtu.be/m_Y3TVgMWnk?t=185) y escenario `down` en los tres registros. |
| Alta de una nueva réplica | El perfil `expansion` incorpora `fibonacci-3`. NGINX empieza a repartir las peticiones entre `fibonacci-1` y `fibonacci-3` con la misma URL pública. | [Prueba de alta · 03:45](https://youtu.be/m_Y3TVgMWnk?t=225) y escenario `up` en los tres registros. |
| Continuidad de NGINX durante los cambios | La secuencia de baja y alta se realiza sin editar `nginx.conf` ni ejecutar una recarga. Se compara el identificador del contenedor NGINX antes y después. | La comparación devuelve `True` en el vídeo. El [script de repeticiones](../scripts/repeticiones.py) también comprueba que el identificador se mantiene. |
| Verificación de respuestas y aislamiento de los grupos | El [cliente Java](../client/src/main/java/es/ulpgc/kata5/client/Main.java) envía peticiones asíncronas. El [verificador HTTP](../scripts/verify.py) comprueba el JSON, las cabeceras `X-Service` y `X-Node` y las réplicas que responden en cada ruta. | Los escenarios `baseline`, `down` y `up` muestran qué nodos responden en cada grupo. |
| Validación de entradas y pruebas | El servicio devuelve HTTP `400` ante parámetros inválidos. El repositorio incluye [pruebas JUnit](../service/src/test/) y [pruebas del verificador](../scripts/test_verify.py). | En el [bloque de control de errores · 04:35](https://youtu.be/m_Y3TVgMWnk?t=275) se muestra la respuesta a `fibonacci?n=0`. El README incluye los comandos para ejecutar las pruebas. |

## Tres repeticiones completas

En cada repetición se desplegó el entorno, se comprobó el reparto inicial, se detuvo `fibonacci-2`, se incorporó `fibonacci-3` y se restauró el estado inicial. Se mantuvieron los mismos servicios, rutas y condiciones para comparar los resultados.

| Repetición | Registro de ejecución | Resultado final | Commit |
|---|---|---|---|
| 1 | [repeticion-1.md](evidencias/repeticion-1.md) | `PASS` | [`cd31e91`](https://github.com/Enrickq/kata5/commit/cd31e91400fd3dc5c8e3766fd644873a47cf087b) |
| 2 | [repeticion-2.md](evidencias/repeticion-2.md) | `PASS` | [`a16d1a5`](https://github.com/Enrickq/kata5/commit/a16d1a59abc7a49cb677ca0e218d6e6447d52f8b) |
| 3 | [repeticion-3.md](evidencias/repeticion-3.md) | `PASS` | [`b42b172`](https://github.com/Enrickq/kata5/commit/b42b17283590bbdf487d4e6ca89ebcb24ce5db46) |

Las tres evidencias y sus commits se muestran en el [vídeo a partir de 05:10](https://youtu.be/m_Y3TVgMWnk?t=310).