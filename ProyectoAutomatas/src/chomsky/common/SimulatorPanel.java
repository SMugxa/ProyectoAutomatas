package chomsky.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Plantilla común de los cuatro simuladores: encabezado, selector de lenguaje/máquina,
 * cadena de prueba (editable), botones Paso / Ejecutar / Reiniciar, velocidad y registro.
 * Cada nivel solo implementa cómo se prepara una ejecución y cómo avanza un paso.
 */
public abstract class SimulatorPanel extends JPanel {

    protected final JComboBox<String> machineBox = new JComboBox<>();
    protected final JComboBox<String> inputBox = new JComboBox<>();
    private final JButton loadBtn = new JButton("Cargar cadena");
    private final JButton stepBtn = new JButton("Paso");
    private final JButton runBtn = new JButton("Ejecutar");
    private final JButton resetBtn = new JButton("Reiniciar");
    private final JSlider speed = new JSlider(1, 10, 5);
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea logArea = new JTextArea();
    private final Timer timer;
    private final String title;
    private final String description;
    private final Runnable onBack;
    private JComponent visual;
    private boolean loaded;
    private boolean finished;
    private boolean adjusting;
    private String loadedInput = null;

    protected SimulatorPanel(String title, String description, Runnable onBack) {
        this.title = title;
        this.description = description;
        this.onBack = onBack;
        this.timer = new Timer(600, e -> doOneStep());
    }

    /** Nombres de los lenguajes o máquinas disponibles. */
    protected abstract String[] machineNames();

    /** Cadenas de prueba del lenguaje/máquina indicado. */
    protected abstract String[] examplesFor(int machine);

    /** Se llama cuando el usuario cambia de lenguaje/máquina. */
    protected void onMachineSelected(int machine) { }

    /** Prepara una ejecución nueva. Lanza IllegalArgumentException si la cadena no es válida. */
    protected abstract void start(int machine, String input);

    /** Ejecuta un paso de la simulación. */
    protected abstract Result advance();

    /** Texto adicional que se muestra junto al veredicto final. */
    protected String finalMessage() {
        return "";
    }

    protected final void log(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    protected final int selectedMachine() {
        return machineBox.getSelectedIndex();
    }

    protected final void build(JComponent visualComponent, JComponent side) {
        this.visual = visualComponent;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 14, 12, 14));
        setBackground(Theme.BG);

