package es.ulpgc.kata5.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class FibonacciSequenceTest {
    private final FibonacciSequence sequence = new FibonacciSequence();

    @Test void generatesKnownSequence() {
        assertEquals(List.of(0L, 1L, 1L, 2L, 3L, 5L, 8L, 13L), sequence.first(8));
    }
    @Test void supportsLowerBoundary() { assertEquals(List.of(0L), sequence.first(1)); }
    @Test void handlesLargestAllowedInputWithoutOverflow() {
        var result = sequence.first(90);
        assertEquals(90, result.size());
        assertEquals(1779979416004714189L, result.getLast().longValue());
    }
    @Test void rejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> sequence.first(0));
        assertThrows(IllegalArgumentException.class, () -> sequence.first(91));
    }
}
