package chomsky.nivel3;

import java.util.List;

/** Lenguajes regulares predeterminados del Nivel 3 y sus cadenas de prueba. */
public final class DFACatalog {

    public record Entry(String name, String definition, String[] examples, boolean[] expected) { }

    public static final String CUSTOM_TEMPLATE =
            "# Escribe aquí tu propio AFD (el formato es el mismo de los ejemplos)\n"
                    + "alfabeto: a,b\n"
                    + "inicial: s0\n"
                    + "finales: s1\n"
                    + "s0,a,s1\n"
                    + "s0,b,s0\n"
                    + "s1,a,s1\n"
                    + "s1,b,s0\n"
                    + "# Este ejemplo acepta las cadenas que terminan en 'a'\n";

    public static final List<Entry> ENTRIES = List.of(
            new Entry("Termina en \"ab\"  (Σ = {a,b})",
                    "# L = { w ∈ {a,b}* | w termina en ab }\n"
                            + "alfabeto: a,b\n"
                            + "inicial: q0\n"
                            + "finales: q2\n"
                            + "q0,a,q1\n"
                            + "q0,b,q0\n"
                            + "q1,a,q1\n"
                            + "q1,b,q2\n"
                            + "q2,a,q1\n"
                            + "q2,b,q0\n",
                    new String[]{"ab", "aab", "bbab", "abab", "ε", "aba", "ba", "abb"},
                    new boolean[]{true, true, true, true, false, false, false, false}),
            new Entry("Número par de ceros  (Σ = {0,1})",
                    "# L = { w ∈ {0,1}* | w tiene un número par de 0 }\n"
                            + "alfabeto: 0,1\n"
                            + "inicial: par\n"
                            + "finales: par\n"
                            + "par,0,impar\n"
                            + "par,1,par\n"
                            + "impar,0,par\n"
                            + "impar,1,impar\n",
                    new String[]{"1100", "ε", "111", "0101", "1001", "0", "1000"},
                    new boolean[]{true, true, true, true, true, false, false}),
            new Entry("(ab)*  (Σ = {a,b})",
                    "# L = (ab)*  — el estado qe es el estado trampa\n"
                            + "alfabeto: a,b\n"
                            + "inicial: q0\n"
                            + "finales: q0\n"
                            + "q0,a,q1\n"
                            + "q0,b,qe\n"
                            + "q1,a,qe\n"
                            + "q1,b,q0\n"
                            + "qe,a,qe\n"
                            + "qe,b,qe\n",
                    new String[]{"abab", "ab", "ε", "ababab", "aba", "ba", "abba"},
                    new boolean[]{true, true, true, true, false, false, false}),
            new Entry("Binarios múltiplos de 3  (Σ = {0,1})",
                    "# L = { w ∈ {0,1}* | w, leída como número binario, es múltiplo de 3 }\n"
                            + "# El estado r_i guarda el residuo módulo 3 del valor leído\n"
                            + "alfabeto: 0,1\n"
                            + "inicial: r0\n"
                            + "finales: r0\n"
                            + "r0,0,r0\n"
                            + "r0,1,r1\n"
                            + "r1,0,r2\n"
                            + "r1,1,r0\n"
                            + "r2,0,r1\n"
                            + "r2,1,r2\n",
                    new String[]{"110", "1001", "1111", "11", "101", "100", "10"},
                    new boolean[]{true, true, true, true, false, false, false}),
            new Entry("Personalizado (editable)",
                    CUSTOM_TEMPLATE,
                    new String[]{"a", "ba", "abab", "ab", "ε"},
                    new boolean[]{true, true, false, false, false})
    );

    private DFACatalog() { }
}