        // Encabezado
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.TITLE);
        titleLabel.setForeground(Theme.INK);
        JButton back = new JButton("Volver al menú");
        back.addActionListener(e -> {
            stopTimer();
            onBack.run();
        });
        top.add(titleLabel, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        JTextArea desc = new JTextArea(description);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setOpaque(false);
        desc.setFont(Theme.UI);
        desc.setForeground(Theme.MUTED);
        header.add(top, BorderLayout.NORTH);
        header.add(desc, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Zona central: visualización + (definición formal / registro)
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(Theme.MONO_SMALL);
        logArea.setForeground(Theme.INK);
        logArea.setBackground(Theme.BG);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(titled("Registro de ejecución"));
        JComponent right = logScroll;
        if (side != null) {
            JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, side, logScroll);
            vertical.setResizeWeight(0.5);
            vertical.setBorder(null);
            right = vertical;
        }
        visualComponent.setPreferredSize(new Dimension(680, 420));
        right.setPreferredSize(new Dimension(390, 420));
        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, visualComponent, right);
        main.setResizeWeight(0.64);
        main.setBorder(null);
        add(main, BorderLayout.CENTER);

        // Controles
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row1.setOpaque(false);
        row1.add(label("Lenguaje / máquina:"));
        row1.add(machineBox);
        row1.add(label("Cadena (ε = vacía):"));
        inputBox.setEditable(true);
        inputBox.setPreferredSize(new Dimension(260, 28));
        row1.add(inputBox);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row2.setOpaque(false);
        row2.add(loadBtn);
        row2.add(stepBtn);
        row2.add(runBtn);
        row2.add(resetBtn);
        row2.add(label("Velocidad:"));
        speed.setPreferredSize(new Dimension(150, 24));
        speed.setOpaque(false);
        row2.add(speed);
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row3.setOpaque(false);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        statusLabel.setForeground(Theme.INK);
        row3.add(statusLabel);
        controls.add(row1);
        controls.add(row2);
        controls.add(row3);
        add(controls, BorderLayout.SOUTH);

        // Eventos
        loadBtn.addActionListener(e -> loadInput());
        resetBtn.addActionListener(e -> loadInput());
        stepBtn.addActionListener(e -> {
            stopTimer();
            doOneStep();
        });
        runBtn.addActionListener(e -> toggleRun());
        speed.addChangeListener(e -> timer.setDelay(1100 - speed.getValue() * 100));
        for (String name : machineNames()) {
            machineBox.addItem(name);
        }
        machineBox.addActionListener(e -> {
            if (!adjusting) {
                machineChanged();
            }
        });
        inputBox.addActionListener(e -> {
            if (!adjusting) {
                loadInput();
            }
        });
        machineChanged();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.MUTED);
        l.setFont(Theme.UI);
        return l;
    }

    protected static TitledBorder titled(String text) {
        TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.GRID), text);
        b.setTitleColor(Theme.MUTED);
        return b;
    }

    private void machineChanged() {
        stopTimer();
        int idx = Math.max(0, machineBox.getSelectedIndex());
        adjusting = true;
        inputBox.removeAllItems();
        for (String ex : examplesFor(idx)) {
            inputBox.addItem(ex);
        }
        if (inputBox.getItemCount() > 0) {
            inputBox.setSelectedIndex(0);
        }
        adjusting = false;
        onMachineSelected(idx);
        loaded = false;
        finished = false;
        logArea.setText("");
        setStatus("Elige o escribe una cadena y pulsa Paso o Ejecutar.", Theme.MUTED);
        visual.repaint();
    }

    private String currentInput() {
        Object o = inputBox.getEditor().getItem();
        String s = "";
        if (o != null) {
            s = o.toString().trim();
        }
        if (s.equals("ε")) {
            s = "";
        }
        return s;
    }

    private void loadInput() {
        stopTimer();
        String in = currentInput();
        logArea.setText("");
        try {
            start(machineBox.getSelectedIndex(), in);
            loaded = true;
            finished = false;
            loadedInput = in;
            String shown = in;
            if (in.isEmpty()) {
                shown = "ε";
            }
            setStatus("Cadena cargada: " + shown + ". Pulsa Paso o Ejecutar.", Theme.INK);
        } catch (IllegalArgumentException ex) {
            loaded = false;
            setStatus(ex.getMessage(), Theme.BAD);
        }
        visual.repaint();
    }

    private void ensureLoaded() {
        if (!loaded || !currentInput().equals(loadedInput)) {
            loadInput();
        }
    }

    private void doOneStep() {
        ensureLoaded();
        if (!loaded || finished) {
            stopTimer();
            return;
        }
        Result r = advance();
        visual.repaint();
        if (r != Result.RUNNING) {
            finished = true;
            stopTimer();
            String extra = finalMessage();
            String text = "CADENA RECHAZADA";
            Color color = Theme.BAD;
            if (r == Result.ACCEPTED) {
                text = "CADENA ACEPTADA";
                color = Theme.OK;
            }
            if (!extra.isEmpty()) {
                text = text + "  —  " + extra;
            }
            setStatus(text, color);
            log(">> " + text);
        }
    }

    private void toggleRun() {
        if (timer.isRunning()) {
            stopTimer();
            return;
        }
        ensureLoaded();
        if (!loaded) {
            return;
        }
        if (finished) {
            loadInput();
            if (!loaded) {
                return;
            }
        }
        timer.setDelay(1100 - speed.getValue() * 100);
        timer.start();
        runBtn.setText("Pausar");
    }

    private void stopTimer() {
        timer.stop();
        runBtn.setText("Ejecutar");
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }
}
