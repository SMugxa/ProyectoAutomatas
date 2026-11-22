package chomsky.common;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.function.Supplier;

/**
 * Dibujo de una cinta con cabezal. Sirve para el Nivel 1 (cinta acotada con marcadores de frontera)
 * y para el Nivel 0 (cinta extensible con puntos suspensivos y posiciones negativas).
 */
public class TapeView extends JPanel {
    private static final int CW = 46;
    private static final int CH = 54;

    private final Supplier<TapeMachine.Simulation> simSupplier;
    private final Supplier<TapeMachine> machineSupplier;
    private final String tapeTitle;

    public TapeView(String tapeTitle, Supplier<TapeMachine> machineSupplier,
                    Supplier<TapeMachine.Simulation> simSupplier) {
        this.tapeTitle = tapeTitle;
        this.machineSupplier = machineSupplier;
        this.simSupplier = simSupplier;
        setBackground(Theme.PANEL);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Theme.aa(g);
        int w = getWidth();
        int h = getHeight();
        TapeMachine tm = machineSupplier.get();
        TapeMachine.Simulation sim = simSupplier.get();
        g.setFont(Theme.BOLD);
        g.setColor(Theme.INK);
        g.drawString(tapeTitle, 14, 26);
        if (sim == null || tm == null) {
            g.setFont(Theme.UI);
            g.setColor(Theme.MUTED);
            g.drawString("Pulsa «Paso» o «Ejecutar» para cargar la cinta.", 14, 52);
            g.dispose();
            return;
        }
        drawInfo(g, tm, sim, w);
        drawTape(g, tm, sim, w, h);
        drawLegend(g, tm, h);
        g.dispose();
    }

    private void drawInfo(Graphics2D g, TapeMachine tm, TapeMachine.Simulation sim, int w) {
        Color fill = Theme.ACCENT_DARK;
        Color border = Theme.ACCENT;
        if (sim.result == Result.ACCEPTED) {
            fill = Theme.OK_DARK;
            border = Theme.OK;
        } else if (sim.result == Result.REJECTED) {
            fill = Theme.BAD_DARK;
            border = Theme.BAD;
        }
        int r = 30;
        int cx = 60;
        int cy = 92;
        g.setColor(fill);
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g.setColor(border);
        g.setStroke(new BasicStroke(3.2f));
        g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g.setStroke(new BasicStroke(1f));
        g.setFont(Theme.BOLD);
        g.setColor(Theme.INK);
        Painters.centered(g, sim.state, cx, cy);
        g.setFont(Theme.SMALL);
        g.setColor(Theme.MUTED);
        Painters.centered(g, "estado activo", cx, cy + r + 14);
        g.setFont(Theme.MONO_SMALL);
        g.setColor(Theme.MUTED);
        int x = 120;
        g.drawString("Pasos: " + sim.steps, x, 80);
        g.drawString("Posición del cabezal: " + (sim.head - sim.origin), x, 100);
        if (sim.last != null) {
            g.setColor(Theme.ACCENT);
            g.drawString("Última transición: " + sim.last.describe(), x, 120);
        }
    }

    private void drawTape(Graphics2D g, TapeMachine tm, TapeMachine.Simulation sim, int w, int h) {
        int n = sim.tape.size();
        int visible = Math.max(5, (w - 60) / CW);
        int first;
        int last;
        if (tm.bounded && n <= visible) {
            first = 0;
            last = n - 1;
        } else {
            int left = visible / 2;
            first = sim.head - left;
            last = first + visible - 1;
            if (tm.bounded) {
                if (first < 0) {
                    first = 0;
                    last = visible - 1;
                }
                if (last > n - 1) {
                    last = n - 1;
                    first = Math.max(0, last - visible + 1);
                }
            }
        }
        int count = last - first + 1;
        int x0 = (w - count * CW) / 2;
        int y = 190;
        for (int i = first; i <= last; i++) {
            int x = x0 + (i - first) * CW;
            char c = TapeMachine.BLANK;
            boolean real = i >= 0 && i < n;
            if (real) {
                c = sim.tape.get(i);
            }
            boolean marker = tm.bounded && (c == '[' || c == ']');
            Color fill = Theme.PANEL_2;
            Color border = Theme.GRID;
            if (marker) {
                fill = Theme.PURPLE_DARK;
                border = Theme.PURPLE;
            } else if (!real) {
                fill = Theme.BG;
            }
            if (i == sim.lastWritten && sim.last != null) {
                fill = Theme.PRIMARY_DARK;
            }
            if (i == sim.head) {
                fill = Theme.ACCENT_DARK;
                border = Theme.ACCENT;
            }
            g.setColor(fill);
            g.fillRect(x, y, CW, CH);
            g.setColor(border);
            g.setStroke(new BasicStroke(i == sim.head ? 3.4f : 1.4f));
            g.drawRect(x, y, CW, CH);
            g.setStroke(new BasicStroke(1f));
            g.setFont(Theme.MONO);
            if (c == TapeMachine.BLANK) {
                g.setColor(Theme.MUTED);
            } else {
                g.setColor(Theme.INK);
            }
            String shown = String.valueOf(c);
            if (c == TapeMachine.BLANK) {
                shown = "␣";
            }
            Painters.centered(g, shown, x + CW / 2.0, y + CH / 2.0);
            g.setFont(Theme.SMALL);
            g.setColor(Theme.MUTED);
            Painters.centered(g, String.valueOf(i - sim.origin), x + CW / 2.0, y + CH + 14);
        }
        // Cabezal de lectura/escritura
        int hx = x0 + (sim.head - first) * CW + CW / 2;
        g.setColor(Theme.ACCENT);
        int[] xs = {hx, hx - 11, hx + 11};
        int[] ys = {y - 4, y - 26, y - 26};
        g.fillPolygon(xs, ys, 3);
        g.setFont(Theme.SMALL);
        Painters.centered(g, "cabezal  L/E", hx, y - 38);
        // Extremos de la cinta
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        g.setColor(Theme.MUTED);
        if (tm.bounded) {
            if (first > 0) {
                g.drawString("···", x0 - 34, y + CH / 2 + 6);
            }
            if (last < n - 1) {
                g.drawString("···", x0 + count * CW + 6, y + CH / 2 + 6);
            }
        } else {
            g.drawString("···", x0 - 34, y + CH / 2 + 6);
            g.drawString("···", x0 + count * CW + 6, y + CH / 2 + 6);
        }
        // Salida de funciones aritméticas / contenido
        g.setFont(Theme.MONO_SMALL);
        g.setColor(Theme.INK);
        String out = sim.output();
        if (out.isEmpty()) {
            out = "(vacía)";
        }
        g.drawString("Contenido de la cinta: " + out, 14, y + CH + 54);
    }

    private void drawLegend(Graphics2D g, TapeMachine tm, int h) {
        g.setFont(Theme.SMALL);
        g.setColor(Theme.MUTED);
        if (tm.bounded) {
            g.drawString("Morado = marcadores de frontera [ ]  ·  naranja = celda bajo el cabezal  ·  azul = última celda escrita", 14, h - 28);
            g.drawString("El cabezal se mueve en ambos sentidos pero nunca sale de la zona [ … ].", 14, h - 10);
        } else {
            g.drawString("␣ = blanco  ·  naranja = celda bajo el cabezal  ·  azul = última celda escrita  ·  las celdas oscuras se agregan al crecer la cinta", 14, h - 28);
            g.drawString("La cinta es infinita: crece a ambos lados bajo demanda y las posiciones pueden ser negativas.", 14, h - 10);
        }
    }
}
