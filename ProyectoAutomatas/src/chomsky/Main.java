package chomsky;

import chomsky.common.Theme;
import chomsky.nivel0.TMPanel;
import chomsky.nivel1.LBAPanel;
import chomsky.nivel2.PDAPanel;
import chomsky.nivel3.DFAPanel;

import javax.swing.*;
import java.awt.*;

/** Ventana principal: panel con los cuatro módulos de la Jerarquía de Chomsky. */
public class Main extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public Main() {
        super("Simulador de la Jerarquía de Chomsky");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Runnable toMenu = () -> cards.show(root, "menu");
        root.setBackground(Theme.BG);
        root.add(buildMenu(), "menu");
        root.add(new DFAPanel(toMenu), "n3");
        root.add(new PDAPanel(toMenu), "n2");
        root.add(new LBAPanel(toMenu), "n1");
        root.add(new TMPanel(toMenu), "n0");
        setContentPane(root);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }

    private JPanel buildMenu() {
        JPanel menu = new JPanel(new BorderLayout(0, 18));
        menu.setBackground(Theme.BG);
        menu.setBorder(BorderFactory.createEmptyBorder(26, 40, 26, 40));
        JLabel title = new JLabel("Simulador de la Jerarquía de Chomsky");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        title.setForeground(Theme.INK);
        JLabel sub = new JLabel("Elige un módulo para visualizar, simular y validar cadenas en cada nivel.");
        sub.setFont(Theme.UI);
        sub.setForeground(Theme.MUTED);
        JPanel head = new JPanel(new BorderLayout(0, 6));
        head.setOpaque(false);
        head.add(title, BorderLayout.NORTH);
        head.add(sub, BorderLayout.SOUTH);
        menu.add(head, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        grid.add(card("Nivel 3", "Lenguajes regulares", "Autómata Finito Determinista",
                "Muestra los estados del autómata y el estado actual al procesar la cadena símbolo por símbolo.",
                Theme.PRIMARY, "n3"));
        grid.add(card("Nivel 2", "Lenguajes libres de contexto", "Autómata de Pila",
                "Animación de la pila LIFO (push / pop) mientras se consume la cadena.",
                Theme.OK, "n2"));
        grid.add(card("Nivel 1", "Lenguajes sensibles al contexto", "Autómata Linealmente Acotado",
                "Cinta acotada a la entrada con delimitadores de frontera y cabezal bidireccional.",
                Theme.PURPLE, "n1"));
        grid.add(card("Nivel 0", "Lenguajes recursivamente enumerables", "Máquina de Turing",
                "Cinta extensible, alfabeto de la cinta, cabezal de lectura/escritura y estado activo.",
                Theme.ACCENT, "n0"));
        menu.add(grid, BorderLayout.CENTER);
        return menu;
    }

    private JComponent card(String level, String language, String machine, String text, Color color, String key) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, color),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        JLabel l = new JLabel(level + " · " + language);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        l.setForeground(color);
        JLabel m = new JLabel(machine);
        m.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        m.setForeground(Theme.INK);
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 2));
        top.setOpaque(false);
        top.add(l);
        top.add(m);
        JTextArea desc = new JTextArea(text);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setFont(Theme.UI);
        desc.setForeground(Theme.MUTED);
        JButton open = new JButton("Abrir módulo");
        open.addActionListener(e -> cards.show(root, key));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(open);
        p.add(top, BorderLayout.NORTH);
        p.add(desc, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
