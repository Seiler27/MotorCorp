package controlador;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ControlIntentos;
import util.Formato;
import util.Seguridad;
import util.Validador;
import vista.FrmLogin;
import vista.FrmPrincipal;

/**
 * Controlador del inicio de sesión (la "C" del patrón MVC).
 * Escucha los botones de FrmLogin, valida los datos con las clases de util
 * y consulta la base de datos mediante UsuarioDAO.
 */
public class ControladorLogin {

    private static final Logger LOG = LoggerFactory.getLogger(ControladorLogin.class);
    private static final int ID_ROL_ADMINISTRADOR = 1;
    private static final int MIN_CARACTERES_CLAVE = 6;

    private final FrmLogin vista;
    private final UsuarioDAO usuarioDAO;
    private final ControlIntentos intentos;

    /**
     * Recibe la vista y sus dependencias por el constructor (inversión de dependencias, SOLID).
     */
    public ControladorLogin(FrmLogin vista, UsuarioDAO usuarioDAO, ControlIntentos intentos) {
        this.vista = vista;
        this.usuarioDAO = usuarioDAO;
        this.intentos = intentos;
        this.vista.getBtnIngresar().addActionListener(e -> ingresar());
        this.vista.getBtnRegistrar().addActionListener(e -> registrarAdministrador());
    }

    /** Muestra la pantalla de inicio de sesión. */
    public void iniciar() {
        vista.setVisible(true);
    }

    /** Valida usuario y contraseña; si son correctos abre el panel principal. */
    private void ingresar() {
        String username = vista.getTxtUsuario().getText().trim();
        String password = new String(vista.getTxtPassword().getPassword());

        // 1. Campos obligatorios
        if (Validador.estaVacio(username) || Validador.estaVacio(password)) {
            vista.mostrarAviso("Ingresa tu usuario y contraseña.");
            return;
        }
        // 2. Bloqueo por intentos fallidos
        if (intentos.estaBloqueado(username)) {
            LOG.warn("Intento de ingreso de un usuario bloqueado: {}", username);
            vista.mostrarError("El usuario está bloqueado por " + ControlIntentos.MAX_INTENTOS
                    + " intentos fallidos.\nEspera 5 minutos e inténtalo de nuevo.");
            return;
        }
        // 3. Buscar el usuario y verificar la contraseña cifrada
        Usuario usuario = usuarioDAO.buscarPorUsername(username);
        if (usuario == null || !Seguridad.verificar(password, usuario.getPassword())) {
            intentos.registrarFallo(username);
            LOG.warn("Inicio de sesión fallido: {}", username);
            vista.getTxtPassword().setText("");
            if (intentos.estaBloqueado(username)) {
                vista.mostrarError("Usuario bloqueado por " + ControlIntentos.MAX_INTENTOS
                        + " intentos fallidos.\nEspera 5 minutos e inténtalo de nuevo.");
            } else {
                vista.mostrarError("Usuario o contraseña incorrectos.\nTe quedan "
                        + intentos.intentosRestantes(username) + " intento(s).");
            }
            return;
        }
        // 4. Ingreso correcto
        intentos.reiniciar(username);
        LOG.info("Inicio de sesión: {} ({})", usuario.getUsername(), usuario.getNombreRol());
        vista.dispose();
        new ControladorPrincipal(new FrmPrincipal(), usuario).iniciar();
    }

    /** Registra un administrador nuevo solo si se conoce el código secreto del taller. */
    private void registrarAdministrador() {
        String[] datos = vista.pedirDatosAdministrador();
        if (datos == null) {
            return;
        }
        String codigo = datos[0];
        String nombres = Formato.nombre(datos[1]);
        String username = datos[2].trim().toLowerCase();
        String clave = datos[3];
        String confirmacion = datos[4];

        if (Validador.estaVacio(codigo) || Validador.estaVacio(nombres)
                || Validador.estaVacio(username) || Validador.estaVacio(clave)) {
            vista.mostrarAviso("Completa todos los campos.");
            return;
        }
        if (!usuarioDAO.esCodigoSecretoValido(codigo)) {
            LOG.warn("Intento de registro de administrador con código secreto incorrecto");
            vista.mostrarError("El código secreto no es correcto.");
            return;
        }
        if (clave.length() < MIN_CARACTERES_CLAVE) {
            vista.mostrarAviso("La contraseña debe tener al menos " + MIN_CARACTERES_CLAVE + " caracteres.");
            return;
        }
        if (!clave.equals(confirmacion)) {
            vista.mostrarAviso("Las contraseñas no coinciden.");
            return;
        }
        if (usuarioDAO.buscarPorUsername(username) != null) {
            vista.mostrarAviso("El usuario \"" + username + "\" ya existe.");
            return;
        }
        Usuario nuevo = new Usuario(nombres, username, Seguridad.cifrar(clave), null, ID_ROL_ADMINISTRADOR);
        if (usuarioDAO.registrar(nuevo)) {
            vista.mostrarInfo("Administrador registrado correctamente.\nYa puedes iniciar sesión.");
            vista.getTxtUsuario().setText(username);
        } else {
            vista.mostrarError("No se pudo registrar el administrador.");
        }
    }
}
