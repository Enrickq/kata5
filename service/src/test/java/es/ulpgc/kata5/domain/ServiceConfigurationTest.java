package es.ulpgc.kata5.domain;

import es.ulpgc.kata5.config.ServiceConfiguration;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ServiceConfigurationTest {
    @Test void mapsEnvironmentParameters() throws Exception {
        var config = ServiceConfiguration.from(Map.of(
                "SERVICE_NAME", "primes", "NODE_ID", "prime-2", "PORT", "8080",
                "ADVERTISE_ADDRESS", "172.20.0.8", "CONSUL_URL", "http://consul:8500"));
        assertEquals("prime-2", config.nodeId());
        assertEquals("172.20.0.8", config.advertisedAddress());
    }
    @Test void rejectsInvalidService() {
        assertThrows(IllegalArgumentException.class, () -> ServiceConfiguration.from(Map.of(
                "SERVICE_NAME", "unknown", "ADVERTISE_ADDRESS", "127.0.0.1")));
    }
    @Test void rejectsInvalidPort() {
        assertThrows(IllegalArgumentException.class, () -> ServiceConfiguration.from(Map.of(
                "PORT", "70000", "ADVERTISE_ADDRESS", "127.0.0.1")));
    }
}
