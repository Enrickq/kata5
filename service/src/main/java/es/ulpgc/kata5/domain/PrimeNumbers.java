package es.ulpgc.kata5.domain;

import java.util.ArrayList;
import java.util.List;

/** Lógica de dominio pura, independiente del transporte HTTP. */
public final class PrimeNumbers {
    public static final int MAX_LIMIT = 10_000;

    public List<Integer> upTo(int limit) {
        if (limit < 2 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit debe estar entre 2 y " + MAX_LIMIT);
        }
        var result = new ArrayList<Integer>();
        for (int number = 2; number <= limit; number++) {
            if (isPrime(number)) result.add(number);
        }
        return List.copyOf(result);
    }

    private boolean isPrime(int number) {
        for (int divisor = 2; divisor <= number / divisor; divisor++) {
            if (number % divisor == 0) return false;
        }
        return true;
    }
}
