package chomsky.nivel3;

import chomsky.common.Painters;
import chomsky.common.Result;
import chomsky.common.SimulatorPanel;
import chomsky.common.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Módulo del Nivel 3: reconocimiento de cadenas con un autómata finito determinista. */
public class DFAPanel extends SimulatorPanel {

    private static final int R = 26;

    private record Edge(int from, int to, List<String> symbols) { }

    private DFA dfa;
    private DFA.Simulation sim;
    private int stepNo;
    private final JTextArea defArea = new JTextArea();
    private final DFAView view = new DFAView();

    public DFAPanel(Runnable onBack) {
        super("Nivel 3 — Lenguajes regulares (Autómata Finito Determinista)",
                "Elige un lenguaje regular predeterminado o escribe tu propio AFD en la opción «Personalizado». "
                        + "Al procesar la cadena símbolo por símbolo se resalta el estado actual y la transición usada.",
                onBack);
        defArea.setFont(Theme.MONO_SMALL);
        defArea.setForeground(Theme.INK);
        defArea.setBackground(Theme.BG);
        defArea.setCaretColor(Theme.INK);
        JScrollPane scroll = new JScrollPane(defArea);
        scroll.setBorder(titled("Definición formal del AFD (editable en «Personalizado»)"));
        build(view, scroll);
    }

    @Override
    protected String[] machineNames() {
        String[] names = new String[DFACatalog.ENTRIES.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = DFACatalog.ENTRIES.get(i).name();
        }
        return names;
    }

    @Override
    protected String[] examplesFor(int machine) {
        return DFACatalog.ENTRIES.get(machine).examples();
    }

    @Override
    protected void onMachineSelected(int machine) {
        defArea.setText(DFACatalog.ENTRIES.get(machine).definition());
        defArea.setCaretPosition(0);
        defArea.setEditable(machine == DFACatalog.ENTRIES.size() - 1);
        sim = null;
        try {
            dfa = DFA.parse(defArea.getText());
        } catch (IllegalArgumentException ex) {
            dfa = null;
        }
    }

    @Override
    protected void start(int machine, String input) {
        DFA parsed = DFA.parse(defArea.getText());
        for (char c : input.toCharArray()) {
            if (!parsed.alphabet.contains(c)) {
                throw new IllegalArgumentException("El símbolo '" + c + "' no pertenece al alfabeto " + parsed.alphabet);
            }
        }
        dfa = parsed;
        sim = dfa.newSimulation(input);
        stepNo = 0;
        log("Estados: " + dfa.states + "   inicial: " + dfa.start + "   finales: " + dfa.accepting);
        log("Alfabeto Σ = " + dfa.alphabet);
        log("--------------------------------------------");
    }

    @Override
    protected Result advance() {
        char read = ' ';
        if (sim.pos < sim.input.length()) {
            read = sim.input.charAt(sim.pos);
        }
        Result r = sim.step();
        if (!sim.stepInfo.isEmpty()) {
            stepNo++;
            log("Paso " + stepNo + ": leo '" + read + "'  →  " + sim.stepInfo);
        }
        if (r != Result.RUNNING) {
            log(sim.message);
        }
        return r;
    }

    @Override
    protected String finalMessage() {
        return sim.message;
    }

    /** Dibujo del autómata: estados en círculo, transiciones etiquetadas y estado actual resaltado. */
    private class DFAView extends JPanel {
        DFAView() {
            setBackground(Theme.PANEL);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Theme.aa(g);
            int w = getWidth();
            int h = getHeight();
            int top = 8;
            if (sim == null) {
                g.setFont(Theme.UI);
                g.setColor(Theme.MUTED);
                g.drawString("Pulsa «Paso» o «Ejecutar» para procesar la cadena seleccionada.", 14, 28);
            } else {
                Painters.drawInputStrip(g, sim.input, sim.pos, w, top);
                g.setFont(Theme.BOLD);
                g.setColor(Theme.INK);
                g.drawString("Estado actual: " + sim.state, 14, top + 82);
                if (!sim.stepInfo.isEmpty()) {
                    g.setFont(Theme.MONO_SMALL);
                    g.setColor(Theme.ACCENT);
                    g.drawString("Última transición: " + sim.stepInfo, 200, top + 82);
                }
            }
            if (dfa != null) {
                drawDiagram(g, w, h, top + 100);
                g.setFont(Theme.SMALL);
                g.setColor(Theme.MUTED);
                g.drawString("Doble círculo = estado final · flecha de entrada = estado inicial · naranja = estado actual", 14, h - 10);
            }
            g.dispose();
        }

