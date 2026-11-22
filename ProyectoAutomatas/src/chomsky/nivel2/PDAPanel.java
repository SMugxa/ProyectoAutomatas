package chomsky.nivel2;

import chomsky.common.Painters;
import chomsky.common.Result;
import chomsky.common.SimulatorPanel;
import chomsky.common.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/** Módulo del Nivel 2: autómata de pila con animación de la pila (push / pop). */
public class PDAPanel extends SimulatorPanel {

    private PDA pda;
    private PDA.Run run;
    private String input = "";
    private int idx;
    private final JTextArea defArea = new JTextArea();
    private final StackView view = new StackView();

    public PDAPanel(Runnable onBack) {
        super("Nivel 2 — Lenguajes libres de contexto (Autómata de Pila)",
                "La pila (memoria LIFO) se dibuja a la derecha: en verde lo que se apila (push) y en rojo "
                        + "lo que se desapila (pop) en cada paso. Los lenguajes no deterministas se resuelven explorando todas las rutas.",
                onBack);
        defArea.setFont(Theme.MONO_SMALL);
        defArea.setForeground(Theme.INK);
        defArea.setBackground(Theme.BG);
        defArea.setEditable(false);
        defArea.setLineWrap(true);
        defArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(defArea);
        scroll.setBorder(titled("Definición formal: δ(estado, entrada, tope) = (estado, apilar)"));
        build(view, scroll);
    }

    @Override
    protected String[] machineNames() {
        String[] names = new String[PDACatalog.ENTRIES.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = PDACatalog.ENTRIES.get(i).name();
        }
        return names;
    }

    @Override
    protected String[] examplesFor(int machine) {
        return PDACatalog.ENTRIES.get(machine).examples();
    }

    @Override
    protected void onMachineSelected(int machine) {
        pda = PDACatalog.ENTRIES.get(machine).pda();
        run = null;
        StringBuilder sb = new StringBuilder();
        sb.append("Estado inicial: ").append(pda.start).append("\n");
        sb.append("Estados finales: ").append(pda.finals).append("\n");
        sb.append("Pila inicial: ").append(PDA.BOTTOM).append("  (el tope es el último símbolo)\n");
        sb.append("ε = sin leer entrada / sin apilar\n\n");
        for (PDA.Transition t : pda.transitions) {
            sb.append(t.describe()).append("\n");
        }
        defArea.setText(sb.toString());
        defArea.setCaretPosition(0);
    }

    @Override
    protected void start(int machine, String in) {
        pda = PDACatalog.ENTRIES.get(machine).pda();
        input = in;
        run = pda.run(in);
        idx = 0;
        log("Pila inicial: " + stackText(run.steps().get(0).config().stack()));
        log("Rutas exploradas: " + run.explored());
        log("--------------------------------------------");
    }

    @Override
    protected Result advance() {
        if (idx + 1 < run.steps().size()) {
            idx++;
            PDA.Step s = run.steps().get(idx);
            String read = "ε";
            if (s.via().read() != PDA.EPS) {
                read = String.valueOf(s.via().read());
            }
            log("Paso " + idx + ": " + s.via().describe());
            log("        entrada leída: " + read + "   pila: " + stackText(s.config().stack()));
        }
        if (idx + 1 < run.steps().size()) {
            return Result.RUNNING;
        }
        if (run.accepted()) {
            return Result.ACCEPTED;
        }
        return Result.REJECTED;
    }

    @Override
    protected String finalMessage() {
        return run.reason();
    }

    private static String stackText(String stack) {
        StringBuilder sb = new StringBuilder("[ ");
        for (char c : stack.toCharArray()) {
            sb.append(c).append(' ');
        }
        sb.append("]  ← tope");
        return sb.toString();
    }

    /** Dibujo de la cadena, el estado actual y la pila con los cambios del último paso. */
    private class StackView extends JPanel {
        private static final int BH = 34;

        StackView() {
            setBackground(Theme.PANEL);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Theme.aa(g);
            int w = getWidth();
            int h = getHeight();
            if (run == null) {
                g.setFont(Theme.UI);
                g.setColor(Theme.MUTED);
                g.drawString("Pulsa «Paso» o «Ejecutar» para simular la pila.", 14, 28);
                g.dispose();
                return;
            }
            PDA.Step cur = run.steps().get(idx);
            Painters.drawInputStrip(g, input, cur.config().pos(), w, 8);
            drawStates(g, cur, w, h);
            drawStack(g, cur, w, h);
            g.dispose();
        }

