package chomsky.nivel0;

import chomsky.common.TapeEntry;
import chomsky.common.TapeMachine;

import java.util.List;

/** Máquinas de Turing del Nivel 0: un reconocedor y dos funciones aritméticas sencillas. */
public final class TMCatalog {
    private TMCatalog() { }

    /** Acepta aⁿbⁿ marcando una 'a' (X) y una 'b' (Y) por pasada. */
    private static TapeMachine anbn() {
        TapeMachine tm = new TapeMachine("q0", "qa", false, "ab");
        tm.add("q0", 'a', "q1", 'X', 'R');
        tm.add("q0", 'Y', "q3", 'Y', 'R');
        tm.add("q0", '_', "qa", '_', 'S');
        tm.skip("q1", "aY", 'R');
        tm.add("q1", 'b', "q2", 'Y', 'L');
        tm.skip("q2", "aY", 'L');
        tm.add("q2", 'X', "q0", 'X', 'R');
        tm.add("q3", 'Y', "q3", 'Y', 'R');
        tm.add("q3", '_', "qa", '_', 'S');
        return tm;
    }

    /** Suma en unario: 1^m + 1^n  →  1^(m+n). Convierte '+' en '1' y borra el último '1'. */
    private static TapeMachine unaryAdd() {
        TapeMachine tm = new TapeMachine("q0", "qa", false, "1+");
        tm.skip("q0", "1", 'R');
        tm.add("q0", '+', "q1", '1', 'R');
        tm.skip("q1", "1", 'R');
        tm.add("q1", '_', "q2", '_', 'L');
        tm.add("q2", '1', "qa", '_', 'S');
        return tm;
    }

    /** Incremento de un número binario; si hay acarreo final la cinta crece hacia la izquierda. */
    private static TapeMachine binaryIncrement() {
        TapeMachine tm = new TapeMachine("q0", "qa", false, "01");
        tm.skip("q0", "01", 'R');
        tm.add("q0", '_', "q1", '_', 'L');
        tm.add("q1", '1', "q1", '0', 'L');
        tm.add("q1", '0', "qa", '1', 'S');
        tm.add("q1", '_', "qa", '1', 'S');
        return tm;
    }

    public static final List<TapeEntry> ENTRIES = List.of(
            new TapeEntry("Aceptar aⁿbⁿ  (n ≥ 0)", anbn(), false,
                    new String[]{"aabb", "ab", "ε", "aaabbb", "aab", "abb", "ba", "abab"},
                    new String[]{"si", "si", "si", "si", "no", "no", "no", "no"}),
            new TapeEntry("Suma unaria  1ᵐ + 1ⁿ", unaryAdd(), true,
                    new String[]{"11+111", "1+1", "+11", "111+", "+"},
                    new String[]{"11111", "11", "11", "111", ""}),
            new TapeEntry("Incremento binario  (+1)", binaryIncrement(), true,
                    new String[]{"1011", "111", "0", "1101", "ε"},
                    new String[]{"1100", "1000", "1", "1110", "1"})
    );
}
