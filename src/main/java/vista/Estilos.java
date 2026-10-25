package vista;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Colores, tema visual e íconos comunes a todas las pantallas de MotorCorp.
 */
public final class Estilos {

    public static final Color AZUL = new Color(0x1F2A44);
    public static final Color NARANJA = new Color(0xF28C28);
    public static final Color GRIS = new Color(0x6B7280);
    public static final Color FONDO = new Color(0xF4F6FA);
    public static final Color VERDE = new Color(0x2E7D32);
    public static final Color ROJO = new Color(0xC62828);

    private Estilos() {
    }

    public static void aplicarTema() {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.focusColor", NARANJA);
        UIManager.put("Component.focusedBorderColor", NARANJA);
        UIManager.put("Table.selectionBackground", NARANJA);
    }

    /** Logo */
    public static Image icono() {
        return new ImageIcon(Estilos.class.getResource("/img/icono.png")).getImage();
    }

    /** Mano al pasar el raton. */
    public static void manito(JComponent componente) {
        componente.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
