package chomsky.nivel2;

import java.util.List;

/** Lenguajes libres de contexto predeterminados del Nivel 2 y sus cadenas de prueba. */
public final class PDACatalog {

    public record Entry(String name, PDA pda, String[] examples, boolean[] expected) { }

    private static PDA anbn() {
        PDA p = new PDA("q0", "qf");
        p.add("q0", 'a', '$', "q0", "A$");
        p.add("q0", 'a', 'A', "q0", "AA");
        p.add("q0", 'b', 'A', "q1", "");
        p.add("q1", 'b', 'A', "q1", "");
        p.add("q0", 'ε', '$', "qf", "$");
        p.add("q1", 'ε', '$', "qf", "$");
        return p;
    }

    private static PDA wcwr() {
        PDA p = new PDA("q0", "qf");
        char[] tops = {'$', 'a', 'b'};
        for (char top : tops) {
            p.add("q0", 'a', top, "q0", "a" + top);
            p.add("q0", 'b', top, "q0", "b" + top);
            p.add("q0", 'c', top, "q1", String.valueOf(top));
        }
        p.add("q1", 'a', 'a', "q1", "");
        p.add("q1", 'b', 'b', "q1", "");
        p.add("q1", 'ε', '$', "qf", "$");
        return p;
    }

    private static PDA parens() {
        PDA p = new PDA("q0", "qf");
        char[] tops = {'$', '(', '['};
        for (char top : tops) {
            p.add("q0", '(', top, "q0", "(" + top);
            p.add("q0", '[', top, "q0", "[" + top);
        }
        p.add("q0", ')', '(', "q0", "");
        p.add("q0", ']', '[', "q0", "");
        p.add("q0", 'ε', '$', "qf", "$");
        return p;
    }

    private static PDA palindromes() {
        PDA p = new PDA("q0", "qf");
        char[] tops = {'$', 'a', 'b'};
        for (char top : tops) {
            p.add("q0", 'a', top, "q0", "a" + top);
            p.add("q0", 'b', top, "q0", "b" + top);
            p.add("q0", 'ε', top, "q1", String.valueOf(top));
        }
        p.add("q1", 'a', 'a', "q1", "");
        p.add("q1", 'b', 'b', "q1", "");
        p.add("q1", 'ε', '$', "qf", "$");
        return p;
    }

    public static final List<Entry> ENTRIES = List.of(
            new Entry("aⁿbⁿ  (n ≥ 0)", anbn(),
                    new String[]{"aabb", "ab", "ε", "aaabbb", "aab", "abb", "ba", "abab"},
                    new boolean[]{true, true, true, true, false, false, false, false}),
            new Entry("w c wᴿ  (w ∈ {a,b}*)", wcwr(),
                    new String[]{"abcba", "c", "aacaa", "abbcbba", "abcab", "abc", "ab"},
                    new boolean[]{true, true, true, true, false, false, false}),
            new Entry("Paréntesis balanceados ( ) [ ]", parens(),
                    new String[]{"(())", "([])", "()[]()", "ε", "(()", "([)]", ")("},
                    new boolean[]{true, true, true, true, false, false, false}),
            new Entry("Palíndromos pares w wᴿ  (no determinista)", palindromes(),
                    new String[]{"abba", "aa", "abbbba", "ε", "aba", "abab", "ab"},
                    new boolean[]{true, true, true, true, false, false, false})
    );

    private PDACatalog() { }
}