        private void drawDiagram(Graphics2D g, int w, int h, int top) {
            int n = dfa.states.size();
            double cx = w / 2.0;
            double cy = top + (h - top - 20) / 2.0;
            double ring = Math.min(w * 0.33, (h - top - 20) * 0.33);
            if (ring < 40) {
                ring = 40;
            }
            Point2D.Double[] p = new Point2D.Double[n];
            for (int i = 0; i < n; i++) {
                if (n == 1) {
                    p[i] = new Point2D.Double(cx, cy);
                } else {
                    double th = Math.PI - 2 * Math.PI * i / n;
                    p[i] = new Point2D.Double(cx + ring * Math.cos(th), cy - ring * Math.sin(th));
                }
            }
            Map<String, Edge> edges = new LinkedHashMap<>();
            for (String s : dfa.states) {
                Map<Character, String> row = dfa.delta.get(s);
                if (row == null) {
                    continue;
                }
                for (Map.Entry<Character, String> e : row.entrySet()) {
                    int a = dfa.states.indexOf(s);
                    int b = dfa.states.indexOf(e.getValue());
                    String key = a + ">" + b;
                    Edge edge = edges.get(key);
                    if (edge == null) {
                        edge = new Edge(a, b, new ArrayList<>());
                        edges.put(key, edge);
                    }
                    edge.symbols().add(String.valueOf(e.getKey()));
                }
            }
            for (Edge e : edges.values()) {
                boolean hi = false;
                if (sim != null && sim.lastFrom != null) {
                    hi = dfa.states.indexOf(sim.lastFrom) == e.from() && dfa.states.indexOf(sim.lastTo) == e.to();
                }
                drawEdge(g, e, p, edges, cx, cy, hi);
            }
            for (int i = 0; i < n; i++) {
                drawNode(g, i, p[i]);
            }
            drawStartArrow(g, p, cx, cy);
        }

        private void drawEdge(Graphics2D g, Edge e, Point2D.Double[] p, Map<String, Edge> edges,
                              double cx, double cy, boolean hi) {
            Color color = Theme.MUTED;
            float stroke = 1.6f;
            if (hi) {
                color = Theme.ACCENT;
                stroke = 3.2f;
            }
            g.setColor(color);
            g.setStroke(new BasicStroke(stroke));
            String label = String.join(",", e.symbols());
            Point2D.Double a = p[e.from()];
            if (e.from() == e.to()) {
                double dx = a.x - cx;
                double dy = a.y - cy;
                double dl = Math.hypot(dx, dy);
                double ux = 0;
                double uy = -1;
                if (dl > 1) {
                    ux = dx / dl;
                    uy = dy / dl;
                }
                // se gira el bucle para que no choque con la flecha del estado inicial
                double rot = 0.9;
                double rx = ux * Math.cos(rot) - uy * Math.sin(rot);
                double ry = ux * Math.sin(rot) + uy * Math.cos(rot);
                ux = rx;
                uy = ry;
                double lr = 15;
                double lcx = a.x + ux * (R + lr - 4);
                double lcy = a.y + uy * (R + lr - 4);
                g.draw(new Ellipse2D.Double(lcx - lr, lcy - lr, 2 * lr, 2 * lr));
                double ra = Math.atan2(uy, ux) + 0.33;
                Painters.arrowHead(g, a.x + Math.cos(ra) * R, a.y + Math.sin(ra) * R, ra + Math.PI);
                g.setFont(Theme.BOLD);
                g.setColor(color);
                Painters.centered(g, label, a.x + ux * (R + 2 * lr + 10), a.y + uy * (R + 2 * lr + 10));
            } else {
                Point2D.Double b = p[e.to()];
                double dx = b.x - a.x;
                double dy = b.y - a.y;
                double len = Math.hypot(dx, dy);
                double nx = -dy / len;
                double ny = dx / len;
                double off = 0;
                if (edges.containsKey(e.to() + ">" + e.from())) {
                    off = 38;
                }
                double mx = (a.x + b.x) / 2 + nx * off;
                double my = (a.y + b.y) / 2 + ny * off;
                double sl = Math.hypot(mx - a.x, my - a.y);
                double sx = a.x + (mx - a.x) / sl * R;
                double sy = a.y + (my - a.y) / sl * R;
                double el = Math.hypot(mx - b.x, my - b.y);
                double ex = b.x + (mx - b.x) / el * R;
                double ey = b.y + (my - b.y) / el * R;
                g.draw(new QuadCurve2D.Double(sx, sy, mx, my, ex, ey));
                Painters.arrowHead(g, ex, ey, Math.atan2(ey - my, ex - mx));
                double lx = 0.25 * sx + 0.5 * mx + 0.25 * ex + nx * 13;
                double ly = 0.25 * sy + 0.5 * my + 0.25 * ey + ny * 13;
                g.setFont(Theme.BOLD);
                g.setColor(color);
                Painters.centered(g, label, lx, ly);
            }
            g.setStroke(new BasicStroke(1f));
        }

