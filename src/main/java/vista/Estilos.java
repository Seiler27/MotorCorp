package vista;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;

/**
 * Colores, tema visual e íconos comunes a todas las pantallas de MotorCorp.
 */
public final class Estilos {

    public static final Color AZUL = new Color(0x1F2A44);
    public static final Color AZUL_CLARO = new Color(0x2C3A5C);
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

    /** Botón plano del menú lateral (sin borde, texto blanco a la izquierda). */
    public static void botonMenu(JButton boton) {
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        boton.setBackground(AZUL);
        manito(boton);
    }

    /** Resalta en naranja el botón del módulo abierto. */
    public static void marcarBotonMenu(JButton boton, boolean activo) {
        boton.setBackground(activo ? NARANJA : AZUL);
        boton.setFont(boton.getFont().deriveFont(activo ? Font.BOLD : Font.PLAIN));
    }

    /** Tarjeta blanca con bordes redondeados (panel principal). */
    public static void tarjeta(JPanel panel) {
        panel.putClientProperty("FlatLaf.style", "arc: 16");
    }

    /** Tabla con filas altas, encabezado en negrita y sin edición directa. */
    public static void tabla(JTable tabla) {
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
    }
}
