package motorcorp;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Seguridad;

/**
 * Clase principal del Sistema de Gestión MotorCorp.
 */
public class MotorCorp {

    private static final Logger LOG = LoggerFactory.getLogger(MotorCorp.class);

    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAOImpl();

        // 1. Simula un login con el usuario admin
        Usuario u = dao.buscarPorUsername("admin");
        if (u != null && Seguridad.verificar("Admin123", u.getPassword())) {
            LOG.info("Login correcto: {} - rol {}", u.getNombres(), u.getNombreRol());
        } else {
            LOG.warn("Usuario o contraseña incorrectos");
        }

        // 2. Lista los mecánicos
        LOG.info("Mecánicos: {}", dao.listarPorRol("Mecanico"));
    }
}