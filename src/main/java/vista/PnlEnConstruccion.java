package vista;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Pantalla provisional para los módulos que aún se están desarrollando.
 */
public class PnlEnConstruccion extends JPanel {

    public PnlEnConstruccion(String modulo) {
        super(new GridBagLayout());
        setBackground(Estilos.FONDO);
        JLabel mensaje = new JLabel("<html><div style='text-align:center'><b style='font-size:20px'>"
                + modulo + "</b><br><br>Este módulo se implementará en el siguiente paso.</div></html>");
        mensaje.setForeground(Estilos.GRIS);
        add(mensaje);
    }
}
