package chomsky.common;

import javax.swing.*;
import java.util.List;

/** Panel de simulación para las máquinas de cinta (Nivel 1 y Nivel 0). */
public class TapeMachinePanel extends SimulatorPanel {

    private final List<TapeEntry> entries;
    private final JTextArea defArea = new JTextArea();
    private TapeEntry entry;
    private TapeMachine.Simulation sim;

    public TapeMachinePanel(String title, String description, String tapeTitle,
                            List<TapeEntry> entries, Runnable onBack) {
        super(title, description, onBack);
        this.entries = entries;
        defArea.setFont(Theme.MONO_SMALL);
        defArea.setForeground(Theme.INK);
        defArea.setBackground(Theme.BG);
        defArea.setEditable(false);
        defArea.setLineWrap(true);
        defArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(defArea);
        scroll.setBorder(titled("Definición formal: δ(estado, símbolo) = (estado, escribir, mover)"));
        build(new TapeView(tapeTitle, () -> entry == null ? null : entry.machine(), () -> sim), scroll);
    }

    @Override
    protected String[] machineNames() {
        String[] names = new String[entries.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = entries.get(i).name();
        }
        return names;
    }

    @Override
    protected String[] examplesFor(int machine) {
        return entries.get(machine).examples();
    }

    @Override
    protected void onMachineSelected(int machine) {
        entry = entries.get(machine);
        sim = null;
        TapeMachine tm = entry.machine();
        StringBuilder sb = new StringBuilder();
        sb.append("Estado inicial: ").append(tm.start).append("\n");
        sb.append("Estado de aceptación: ").append(tm.accept).append("\n");
        sb.append("Alfabeto de entrada: ").append(tm.inputAlphabet).append("\n");
        if (tm.bounded) {
            sb.append("Alfabeto de la cinta: entrada + marcas + [ ]\n");
            sb.append("Cinta acotada: el cabezal no sale de [ … ]\n");
        } else {
            sb.append("Alfabeto de la cinta: entrada + marcas + ␣ (blanco)\n");
            sb.append("Cinta infinita a ambos lados\n");
        }
        sb.append("Movimientos: L = izquierda, R = derecha, S = quieto\n");
        sb.append("Sin transición aplicable → rechaza\n\n");
        for (TapeMachine.Transition t : tm.transitions) {
            sb.append(t.describe()).append("\n");
        }
        defArea.setText(sb.toString());
        defArea.setCaretPosition(0);
    }

    @Override
    protected void start(int machine, String input) {
        entry = entries.get(machine);
        TapeMachine tm = entry.machine();
        for (char c : input.toCharArray()) {
            if (tm.inputAlphabet.indexOf(c) < 0) {
                throw new IllegalArgumentException("El símbolo '" + c + "' no pertenece al alfabeto de entrada {"
                        + tm.inputAlphabet + "} de esta máquina.");
            }
        }
        sim = tm.newSimulation(input);
        log("Cinta inicial: " + sim.output() + "   cabezal en la posición 0");
        log("--------------------------------------------");
    }

    @Override
    protected Result advance() {
        char read = sim.current();
        String stateBefore = sim.state;
        Result r = sim.step();
        if (sim.last != null) {
            log("Paso " + sim.steps + ": en " + stateBefore + " leo '" + read + "'  →  " + sim.last.describe());
        }
        if (r != Result.RUNNING) {
            log(sim.message);
        }
        return r;
    }

    @Override
    protected String finalMessage() {
        String msg = sim.message;
        if (entry.function() && sim.result == Result.ACCEPTED) {
            String out = sim.output();
            if (out.isEmpty()) {
                out = "ε";
            }
            msg = "resultado en la cinta: " + out;
        }
        return msg;
    }
}
