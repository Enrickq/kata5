#!/usr/bin/env python3
"""Tres despliegues REALES, con evidencias Markdown generadas al ejecutarse.

Requiere Docker Compose y Python 3; utilizable desde PowerShell, cmd o Bash.
No crea evidencia PASS si falla un paso ni fabrica resultados.
"""
from __future__ import annotations

import argparse
import datetime
import os
from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / "docs" / "evidencias"


def main() -> int:
    parser = argparse.ArgumentParser(description="Repetir 3 veces la Kata 5 con Docker Compose")
    parser.add_argument("--port", type=int, default=8080, help="Puerto público de NGINX")
    args = parser.parse_args()
    if not 1 <= args.port <= 65535:
        parser.error("--port: rango 1..65535")
    if not shutil.which("docker"):
        parser.error("No se ha encontrado Docker. Instala e inicia Docker Desktop/Engine.")
    EVIDENCE.mkdir(parents=True, exist_ok=True)
    env = os.environ.copy()
    env["PUBLIC_PORT"] = str(args.port)
    base = f"http://127.0.0.1:{args.port}"
    try:
        subprocess.run(["docker", "info"], cwd=ROOT, env=env, check=True,
                       stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
    except (OSError, subprocess.CalledProcessError) as error:
        parser.error(f"Docker no está iniciado: {error}")

    for number in range(1, 4):
        path = EVIDENCE / f"repeticion-{number}.md"
        with path.open("w", encoding="utf-8") as out:
            out.write(f"# Repetición {number}: ejecución real\n\n")
            out.write(f"Fecha UTC: {datetime.datetime.now(datetime.timezone.utc).isoformat()}\n\n")
            out.write("Mismos upstreams, misma configuración, ninguna variación opcional.\n\n")
            out.write("Estado inicial: **EN PROCESO**.\n\n```text\n")
            out.flush()

            def step(*command: str) -> str:
                label = "$ " + " ".join(command)
                print(label, flush=True)
                out.write(label + "\n")
                out.flush()
                result = subprocess.run(command, cwd=ROOT, env=env, text=True,
                                        stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                print(result.stdout, end="", flush=True)
                out.write(result.stdout)
                out.flush()
                if result.returncode:
                    raise RuntimeError(f"Orden fallida ({result.returncode}): {label}")
                return result.stdout.strip()

            def verify(scenario: str) -> None:
                step(sys.executable, "scripts/verify.py", "--scenario", scenario, "--base", base)

            try:
                step("docker", "compose", "--profile", "expansion", "down", "-v", "--remove-orphans")
                step("docker", "compose", "up", "-d", "--build", "--wait")
                step("docker", "compose", "ps")
                step("docker", "compose", "exec", "-T", "nginx", "nginx", "-t")
                step("docker", "compose", "exec", "-T", "nginx", "nginx", "-v")
                nginx_id = step("docker", "compose", "ps", "-q", "nginx")
                if not nginx_id:
                    raise RuntimeError("No se pudo obtener el ID de NGINX")
                verify("baseline")
                step("docker", "compose", "stop", "fibonacci-2")
                verify("down")
                if step("docker", "compose", "ps", "-q", "nginx") != nginx_id:
                    raise RuntimeError("NGINX se recreó durante la baja")
                step("docker", "compose", "--profile", "expansion", "up", "-d", "fibonacci-3")
                verify("up")
                if step("docker", "compose", "ps", "-q", "nginx") != nginx_id:
                    raise RuntimeError("NGINX se recreó durante el alta")
                step("docker", "compose", "--profile", "expansion", "stop", "fibonacci-3")
                step("docker", "compose", "start", "fibonacci-2")
                verify("baseline")
                if step("docker", "compose", "ps", "-q", "nginx") != nginx_id:
                    raise RuntimeError("NGINX se recreó durante la restauración")
            except (RuntimeError, OSError) as error:
                out.write(f"\n```\n\n**FALLO**: {error}. No constituye repetición válida.\n")
                print(f"Error en repetición {number}: {error}", file=sys.stderr)
                print(f"Evidencia incompleta: {path}", file=sys.stderr)
                return 1
            else:
                out.write("\n```\n\n**PASS**: comprobados reparto, baja, alta, independencia ")
                out.write("de upstreams, consistencia y ausencia de reinicios de NGINX.\n")
                print(f"Repetición {number}/3 completada: {path}")
    print("Tres repeticiones completas. Añade los tres archivos a Git mediante commits separados.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