        private void drawStates(Graphics2D g, PDA.Step cur, int w, int h) {
            g.setFont(Theme.SMALL);
            g.setColor(Theme.MUTED);
            g.drawString("Estado de control", 14, 100);
            int n = pda.states.size();
            int r = 24;
            int areaW = (int) (w * 0.5);
            int gap = Math.min(80, (areaW - 40) / Math.max(1, n));
            int y = 140;
            for (int i = 0; i < n; i++) {
                String name = pda.states.get(i);
                int cx = 40 + i * gap + r;
                boolean active = name.equals(cur.config().state());
                Color fill = Theme.PANEL_2;
                Color border = Theme.MUTED;
                if (active) {
                    fill = Theme.ACCENT_DARK;
                    border = Theme.ACCENT;
                    if (run.steps().size() - 1 == idx) {
                        if (run.accepted()) {
                            fill = Theme.OK_DARK;
                            border = Theme.OK;
                        } else {
                            fill = Theme.BAD_DARK;
                            border = Theme.BAD;
                        }
                    }
                }
                g.setColor(fill);
                g.fill(new Ellipse2D.Double(cx - r, y - r, 2 * r, 2 * r));
                g.setColor(border);
                g.setStroke(new BasicStroke(active ? 3.4f : 1.8f));
                g.draw(new Ellipse2D.Double(cx - r, y - r, 2 * r, 2 * r));
                if (pda.finals.contains(name)) {
                    g.setStroke(new BasicStroke(1.5f));
                    g.draw(new Ellipse2D.Double(cx - r + 5, y - r + 5, 2 * r - 10, 2 * r - 10));
                }
                g.setStroke(new BasicStroke(1f));
                g.setFont(Theme.BOLD);
                g.setColor(Theme.INK);
                Painters.centered(g, name, cx, y);
            }
            g.setFont(Theme.SMALL);
            g.setColor(Theme.MUTED);
            g.drawString("Doble círculo = estado final", 14, y + r + 22);
            if (cur.via() != null) {
                g.setFont(Theme.MONO_SMALL);
                g.setColor(Theme.ACCENT);
                g.drawString("Transición aplicada:", 14, y + r + 56);
                g.setColor(Theme.INK);
                g.drawString(cur.via().describe(), 14, y + r + 76);
            }
        }

        private void drawStack(Graphics2D g, PDA.Step cur, int w, int h) {
            String now = cur.config().stack();
            String before = "";
            if (idx > 0) {
                before = run.steps().get(idx - 1).config().stack();
            } else {
                before = now;
            }
            int common = 0;
            while (common < now.length() && common < before.length()
                    && now.charAt(common) == before.charAt(common)) {
                common++;
            }
            int cx = (int) (w * 0.74);
            int bw = 96;
            int bottom = h - 46;
            int topLimit = 112;
            int maxBlocks = Math.max(4, (bottom - topLimit) / BH);
            int bh = BH;
            int shown = Math.max(now.length(), before.length());
            if (shown + 1 > maxBlocks) {
                bh = Math.max(14, (bottom - topLimit) / (shown + 1));
            }
            g.setFont(Theme.BOLD);
            g.setColor(Theme.MUTED);
            Painters.centered(g, "PILA (LIFO)", cx, 100);
            g.setColor(Theme.GRID);
            g.setStroke(new BasicStroke(3f));
            g.drawLine(cx - bw / 2 - 8, bottom, cx + bw / 2 + 8, bottom);
            g.setStroke(new BasicStroke(1f));
            Font f = new Font(Font.MONOSPACED, Font.BOLD, Math.max(11, Math.min(20, bh - 8)));
            for (int i = 0; i < now.length(); i++) {
                int y = bottom - (i + 1) * bh;
                boolean fresh = i >= common;
                Color fill = Theme.PRIMARY_DARK;
                Color border = Theme.PRIMARY;
                if (fresh) {
                    fill = Theme.OK_DARK;
                    border = Theme.OK;
                }
                g.setColor(fill);
                g.fillRoundRect(cx - bw / 2, y + 2, bw, bh - 4, 8, 8);
                g.setColor(border);
                g.setStroke(new BasicStroke(fresh ? 2.6f : 1.6f));
                g.drawRoundRect(cx - bw / 2, y + 2, bw, bh - 4, 8, 8);
                g.setStroke(new BasicStroke(1f));
                g.setFont(f);
                g.setColor(Theme.INK);
                Painters.centered(g, String.valueOf(now.charAt(i)), cx, y + bh / 2.0);
            }
            // Símbolos desapilados en este paso: bloques rojos punteados a la derecha
            for (int i = common; i < before.length(); i++) {
                int y = bottom - (i + 1) * bh;
                int gx = cx + bw / 2 + 14;
                g.setColor(Theme.BAD_DARK);
                g.fillRoundRect(gx, y + 2, bw - 20, bh - 4, 8, 8);
                g.setColor(Theme.BAD);
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                        new float[]{5f, 4f}, 0f));
                g.drawRoundRect(gx, y + 2, bw - 20, bh - 4, 8, 8);
                g.setStroke(new BasicStroke(1f));
                g.setFont(f);
                g.setColor(Theme.INK);
                Painters.centered(g, "pop " + before.charAt(i), gx + (bw - 20) / 2.0, y + bh / 2.0);
            }
            // Flecha y etiqueta del tope
            int ty = bottom - now.length() * bh + bh / 2;
            g.setColor(Theme.ACCENT);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(cx - bw / 2 - 52, ty, cx - bw / 2 - 12, ty);
            Painters.arrowHead(g, cx - bw / 2 - 8, ty, 0);
            g.setStroke(new BasicStroke(1f));
            g.setFont(Theme.SMALL);
            g.drawString("tope", cx - bw / 2 - 52, ty - 6);
            g.setColor(Theme.MUTED);
            Painters.centered(g, "altura = " + (now.length() - 1) + " (sin contar $)", cx, bottom + 22);
        }
    }
}
