package chomsky.nivel1;

import chomsky.common.TapeEntry;
import chomsky.common.TapeMachine;

import java.util.List;

/**
 * Autómatas linealmente acotados (lenguajes sensibles al contexto, Nivel 1).
 * Los lenguajes aⁿbⁿcⁿ y aⁿbⁿcⁿdⁿ NO son libres de contexto, pero se reconocen usando solo
 * la cinta que ocupa la entrada, delimitada por los marcadores '[' y ']'.
 * <p>
 * Estrategia: en cada pasada se marca una 'a' (X), una 'b' (Y), una 'c' (Z) y, si hay, una 'd' (W);
 * el cabezal vuelve a la izquierda hasta la última X y se repite. Al terminar se verifica que no
 * quede ningún símbolo sin marcar.
 */
public final class LBACatalog {
    private static final String MARKS = "XYZW";

    private LBACatalog() { }

    private static TapeMachine build(String letters) {
        int k = letters.length();
        String ret = "q" + (k + 1);
        String ver = "q" + (k + 2);
        TapeMachine tm = new TapeMachine("q0", "qa", true, letters);
        tm.add("q0", '[', "q1", '[', 'R');
        tm.add("q1", letters.charAt(0), "q2", MARKS.charAt(0), 'R');
        tm.add("q1", MARKS.charAt(1), ver, MARKS.charAt(1), 'R');
        tm.add("q1", ']', "qa", ']', 'S');
        for (int i = 1; i < k; i++) {
            String f = "q" + (i + 1);
            tm.add(f, letters.charAt(i - 1), f, letters.charAt(i - 1), 'R');
            tm.add(f, MARKS.charAt(i), f, MARKS.charAt(i), 'R');
            if (i < k - 1) {
                tm.add(f, letters.charAt(i), "q" + (i + 2), MARKS.charAt(i), 'R');
            } else {
                tm.add(f, letters.charAt(i), ret, MARKS.charAt(i), 'L');
            }
        }
        StringBuilder back = new StringBuilder(letters);
        for (int i = 1; i < k; i++) {
            back.append(MARKS.charAt(i));
        }
        tm.skip(ret, back.toString(), 'L');
        tm.add(ret, MARKS.charAt(0), "q1", MARKS.charAt(0), 'R');
        StringBuilder marked = new StringBuilder();
        for (int i = 1; i < k; i++) {
            marked.append(MARKS.charAt(i));
        }
        tm.skip(ver, marked.toString(), 'R');
        tm.add(ver, ']', "qa", ']', 'S');
        return tm;
    }

    public static final List<TapeEntry> ENTRIES = List.of(
            new TapeEntry("aⁿbⁿcⁿ  (n ≥ 0)", build("abc"), false,
                    new String[]{"aabbcc", "abc", "ε", "aaabbbccc", "aabbc", "abcc", "acb", "ba", "abcabc"},
                    new String[]{"si", "si", "si", "si", "no", "no", "no", "no", "no"}),
            new TapeEntry("aⁿbⁿcⁿdⁿ  (n ≥ 0)", build("abcd"), false,
                    new String[]{"aabbccdd", "abcd", "ε", "aabbccd", "abcdd", "abdc"},
                    new String[]{"si", "si", "si", "no", "no", "no"})
    );
}
