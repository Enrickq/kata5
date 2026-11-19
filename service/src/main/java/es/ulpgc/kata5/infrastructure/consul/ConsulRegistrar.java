package es.ulpgc.kata5.infrastructure.consul;

import com.google.gson.Gson;
import es.ulpgc.kata5.config.ServiceConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Adaptador de salida: alta, reintento y baja en Consul sin bloquear el hilo de Javalin. */
public final class ConsulRegistrar implements AutoCloseable {
    private final ServiceConfiguration config;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "consul-registration");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Gson gson = new Gson();

    public ConsulRegistrar(ServiceConfiguration config) { this.config = config; }

    public void start() { scheduleRegistration(0); }

    private void scheduleRegistration(long seconds) {
        if (!closed.get()) scheduler.schedule(this::register, seconds, TimeUnit.SECONDS);
    }

    private void register() {
        if (closed.get()) return;
        String address = config.advertisedAddress();
        String checkUrl = "http://" + address + ":" + config.port() + "/health";
        var check = Map.of("HTTP", checkUrl, "Interval", "2s", "Timeout", "1s",
                "DeregisterCriticalServiceAfter", "1m");
        var payload = Map.of("ID", config.nodeId(), "Name", config.serviceName(),
                "Address", address, "Port", config.port(), "Check", check);
        var request = HttpRequest.newBuilder(endpoint("/v1/agent/service/register"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, failure) -> {
                    if (closed.get()) return;
                    if (failure != null || response.statusCode() / 100 != 2) {
                        String detail = failure == null
                                ? "HTTP " + response.statusCode() + " " + response.body()
                                : failure.toString();
                        System.err.println("Consul no disponible para " + config.nodeId()
                                + ": " + detail + " (reintento en 2 s)");
                        scheduleRegistration(2);
                        return;
                    }
                    System.out.printf("Consul: %s registrado como %s -> %s:%d%n",
                            config.nodeId(), config.serviceName(), address, config.port());
                    // Verifica periódicamente si Consul sigue conservando el registro.
                    // Evita re-registrar innecesariamente y reiniciar el estado del check.
                    scheduleProbe(30);
                });
    }

    private void scheduleProbe(long seconds) {
        if (!closed.get()) scheduler.schedule(this::probe, seconds, TimeUnit.SECONDS);
    }

    private void probe() {
        if (closed.get()) return;
        var request = HttpRequest.newBuilder(endpoint(
                "/v1/agent/service/" + config.nodeId()))
                .timeout(Duration.ofSeconds(5)).GET().build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .whenComplete((response, failure) -> {
                    if (closed.get()) return;
                    if (failure == null && response.statusCode() == 200) scheduleProbe(30);
                    else scheduleRegistration(2);
                });
    }

    private URI endpoint(String path) {
        return URI.create(config.consulUrl()).resolve(path);
    }

    @Override public void close() {
        if (!closed.compareAndSet(false, true)) return;
        scheduler.shutdownNow();
        var request = HttpRequest.newBuilder(endpoint(
                "/v1/agent/service/deregister/" + config.nodeId()))
                .timeout(Duration.ofSeconds(3))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            // Solo el cierre espera, con límite: las peticiones de trabajo son asíncronas.
            var response = client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .orTimeout(3, TimeUnit.SECONDS).join();
            System.out.println("Consul: baja de " + config.nodeId() + " (HTTP "
                    + response.statusCode() + ")");
        } catch (RuntimeException e) {
            System.err.println("Consul: baja no confirmada; /health detectará la caída: " + e);
        } finally {
            client.close();
        }
    }
}
