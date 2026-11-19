"""Pruebas del verificador contra servidor simulado (NO sustituyen Docker real)."""
from __future__ import annotations

import http.server
import json
from pathlib import Path
import socketserver
import sys
import threading
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parent))
import verify


class Handler(http.server.BaseHTTPRequestHandler):
    scenario = "baseline"
    counters = {"fibonacci": 0, "primes": 0}

    def do_GET(self) -> None:
        service = "fibonacci" if self.path.startswith("/api/v1/fibonacci") else "primes"
        expected = sorted(verify.SCENARIOS[self.scenario][service])
        invalid = "?" not in self.path or any(item in self.path for item in
                                           ("=0", "=91", "=no", "=1", "=10001", "=hola"))
        if invalid:
            body = {"error": "invalid"}
            status = 400
        else:
            status = 200
            body = {"numbers": verify.EXPECTED_NUMBERS[service]}
            body.update({"terms": 8} if service == "fibonacci" else {"count": 8, "limit": 20})
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("X-Service", service)
        self.send_header("X-Node", expected[self.counters[service] % len(expected)])
        self.counters[service] += 1
        self.end_headers()
        self.wfile.write(json.dumps(body).encode())

    def log_message(self, format, *args) -> None:
        pass


class VerifierTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.base = "http://127.0.0.1:" + str(cls.server.server_port)

    @classmethod
    def tearDownClass(cls) -> None:
        cls.server.shutdown()
        cls.server.server_close()

    def test_all_three_scenarios(self) -> None:
        for scenario in ("baseline", "down", "up"):
            Handler.scenario = scenario
            Handler.counters = {"fibonacci": 0, "primes": 0}
            result = verify.verify(self.base, scenario, 12, 0)
            self.assertEqual(set(result["fibonacci"]), verify.SCENARIOS[scenario]["fibonacci"])
            self.assertEqual(set(result["primes"]), verify.SCENARIOS[scenario]["primes"])


if __name__ == "__main__":
    unittest.main()
