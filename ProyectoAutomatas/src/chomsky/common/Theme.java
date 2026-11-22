package chomsky.common;

import javax.swing.*;
import java.awt.*;

/** Tema oscuro unificado: colores, fuentes e instalación del Look and Feel. */
public final class Theme {
    public static final Color BG = new Color(0x0D1117);
    public static final Color PANEL = new Color(0x161B22);
    public static final Color PANEL_2 = new Color(0x21262D);
    public static final Color INK = new Color(0xE6EDF3);
    public static final Color MUTED = new Color(0x8B949E);
    public static final Color GRID = new Color(0x3D444D);
    public static final Color PRIMARY = new Color(0x58A6FF);
    public static final Color PRIMARY_DARK = new Color(0x1F3A5F);
    public static final Color ACCENT = new Color(0xF0883E);
    public static final Color ACCENT_DARK = new Color(0x4D3015);
    public static final Color OK = new Color(0x3FB950);
    public static final Color OK_DARK = new Color(0x13361F);
    public static final Color BAD = new Color(0xF85149);
    public static final Color BAD_DARK = new Color(0x4A1B1B);
    public static final Color PURPLE = new Color(0xBC8CFF);
    public static final Color PURPLE_DARK = new Color(0x33264F);

    public static final Font UI = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.BOLD, 18);
    public static final Font MONO_SMALL = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private Theme() { }

    /** Instala Nimbus con la paleta oscura. Debe llamarse antes de crear ventanas. */
    public static void install() {
        UIManager.put("control", PANEL);
        UIManager.put("info", PANEL_2);
        UIManager.put("nimbusBase", PANEL_2);
        UIManager.put("nimbusBlueGrey", GRID);
        UIManager.put("nimbusDisabledText", MUTED);
        UIManager.put("nimbusFocus", PRIMARY);
        UIManager.put("nimbusInfoBlue", PRIMARY);
        UIManager.put("nimbusLightBackground", BG);
        UIManager.put("nimbusOrange", ACCENT);
        UIManager.put("nimbusRed", BAD);
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("nimbusSelectionBackground", PRIMARY_DARK);
        UIManager.put("text", INK);
        UIManager.put("List.background", PANEL_2);
        UIManager.put("ComboBox.background", PANEL_2);
        UIManager.put("TextField.background", PANEL_2);
        UIManager.put("TextArea.background", BG);
        UIManager.put("TextArea.foreground", INK);
        UIManager.put("TextArea.caretForeground", INK);
        UIManager.put("TextField.caretForeground", INK);
        UIManager.put("SplitPane.background", PANEL);
        UIManager.put("ScrollPane.background", PANEL);
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
            // si Nimbus no está disponible se usa el Look and Feel por defecto
        }
    }

    public static void aa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
