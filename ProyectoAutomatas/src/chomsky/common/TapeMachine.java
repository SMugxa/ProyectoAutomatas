package chomsky.common;

import java.util.*;

/**
 * Máquina de Turing determinista de una cinta. Se usa en dos modos:
 * <ul>
 *   <li>Nivel 0 (Máquina de Turing): cinta extensible hacia ambos lados con blancos '_'.</li>
 *   <li>Nivel 1 (Autómata Linealmente Acotado): cinta acotada a la entrada, delimitada por los
 *       marcadores '[' y ']' que el cabezal nunca puede sobrepasar.</li>
 * </ul>
 * Si no existe transición para el par (estado, símbolo) la máquina rechaza.
 */
public class TapeMachine {
    public static final char BLANK = '_';
    public static final int MAX_STEPS = 20000;

    public record Transition(String from, char read, String to, char write, char move) {
        public String describe() {
            return "δ(" + from + ", " + read + ") = (" + to + ", " + write + ", " + move + ")";
        }
    }

    public final String start;
    public final String accept;
    public final boolean bounded;
    public final String inputAlphabet;
    public final List<String> states = new ArrayList<>();
    public final List<Transition> transitions = new ArrayList<>();
    private final Map<String, Transition> table = new HashMap<>();

    public TapeMachine(String start, String accept, boolean bounded, String inputAlphabet) {
        this.start = start;
        this.accept = accept;
        this.bounded = bounded;
        this.inputAlphabet = inputAlphabet;
        states.add(start);
        states.add(accept);
    }

    /** Agrega δ(from, read) = (to, write, move) con move ∈ {L, R, S}. */
    public TapeMachine add(String from, char read, String to, char write, char move) {
        if (!states.contains(from)) {
            states.add(from);
        }
        if (!states.contains(to)) {
            states.add(to);
        }
        Transition t = new Transition(from, read, to, write, move);
        if (table.put(from + "\u0000" + read, t) != null) {
            throw new IllegalStateException("Transición duplicada: " + t.describe());
        }
        transitions.add(t);
        return this;
    }

    /** Agrega la misma acción (to, write=símbolo leído, move) para varios símbolos. */
    public TapeMachine skip(String from, String symbols, char move) {
        for (char c : symbols.toCharArray()) {
            add(from, c, from, c, move);
        }
        return this;
    }

    public Transition find(String state, char symbol) {
        return table.get(state + "\u0000" + symbol);
    }

    public Simulation newSimulation(String input) {
        return new Simulation(input);
    }

    /** Ejecución paso a paso. */
    public class Simulation {
        public final List<Character> tape = new ArrayList<>();
        public int head;
        public int origin;
        public String state;
        public int steps;
        public Transition last;
        public int lastWritten = -1;
        public String message = "";
        public Result result = Result.RUNNING;

        Simulation(String input) {
            state = start;
            if (bounded) {
                tape.add('[');
                for (char c : input.toCharArray()) {
                    tape.add(c);
                }
                tape.add(']');
                head = 0;
            } else {
                if (input.isEmpty()) {
                    tape.add(BLANK);
                }
                for (char c : input.toCharArray()) {
                    tape.add(c);
                }
                head = 0;
            }
            origin = 0;
        }

        public char current() {
            return tape.get(head);
        }

        public Result step() {
            if (result != Result.RUNNING) {
                return result;
            }
            if (steps >= MAX_STEPS) {
                result = Result.REJECTED;
                message = "se alcanzó el límite de " + MAX_STEPS + " pasos (posible ciclo infinito)";
                return result;
            }
            char sym = current();
            Transition t = find(state, sym);
            if (t == null) {
                result = Result.REJECTED;
                message = "no hay transición desde " + state + " leyendo '" + sym + "'";
                last = null;
                return result;
            }
            last = t;
            steps++;
            tape.set(head, t.write());
            lastWritten = head;
            state = t.to();
            if (t.move() == 'R') {
                head++;
                if (head >= tape.size()) {
                    if (bounded) {
                        result = Result.REJECTED;
                        message = "el cabezal intentó salir de la cinta acotada por la derecha";
                        head--;
                        return result;
                    }
                    tape.add(BLANK);
                }
            } else if (t.move() == 'L') {
                head--;
                if (head < 0) {
                    if (bounded) {
                        result = Result.REJECTED;
                        message = "el cabezal intentó salir de la cinta acotada por la izquierda";
                        head = 0;
                        return result;
                    }
                    tape.add(0, BLANK);
                    head = 0;
                    origin++;
                    lastWritten++;
                }
            }
            if (state.equals(accept)) {
                result = Result.ACCEPTED;
                message = "alcanzó el estado de aceptación " + accept + " en " + steps + " pasos";
            }
            return result;
        }

        /** Contenido de la cinta sin blancos sobrantes ni marcadores de frontera. */
        public String output() {
            StringBuilder sb = new StringBuilder();
            for (char c : tape) {
                sb.append(c);
            }
            String s = sb.toString();
            int a = 0;
            int b = s.length();
            while (a < b && s.charAt(a) == BLANK) {
                a++;
            }
            while (b > a && s.charAt(b - 1) == BLANK) {
                b--;
            }
            return s.substring(a, b);
        }
    }
}
