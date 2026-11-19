package es.ulpgc.kata5.infrastructure.http;

import com.google.gson.Gson;
import es.ulpgc.kata5.application.CalculationUseCase;
import es.ulpgc.kata5.application.ServiceConfiguration;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;

/** Adaptador de entrada: traduce HTTP y delega en el caso de uso. */
public final class HttpApi {
    private final ServiceConfiguration config;
    private final CalculationUseCase useCase;
    private final Gson gson = new Gson();

    public HttpApi(ServiceConfiguration config, CalculationUseCase useCase) {
        this.config = config;
        this.useCase = useCase;
    }

    public Javalin start() {
        var app = Javalin.create(javalin -> javalin.showJavalinBanner = false);
        app.get("/health", ctx -> ctx.contentType("application/json")
                .result(gson.toJson(Map.of("status", "UP", "node", config.nodeId()))));
        app.get(useCase.route(), this::calculate);
        app.start("0.0.0.0", config.port());
        return app;
    }

    private void calculate(Context ctx) {
        ctx.header("X-Node", config.nodeId());
        ctx.header("X-Service", config.serviceName());
        ctx.contentType("application/json");
        String key = config.serviceName().equals("fibonacci") ? "n" : "limit";
        String raw = ctx.queryParam(key);
        if (raw == null || !raw.matches("[0-9]+")) {
            badRequest(ctx, "El parámetro " + key + " debe ser un entero positivo");
            return;
        }
        try {
            ctx.result(gson.toJson(useCase.calculate(Integer.parseInt(raw))));
        } catch (IllegalArgumentException exception) {
            badRequest(ctx, exception.getMessage());
        }
    }

    private void badRequest(Context ctx, String message) {
        ctx.status(400).result(gson.toJson(Map.of("error", message)));
    }
}
