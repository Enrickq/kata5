package es.ulpgc.kata5.application;

import es.ulpgc.kata5.domain.FibonacciSequence;
import java.util.List;

public final class FibonacciUseCase implements CalculationUseCase {
    private final FibonacciSequence domain;
    public FibonacciUseCase(FibonacciSequence domain) { this.domain = domain; }
    @Override public String route() { return "/api/v1/fibonacci"; }
    @Override public Object calculate(int terms) { return new Result(terms, domain.first(terms)); }
    public record Result(int terms, List<Long> numbers) { }
}
