# Matriz de trazabilidad — enunciado ACTUALIZADO Kata 5

Distinguir entre **implementación preparada** y **verificación real ejecutada**. No confundir el cumplimiento estático con un resultado de Docker.

| Requisito textual | Evidencia en el repositorio | Pendiente de demostrar en ejecución |
|---|---|---|
| Dos `upstream`, cada uno con ruta pública | `infra/nginx.conf`: `fibonacci_pool` + `primes_pool` | `verify.py --scenario baseline` |
| Descubrimiento `zone`, `resolver`, `service`, `resolve`, DNS SRV | `infra/nginx.conf`, `infra/consul.json` | `nginx -t`, consulta DNS SRV y prueba de cambio |
| NGINX libre ≥ 1.27.3 | `compose.yaml`: `nginx:1.28.0-alpine` | `docker compose exec -T nginx nginx -v` |
| ≥2 réplicas por upstream | `compose.yaml`: cuatro instancias iniciales con nombre y ID únicos | `verify.py` identifica las cuatro |
| Registro en Consul de dirección y puerto alcanzables | `ConsulRegistrar` anuncia dirección IPv4 del contenedor | API `/v1/health/service/...?passing=1` |
| HTTP /health por réplica; control de salud | `HttpApi`, `ConsulRegistrar`, `infra/consul.json` | Consul UI / catálogo y baja |
| Eliminar una réplica sin editar o recargar NGINX | `compose stop fibonacci-2`, `service=... resolve` | `--scenario down` con NGINX ID constante |
| Incorporar nueva réplica sin editar o recargar NGINX | perfil `expansion`: `fibonacci-3` | `--scenario up` con NGINX ID constante |
| Independencia de las rutas y cálculos consistentes | Casos de uso distintos en dominio y en NGINX | Comparación HTTP entre réplicas de cada pool |
| Cliente o script de múltiples consultas y X-Node | `client/Main.java`, `scripts/verify.py` | Ejecutar y mostrar salidas por réplica |
| Repetir el mismo despliegue tres veces | `scripts/repeticiones.py` | **Ejecutar de verdad** y registrar las 3 evidencias y 3 commits |
| Tests, entradas inválidas y bordes | `service/src/test`, `scripts/test_verify.py`, `verify.py` | Ejecutar Maven/JUnit e integración Docker |
| README versiones, puertos, topología, comandos y reproducibilidad | `README.md`, este documento | Comprobar ejecución en equipo propio |
| Repo público `kata5`, main, Git, sin binarios | `.gitignore`, historial del proyecto | Publicar `https://github.com/Enrickq/kata5` después de revisar el remoto |
| Vídeo ≤ 7 minutos con decisiones, proceso y pruebas | `docs/guion-video.md` | **Grabar y publicar** vídeo real, añadir URL al README |

## Evidencias generadas en un equipo con Docker

Tras completar `py -3 scripts/repeticiones.py` o `python3 scripts/repeticiones.py`:

- `docs/evidencias/repeticion-1.md`: despliegue, reparto, baja, alta, restauración.
- `docs/evidencias/repeticion-2.md`: misma secuencia de nuevo, datos reales.
- `docs/evidencias/repeticion-3.md`: tercera secuencia, datos reales.

No añadir ejemplos inventados de salidas `PASS`. Cualquier fichero con estado de fallo deberá corregirse mediante una nueva ejecución antes de confirmar una repetición.
