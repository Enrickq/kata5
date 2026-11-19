package es.ulpgc.kata5.domain;

import java.util.ArrayList;
import java.util.List;

/** Lógica de dominio pura: no depende de HTTP, Javalin ni Gson. */
public final class FibonacciSequence {
    public static final int MAX_TERMS = 90;

    public List<Long> first(int terms) {
        if (terms < 1 || terms > MAX_TERMS) {
            throw new IllegalArgumentException("n debe estar entre 1 y " + MAX_TERMS);
        }
        var result = new ArrayList<Long>(terms);
        long current = 0;
        long next = 1;
        for (int index = 0; index < terms; index++) {
            result.add(current);
            long following = current + next;
            current = next;
            next = following;
        }
        return List.copyOf(result);
    }
}
