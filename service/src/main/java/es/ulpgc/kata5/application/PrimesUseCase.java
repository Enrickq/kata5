package es.ulpgc.kata5.application;

import es.ulpgc.kata5.domain.PrimeNumbers;
import java.util.List;

public final class PrimesUseCase implements CalculationUseCase {
    private final PrimeNumbers domain;
    public PrimesUseCase(PrimeNumbers domain) { this.domain = domain; }
    @Override public String route() { return "/api/v1/primes"; }
    @Override public Object calculate(int limit) {
        List<Integer> numbers = domain.upTo(limit);
        return new Result(limit, numbers.size(), numbers);
    }
    public record Result(int limit, int count, List<Integer> numbers) { }
}
