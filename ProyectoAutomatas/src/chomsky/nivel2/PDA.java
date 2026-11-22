package chomsky.nivel2;

import java.util.*;


public class PDA {
    public static final char EPS = 0;
    public static final char BOTTOM = '$';
    private static final int MAX_CONFIGS = 50000;

    public record Transition(String from, char read, char pop, String to, String push) {
        public String describe() {
            String r = "ε";
            if (read != EPS) {
                r = String.valueOf(read);
            }
            String p = "ε";
            if (!push.isEmpty()) {
                p = push;
            }
            return "δ(" + from + ", " + r + ", " + pop + ") = (" + to + ", " + p + ")";
        }
    }

    /** Configuración instantánea: la pila se guarda con el tope al final de la cadena. */
    public record Config(String state, int pos, String stack) { }

    public record Step(Config config, Transition via) { }

    public record Run(List<Step> steps, boolean accepted, int explored, String reason) { }

    public final String start;
    public final List<String> states = new ArrayList<>();
    public final Set<String> finals = new LinkedHashSet<>();
    public final List<Transition> transitions = new ArrayList<>();

    public PDA(String start, String... finalStates) {
        this.start = start;
        states.add(start);
        for (String f : finalStates) {
            finals.add(f);
            if (!states.contains(f)) {
                states.add(f);
            }
        }
    }

    /** Agrega una transición. Use 'ε' como símbolo de entrada para una transición espontánea. */
    public PDA add(String from, char read, char pop, String to, String push) {
        char r = read;
        if (read == 'ε') {
            r = EPS;
        }
        if (!states.contains(from)) {
            states.add(from);
        }
        if (!states.contains(to)) {
            states.add(to);
        }
        transitions.add(new Transition(from, r, pop, to, push));
        return this;
    }

    public Run run(String input) {
        Config init = new Config(start, 0, String.valueOf(BOTTOM));
        Map<Config, Config> parent = new HashMap<>();
        Map<Config, Transition> via = new HashMap<>();
        Set<Config> seen = new HashSet<>();
        Deque<Config> queue = new ArrayDeque<>();
        queue.add(init);
        seen.add(init);
        Config accepted = null;
        Config best = init;
        int explored = 0;
        while (!queue.isEmpty() && explored < MAX_CONFIGS) {
            Config c = queue.poll();
            explored++;
            if (c.pos() == input.length() && finals.contains(c.state())) {
                accepted = c;
                break;
            }
            if (c.pos() > best.pos()) {
                best = c;
            }
            if (c.stack().isEmpty()) {
                continue;
            }
            char top = c.stack().charAt(c.stack().length() - 1);
            for (Transition t : transitions) {
                if (!t.from().equals(c.state()) || t.pop() != top) {
                    continue;
                }
                int npos = c.pos();
                if (t.read() != EPS) {
                    if (npos >= input.length() || input.charAt(npos) != t.read()) {
                        continue;
                    }
                    npos++;
                }
                String base = c.stack().substring(0, c.stack().length() - 1);
                String ns = base + new StringBuilder(t.push()).reverse();
                if (ns.length() > input.length() + 3) {
                    continue;
                }
                Config n = new Config(t.to(), npos, ns);
                if (seen.add(n)) {
                    parent.put(n, c);
                    via.put(n, t);
                    queue.add(n);
                }
            }
        }
        Config end = best;
        if (accepted != null) {
            end = accepted;
        }
        LinkedList<Step> path = new LinkedList<>();
        Config cur = end;
        while (cur != null) {
            path.addFirst(new Step(cur, via.get(cur)));
            cur = parent.get(cur);
        }
        String reason;
        if (accepted != null) {
            reason = "estado final " + end.state() + " alcanzado con toda la entrada consumida";
        } else if (end.pos() < input.length()) {
            char top = end.stack().charAt(end.stack().length() - 1);
            reason = "ninguna ruta puede seguir: no hay transición desde " + end.state()
                    + " leyendo '" + input.charAt(end.pos()) + "' con tope '" + top + "'";
        } else if (!end.stack().equals(String.valueOf(BOTTOM))) {
            reason = "se consumió la entrada pero la pila no quedó vacía (" + end.stack() + ")";
        } else {
            reason = "se consumió la entrada pero " + end.state() + " no es un estado final";
        }
        return new Run(new ArrayList<>(path), accepted != null, explored, reason);
    }
}
