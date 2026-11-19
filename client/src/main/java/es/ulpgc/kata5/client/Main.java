package es.ulpgc.kata5.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Cliente demostrativo con sendAsync, sin bloquear en cada petición. */
public final class Main {
    private static final Map<String, String> ROUTES = Map.of(
            "fibonacci", "/api/v1/fibonacci?n=8",
            "primes", "/api/v1/primes?limit=20");

    public static void main(String[] args) {
        String base = args.length >= 1 ? args[0] : "http://localhost:8080";
        int samples = args.length >= 2 ? Integer.parseInt(args[1]) : 20;
        if (samples < 2 || samples > 500) throw new IllegalArgumentException("samples: 2..500");
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3)).build()) {
            var pending = new ArrayList<CompletableFuture<Sample>>();
            for (var route : ROUTES.entrySet()) {
                for (int index = 0; index < samples; index++) {
                    var request = HttpRequest.newBuilder(URI.create(base + route.getValue()))
                            .timeout(Duration.ofSeconds(5)).GET().build();
                    pending.add(client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApply(response -> parse(route.getKey(), response)));
                }
            }
            // join se usa SOLO al final, después de iniciar todas las peticiones.
            CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).join();
            Map<String, Set<String>> nodes = Map.of("fibonacci", new HashSet<>(), "primes", new HashSet<>());
            for (var task : pending) {
                var sample = task.join();
                nodes.get(sample.service()).add(sample.node());
                System.out.printf("%-10s | %-12s | HTTP 200 | %s%n",
                        sample.service(), sample.node(), sample.body());
            }
            nodes.forEach((name, replicas) -> System.out.println(name + " -> réplicas: " + replicas));
        }
    }

    static Sample parse(String expectedService, HttpResponse<String> response) {
        if (response.statusCode() != 200)
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        String service = response.headers().firstValue("X-Service").orElseThrow();
        String node = response.headers().firstValue("X-Node").orElseThrow();
        if (!expectedService.equals(service))
            throw new IllegalStateException("Enrutamiento incorrecto: " + service + " en " + expectedService);
        JsonElement body = JsonParser.parseString(response.body());
        if (!body.isJsonObject()) throw new IllegalStateException("Respuesta JSON no es objeto");
        return new Sample(service, node, body);
    }

    record Sample(String service, String node, JsonElement body) { }
}
