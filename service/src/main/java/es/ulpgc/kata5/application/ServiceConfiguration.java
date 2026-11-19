package es.ulpgc.kata5.application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/** Parámetros de infraestructura fuera de los casos de uso. */
public record ServiceConfiguration(String nodeId, String serviceName, String consulUrl,
                                   String advertisedAddress, int port) {
    public static ServiceConfiguration from(Map<String, String> env) throws UnknownHostException {
        String name = env.getOrDefault("SERVICE_NAME", "fibonacci").strip();
        String id = env.getOrDefault("NODE_ID", "fibonacci-local").strip();
        String consul = env.getOrDefault("CONSUL_URL", "http://localhost:8500").strip();
        String address = env.getOrDefault("ADVERTISE_ADDRESS", "").strip();
        if (address.isEmpty()) address = InetAddress.getLocalHost().getHostAddress();
        int port = Integer.parseInt(env.getOrDefault("PORT", "8080"));
        if (!name.equals("fibonacci") && !name.equals("primes"))
            throw new IllegalArgumentException("SERVICE_NAME debe ser fibonacci o primes");
        if (!id.matches("[a-zA-Z0-9][a-zA-Z0-9-]{0,62}"))
            throw new IllegalArgumentException("NODE_ID debe ser un identificador DNS sencillo");
        if (!consul.startsWith("http://") && !consul.startsWith("https://"))
            throw new IllegalArgumentException("CONSUL_URL debe ser una URL HTTP(S)");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("PORT no válido");
        return new ServiceConfiguration(id, name, consul, address, port);
    }
}
