package es.ulpgc.kata5.application;

import java.util.List;

/** Puerto de entrada a la operación de dominio (OCP: más variantes sin cambiar HTTP). */
public interface CalculationUseCase {
    Object calculate(int parameter);
}
