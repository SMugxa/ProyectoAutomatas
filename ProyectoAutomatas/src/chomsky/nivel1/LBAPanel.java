package chomsky.nivel1;

import chomsky.common.TapeMachinePanel;

/** Módulo del Nivel 1: autómata linealmente acotado con cinta delimitada por [ y ]. */
public class LBAPanel extends TapeMachinePanel {
    public LBAPanel(Runnable onBack) {
        super("Nivel 1 — Lenguajes sensibles al contexto (Autómata Linealmente Acotado)",
                "La cinta mide exactamente lo que ocupa la entrada y está cerrada por los marcadores de frontera [ y ]. "
                        + "El cabezal se mueve en ambos sentidos y reescribe símbolos, pero nunca sale de la zona acotada.",
                "Cinta acotada con delimitadores", LBACatalog.ENTRIES, onBack);
    }
}
