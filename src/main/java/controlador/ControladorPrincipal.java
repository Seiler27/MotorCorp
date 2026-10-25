package controlador;

import dao.ClienteDAOImpl;
import dao.ComprobanteDAOImpl;
import dao.DashboardDAOImpl;
import dao.OrdenServicioDAOImpl;
import dao.ProductoDAOImpl;
import dao.UsuarioDAOImpl;
import dao.VehiculoDAOImpl;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JButton;
import modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ControlIntentos;
import util.Modulo;
import util.Permisos;
import vista.FrmLogin;
import vista.FrmPrincipal;
import vista.PnlClientes;
import vista.PnlCola;
import vista.PnlHistorial;
import vista.PnlInventario;
import vista.PnlPos;
import vista.PnlEnConstruccion;
import vista.PnlInicio;

/**
 * Controlador de la ventana principal: menú lateral, permisos por rol,
 * cambio de módulos y cierre de sesión.
 */
public class ControladorPrincipal {

    private static final Logger LOG = LoggerFactory.getLogger(ControladorPrincipal.class);

    private final FrmPrincipal vista;
    private final Usuario usuario;
    private final Map<Modulo, JButton> botones = new EnumMap<>(Modulo.class);
    private final ControladorInicio controladorInicio;
    /** Acción que se ejecuta cada vez que se abre un módulo (recargar sus datos). */
    private final Map<Modulo, Runnable> alAbrir = new EnumMap<>(Modulo.class);

    public ControladorPrincipal(FrmPrincipal vista, Usuario usuario) {
        this.vista = vista;
        this.usuario = usuario;

        botones.put(Modulo.INICIO, vista.getBtnInicio());
        botones.put(Modulo.CLIENTES, vista.getBtnClientes());
        botones.put(Modulo.COLA, vista.getBtnCola());
        botones.put(Modulo.INVENTARIO, vista.getBtnInventario());
        botones.put(Modulo.HISTORIAL, vista.getBtnHistorial());
        botones.put(Modulo.POS, vista.getBtnPos());
        botones.put(Modulo.REPORTES, vista.getBtnReportes());

        // Pantallas de cada módulo
        PnlInicio pnlInicio = new PnlInicio();
        vista.agregarModulo(pnlInicio, Modulo.INICIO.name());
        controladorInicio = new ControladorInicio(pnlInicio, new DashboardDAOImpl(), usuario);
        alAbrir.put(Modulo.INICIO, controladorInicio::cargar);

        PnlClientes pnlClientes = new PnlClientes();
        vista.agregarModulo(pnlClientes, Modulo.CLIENTES.name());
        ControladorClientes controladorClientes = new ControladorClientes(pnlClientes, new ClienteDAOImpl(), new VehiculoDAOImpl());
        alAbrir.put(Modulo.CLIENTES, controladorClientes::cargar);

        PnlCola pnlCola = new PnlCola();
        vista.agregarModulo(pnlCola, Modulo.COLA.name());
        ControladorCola controladorCola = new ControladorCola(pnlCola, new OrdenServicioDAOImpl(),
                new VehiculoDAOImpl(), new ClienteDAOImpl(), new UsuarioDAOImpl());
        alAbrir.put(Modulo.COLA, controladorCola::cargar);

        PnlHistorial pnlHistorial = new PnlHistorial();
        vista.agregarModulo(pnlHistorial, Modulo.HISTORIAL.name());
        ControladorHistorial controladorHistorial = new ControladorHistorial(pnlHistorial, new OrdenServicioDAOImpl(),
                new ProductoDAOImpl(), new UsuarioDAOImpl(), new VehiculoDAOImpl(), usuario);
        alAbrir.put(Modulo.HISTORIAL, controladorHistorial::cargar);

        PnlInventario pnlInventario = new PnlInventario();
        vista.agregarModulo(pnlInventario, Modulo.INVENTARIO.name());
        ControladorInventario controladorInventario = new ControladorInventario(pnlInventario, new ProductoDAOImpl(), usuario);
        alAbrir.put(Modulo.INVENTARIO, controladorInventario::cargar);

        PnlPos pnlPos = new PnlPos();
        vista.agregarModulo(pnlPos, Modulo.POS.name());
        ControladorPos controladorPos = new ControladorPos(pnlPos, new OrdenServicioDAOImpl(), new ProductoDAOImpl(),
                new ComprobanteDAOImpl(), usuario);
        alAbrir.put(Modulo.POS, controladorPos::cargar);

        for (Modulo modulo : Modulo.values()) {
            if (!alAbrir.containsKey(modulo)) {
                vista.agregarModulo(new PnlEnConstruccion(botones.get(modulo).getText()), modulo.name());
            }
        }

        // Permisos por rol y eventos de los botones
        botones.forEach((modulo, boton) -> {
            boton.setEnabled(Permisos.puedeAcceder(usuario.getNombreRol(), modulo));
            boton.addActionListener(e -> abrir(modulo));
        });
        vista.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
    }

    /** Muestra la ventana principal con el módulo de Inicio abierto. */
    public void iniciar() {
        vista.getLblUsuarioActual().setText(usuario.getNombres());
        vista.getLblRolActual().setText(nombreVisible(usuario.getNombreRol()));
        abrir(Modulo.INICIO);
        vista.setVisible(true);
    }

    /** Muestra el nombre del rol con tildes (en la base se guarda sin tildes). */
    private static String nombreVisible(String rol) {
        return switch (rol) {
            case "Recepcion" -> "Recepción";
            case "Mecanico" -> "Mecánico";
            default -> rol;
        };
    }

    /** Cambia al módulo indicado si el rol del usuario tiene permiso. */
    private void abrir(Modulo modulo) {
        if (!Permisos.puedeAcceder(usuario.getNombreRol(), modulo)) {
            LOG.warn("{} intentó abrir {} sin permiso", usuario.getUsername(), modulo);
            return;
        }
        vista.mostrarModulo(modulo.name());
        vista.marcarActivo(botones.get(modulo));
        Runnable recargar = alAbrir.get(modulo);
        if (recargar != null) {
            recargar.run();
        }
    }

    /** Cierra la sesión y vuelve a la pantalla de inicio de sesión. */
    private void cerrarSesion() {
        if (vista.confirmar("¿Deseas cerrar la sesión?")) {
            LOG.info("Cierre de sesión: {}", usuario.getUsername());
            vista.dispose();
            new ControladorLogin(new FrmLogin(), new UsuarioDAOImpl(), new ControlIntentos()).iniciar();
        }
    }
}
