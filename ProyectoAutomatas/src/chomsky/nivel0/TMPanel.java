package chomsky.nivel0;

import chomsky.common.TapeMachinePanel;

/** Módulo del Nivel 0: máquina de Turing con cinta extensible. */
public class TMPanel extends TapeMachinePanel {
    public TMPanel(Runnable onBack) {
        super("Nivel 0 — Lenguajes recursivamente enumerables (Máquina de Turing)",
                "La cinta es infinita en ambos sentidos (se agregan blancos ␣ bajo demanda). El cabezal lee, escribe y se mueve; "
                        + "la máquina puede aceptar una cadena o calcular una función aritmética dejando el resultado en la cinta.",
                "Cinta extensible", TMCatalog.ENTRIES, onBack);
    }
}
