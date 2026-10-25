package vista;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Ventanas de mensaje comunes a todas las pantallas (error, aviso, información y confirmación).
 */
public final class Mensajes {

    private static final String TITULO = "MotorCorp";

    private Mensajes() {
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.ERROR_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Devuelve true si el usuario responde "Sí". */
    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, TITULO, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }
}
