package chomsky.nivel3;

import chomsky.common.Result;
import java.util.*;


public class DFA {
    public final List<Character> alphabet = new ArrayList<>();
    public final List<String> states = new ArrayList<>();
    public final Set<String> accepting = new LinkedHashSet<>();
    public final Map<String, Map<Character, String>> delta = new LinkedHashMap<>();
    public String start;

    public String next(String state, char symbol) {
        Map<Character, String> row = delta.get(state);
        if (row == null) {
            return null;
        }
        return row.get(symbol);
    }

    private void addState(String s) {
        if (!states.contains(s)) {
            states.add(s);
        }
    }

    public static DFA parse(String text) {
        DFA d = new DFA();
        List<String[]> transitions = new ArrayList<>();
        List<String> finals = new ArrayList<>();
        int lineNo = 0;
        for (String raw : text.split("\\R")) {
            lineNo++;
            String line = raw;
            int hash = line.indexOf('#');
            if (hash >= 0) {
                line = line.substring(0, hash);
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String low = line.toLowerCase();
            if (low.startsWith("alfabeto:")) {
                for (String s : line.substring(9).split(",")) {
                    String sym = s.trim();
                    if (sym.length() != 1) {
                        throw new IllegalArgumentException("Línea " + lineNo + ": cada símbolo del alfabeto debe ser un carácter.");
                    }
                    if (!d.alphabet.contains(sym.charAt(0))) {
                        d.alphabet.add(sym.charAt(0));
                    }
                }
            } else if (low.startsWith("inicial:")) {
                d.start = line.substring(8).trim();
            } else if (low.startsWith("finales:")) {
                for (String s : line.substring(8).split(",")) {
                    if (!s.trim().isEmpty()) {
                        finals.add(s.trim());
                    }
                }
            } else {
                String[] parts = line.split(",");
                if (parts.length != 3 || parts[1].trim().length() != 1) {
                    throw new IllegalArgumentException("Línea " + lineNo + ": use el formato  estado,símbolo,estado_destino");
                }
                transitions.add(new String[]{parts[0].trim(), parts[1].trim(), parts[2].trim()});
            }
        }
        if (d.alphabet.isEmpty()) {
            throw new IllegalArgumentException("Falta la línea  alfabeto: a,b,...");
        }
        if (d.start == null || d.start.isEmpty()) {
            throw new IllegalArgumentException("Falta la línea  inicial: q0");
        }
        d.addState(d.start);
        for (String[] t : transitions) {
            d.addState(t[0]);
            d.addState(t[2]);
        }
        for (String f : finals) {
            d.addState(f);
            d.accepting.add(f);
        }
        for (String[] t : transitions) {
            char sym = t[1].charAt(0);
            if (!d.alphabet.contains(sym)) {
                throw new IllegalArgumentException("El símbolo '" + sym + "' de la transición " + t[0] + "," + sym + "," + t[2] + " no está en el alfabeto.");
            }
            Map<Character, String> row = d.delta.computeIfAbsent(t[0], k -> new LinkedHashMap<>());
            if (row.containsKey(sym)) {
                throw new IllegalArgumentException("Autómata no determinista: hay dos transiciones desde " + t[0] + " con '" + sym + "'.");
            }
            row.put(sym, t[2]);
        }
        return d;
    }

    public Simulation newSimulation(String input) {
        return new Simulation(input);
    }

    /** Ejecución paso a paso de una cadena sobre el autómata. */
    public class Simulation {
        public final String input;
        public String state;
        public int pos;
        public String lastFrom;
        public String lastTo;
        public String stepInfo = "";
        public String message = "";
        public Result result = Result.RUNNING;

        Simulation(String input) {
            this.input = input;
            this.state = start;
        }

        public Result step() {
            if (result != Result.RUNNING) {
                return result;
            }
            stepInfo = "";
            if (pos >= input.length()) {
                finish();
                return result;
            }
            char c = input.charAt(pos);
            if (!alphabet.contains(c)) {
                result = Result.REJECTED;
                message = "el símbolo '" + c + "' no pertenece al alfabeto";
                return result;
            }
            String to = next(state, c);
            if (to == null) {
                result = Result.REJECTED;
                message = "no existe transición desde " + state + " con '" + c + "'";
                return result;
            }
            lastFrom = state;
            lastTo = to;
            stepInfo = "δ(" + state + ", " + c + ") = " + to;
            state = to;
            pos++;
            if (pos >= input.length()) {
                finish();
            }
            return result;
        }

        private void finish() {
            if (accepting.contains(state)) {
                result = Result.ACCEPTED;
                message = "terminó en el estado final " + state;
            } else {
                result = Result.REJECTED;
                message = "terminó en " + state + ", que no es un estado final";
            }
        }
    }
}
