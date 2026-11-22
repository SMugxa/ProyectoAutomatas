package chomsky.common;

/**
 * Una máquina de cinta lista para simular, con sus cadenas de prueba.
 * <p>
 * Si {@code function} es falso, {@code expected[i]} vale "si" (acepta) o "no" (rechaza).
 * Si es verdadero, {@code expected[i]} es el contenido final esperado de la cinta.
 */
public record TapeEntry(String name, TapeMachine machine, boolean function,
                        String[] examples, String[] expected) { }
