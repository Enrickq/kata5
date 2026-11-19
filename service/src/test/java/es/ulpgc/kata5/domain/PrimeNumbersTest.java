package es.ulpgc.kata5.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class PrimeNumbersTest {
    private final PrimeNumbers primes = new PrimeNumbers();

    @Test void findsPrimes() {
        assertEquals(List.of(2, 3, 5, 7, 11, 13, 17, 19), primes.upTo(20));
    }
    @Test void supportsLowerBoundary() { assertEquals(List.of(2), primes.upTo(2)); }
    @Test void findsExpectedCountAtUpperBoundary() {
        assertEquals(1229, primes.upTo(10_000).size());
    }
    @Test void rejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> primes.upTo(1));
        assertThrows(IllegalArgumentException.class, () -> primes.upTo(10_001));
    }
}
