package chomsky.common;

import java.awt.*;
import java.awt.geom.Path2D;

/** Utilidades de dibujo reutilizadas por las distintas vistas. */
public final class Painters {
    private Painters() { }

    /** Dibuja texto centrado en (cx, cy). */
    public static void centered(Graphics2D g, String s, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        float x = (float) (cx - fm.stringWidth(s) / 2.0);
        float y = (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0);
        g.drawString(s, x, y);
    }

    /** Punta de flecha en (x, y) apuntando en la dirección angle (radianes). */
    public static void arrowHead(Graphics2D g, double x, double y, double angle) {
        double len = 11;
        double spread = Math.toRadians(25);
        Path2D p = new Path2D.Double();
        p.moveTo(x, y);
        p.lineTo(x - len * Math.cos(angle - spread), y - len * Math.sin(angle - spread));
        p.lineTo(x - len * Math.cos(angle + spread), y - len * Math.sin(angle + spread));
        p.closePath();
        g.fill(p);
    }

    /**
     * Franja con los símbolos de la cadena de entrada. Los símbolos ya consumidos
     * se pintan en azul y el símbolo que se leerá a continuación se resalta en naranja.
     */
    public static void drawInputStrip(Graphics2D g, String input, int pos, int width, int top) {
        g.setFont(Theme.SMALL);
        g.setColor(Theme.MUTED);
        g.drawString("Cadena de entrada", 14, top + 12);
        int n = input.length();
        if (n == 0) {
            g.setFont(Theme.MONO);
            g.setColor(Theme.INK);
            centered(g, "ε  (cadena vacía)", width / 2.0, top + 38);
            return;
        }
        int cw = Math.max(16, Math.min(40, (width - 28) / n));
        int ch = Math.min(40, cw + 4);
        int x0 = (width - cw * n) / 2;
        int y = top + 20;
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.max(11, Math.min(20, cw - 8))));
        for (int i = 0; i < n; i++) {
            int x = x0 + i * cw;
            Color fill = Theme.PANEL_2;
            Color border = Theme.GRID;
            float width2 = 1.2f;
            if (i < pos) {
                fill = Theme.PRIMARY_DARK;
            } else if (i == pos) {
                fill = Theme.ACCENT_DARK;
                border = Theme.ACCENT;
                width2 = 3f;
            }
            g.setColor(fill);
            g.fillRect(x, y, cw, ch);
            g.setColor(border);
            g.setStroke(new BasicStroke(width2));
            g.drawRect(x, y, cw, ch);
            g.setStroke(new BasicStroke(1f));
            g.setColor(Theme.INK);
            centered(g, String.valueOf(input.charAt(i)), x + cw / 2.0, y + ch / 2.0);
        }
    }
}