        private void drawNode(Graphics2D g, int i, Point2D.Double c) {
            String name = dfa.states.get(i);
            Color fill = Theme.PANEL_2;
            Color border = Theme.INK;
            float stroke = 2.2f;
            if (sim != null && name.equals(sim.state)) {
                stroke = 3.6f;
                if (sim.result == Result.ACCEPTED) {
                    fill = Theme.OK_DARK;
                    border = Theme.OK;
                } else if (sim.result == Result.REJECTED) {
                    fill = Theme.BAD_DARK;
                    border = Theme.BAD;
                } else {
                    fill = Theme.ACCENT_DARK;
                    border = Theme.ACCENT;
                }
            }
            g.setColor(fill);
            g.fill(new Ellipse2D.Double(c.x - R, c.y - R, 2 * R, 2 * R));
            g.setColor(border);
            g.setStroke(new BasicStroke(stroke));
            g.draw(new Ellipse2D.Double(c.x - R, c.y - R, 2 * R, 2 * R));
            if (dfa.accepting.contains(name)) {
                g.setStroke(new BasicStroke(1.6f));
                g.draw(new Ellipse2D.Double(c.x - R + 5, c.y - R + 5, 2 * R - 10, 2 * R - 10));
            }
            g.setStroke(new BasicStroke(1f));
            g.setFont(Theme.BOLD);
            if (g.getFontMetrics().stringWidth(name) > 2 * R - 8) {
                g.setFont(Theme.SMALL);
            }
            g.setColor(Theme.INK);
            Painters.centered(g, name, c.x, c.y);
        }

        private void drawStartArrow(Graphics2D g, Point2D.Double[] p, double cx, double cy) {
            int s = dfa.states.indexOf(dfa.start);
            Point2D.Double c = p[s];
            double ux = -1;
            double uy = 0;
            if (dfa.states.size() > 1) {
                double dl = Math.hypot(c.x - cx, c.y - cy);
                if (dl > 1) {
                    ux = (c.x - cx) / dl;
                    uy = (c.y - cy) / dl;
                }
            }
            g.setColor(Theme.PRIMARY);
            g.setStroke(new BasicStroke(2f));
            g.draw(new java.awt.geom.Line2D.Double(c.x + ux * (R + 38), c.y + uy * (R + 38), c.x + ux * R, c.y + uy * R));
            Painters.arrowHead(g, c.x + ux * R, c.y + uy * R, Math.atan2(-uy, -ux));
            g.setStroke(new BasicStroke(1f));
        }
    }
}
