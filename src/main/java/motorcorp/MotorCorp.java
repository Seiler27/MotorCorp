package motorcorp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vista.Estilos;
import vista.FrmLogin;

/**
 * Clase principal del Sistema de Gestión MotorCorp.
 * Aplica el tema visual y abre la pantalla de inicio de sesión.
 */
public class MotorCorp {

    private static final Logger LOG = LoggerFactory.getLogger(MotorCorp.class);

    public static void main(String[] args) {
        LOG.info("Iniciando el sistema MotorCorp");
        Estilos.aplicarTema();
        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }
}
