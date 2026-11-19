#!/usr/bin/env python3
"""Comprobación negra vía NGINX (sin conectarse directamente a ningún backend).

Escenarios: baseline (2+2), down (1+2), up (2+2 con la réplica 3).
Cada escenario comprueba respuesta, consistencia, X-Node y ausencia de fugas entre pools.
"""
from __future__ import annotations

import argparse
import collections
import json
import sys
import time
import urllib.error
import urllib.request

EXPECTED_NUMBERS = {
    "fibonacci": [0, 1, 1, 2, 3, 5, 8, 13],
    "primes": [2, 3, 5, 7, 11, 13, 17, 19],
}
PATHS = {
    "fibonacci": "/api/v1/fibonacci?n=8",
    "primes": "/api/v1/primes?limit=20",
}
SCENARIOS = {
    "baseline": {
        "fibonacci": {"fibonacci-1", "fibonacci-2"},
        "primes": {"primes-1", "primes-2"},
    },
    "down": {
        "fibonacci": {"fibonacci-1"},
        "primes": {"primes-1", "primes-2"},
    },
    "up": {
        "fibonacci": {"fibonacci-1", "fibonacci-3"},
        "primes": {"primes-1", "primes-2"},
    },
}


def request(base: str, path: str) -> tuple[int, dict[str, str], dict]:
    url = base.rstrip("/") + path
    try:
        with urllib.request.urlopen(url, timeout=3) as response:
            payload = response.read().decode("utf-8")
            return response.status, dict(response.headers), json.loads(payload)
    except urllib.error.HTTPError as error:
        return error.code, dict(error.headers), json.loads(error.read().decode("utf-8"))


def sample(base: str, scenario: str, count: int) -> dict[str, collections.Counter]:
    actual: dict[str, collections.Counter] = {}
    for service, expected_nodes in SCENARIOS[scenario].items():
        nodes: collections.Counter = collections.Counter()
        responses = []
        for _ in range(count):
            status, headers, body = request(base, PATHS[service])
            if status != 200:
                raise AssertionError(f"{service}: HTTP {status}, body={body}")
            if "application/json" not in headers.get("Content-Type", ""):
                raise AssertionError(f"{service}: Content-Type incorrecto")
            node = headers.get("X-Node")
            server = headers.get("X-Service")
            if not node or server != service:
                raise AssertionError(f"{service}: X-Node={node!r}, X-Service={server!r}")
            if node not in expected_nodes:
                raise AssertionError(f"{service}: réplica inesperada {node!r}")
            if body.get("numbers") != EXPECTED_NUMBERS[service]:
                raise AssertionError(f"{service}: cálculo incorrecto: {body}")
            if service == "fibonacci" and body.get("terms") != 8:
                raise AssertionError("fibonacci: número de términos incorrecto")
            if service == "primes" and (body.get("count") != 8 or body.get("limit") != 20):
                raise AssertionError("primes: resumen incorrecto")
            nodes[node] += 1
            responses.append(body)
        if set(nodes) != expected_nodes:
            raise AssertionError(
                f"{service}: se esperaban réplicas {sorted(expected_nodes)} y respondieron {sorted(nodes)}"
            )
        if any(body != responses[0] for body in responses):
            raise AssertionError(f"{service}: réplicas con resultados distintos")
        actual[service] = nodes
    return actual


def invalid_cases(base: str) -> None:
    for path in (
        "/api/v1/fibonacci?n=0",
        "/api/v1/fibonacci?n=91",
        "/api/v1/fibonacci?n=no",
        "/api/v1/fibonacci",
        "/api/v1/primes?limit=1",
        "/api/v1/primes?limit=10001",
        "/api/v1/primes?limit=hola",
        "/api/v1/primes",
    ):
        status, _, body = request(base, path)
        if status != 400 or not isinstance(body.get("error"), str):
            raise AssertionError(f"Caso inválido {path}: esperaba HTTP 400 + error; vino {status} {body}")


def verify(base: str, scenario: str, count: int, wait: int) -> dict:
    deadline = time.monotonic() + wait
    attempts = 0
    while True:
        attempts += 1
        try:
            nodes = sample(base, scenario, count)
            invalid_cases(base)
            return {name: dict(counter) for name, counter in nodes.items()}
        except (AssertionError, OSError, ValueError, TimeoutError) as error:
            if time.monotonic() >= deadline:
                raise AssertionError(
                    f"Escenario {scenario} sin converger tras {attempts} intentos: {error}"
                ) from error
            if attempts % 5 == 1:
                print(f"  espera de salud/DNS ({scenario}), intento {attempts}: {error}", file=sys.stderr)
            time.sleep(2)


def main() -> None:
    parser = argparse.ArgumentParser(description="Verificar dos upstreams dinámicos de la Kata 5")
    parser.add_argument("--scenario", required=True, choices=sorted(SCENARIOS))
    parser.add_argument("--base", default="http://127.0.0.1:8080")
    parser.add_argument("--samples", type=int, default=24)
    parser.add_argument("--wait", type=int, default=100)
    args = parser.parse_args()
    if args.samples < 8 or args.wait < 0:
        parser.error("--samples >= 8 y --wait >= 0")
    nodes = verify(args.base, args.scenario, args.samples, args.wait)
    print(json.dumps({"scenario": args.scenario, "counts": nodes, "result": "PASS"},
                     ensure_ascii=False, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
