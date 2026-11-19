package es.ulpgc.kata5;

import es.ulpgc.kata5.application.*;
import es.ulpgc.kata5.domain.*;
import es.ulpgc.kata5.infrastructure.consul.ConsulRegistrar;
import es.ulpgc.kata5.infrastructure.http.HttpApi;

public final class Main {
    public static void main(String[] args) throws Exception {
        var config = ServiceConfiguration.from(System.getenv());
        CalculationUseCase useCase = config.serviceName().equals("fibonacci")
                ? new FibonacciUseCase(new FibonacciSequence())
                : new PrimesUseCase(new PrimeNumbers());
        var app = new HttpApi(config, useCase).start();
        var registrar = new ConsulRegistrar(config);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            registrar.close();
            app.stop();
        }, "graceful-shutdown"));
        registrar.start();
        System.out.printf("Nodo %s (%s) escuchando en :%d / Consul en %s%n",
                config.nodeId(), config.serviceName(), config.port(), config.consulUrl());
    }
}
