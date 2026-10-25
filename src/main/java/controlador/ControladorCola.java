package controlador;

import dao.ClienteDAO;
import dao.OrdenServicioDAO;
import dao.UsuarioDAO;
import dao.VehiculoDAO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.OrdenServicio;
import modelo.Usuario;
import modelo.Vehiculo;
import util.EstadoOrden;
import util.Formato;
import util.Validador;
import vista.Estilos;
import vista.Mensajes;
import vista.PnlCola;

/**
 * Controlador del módulo Cola de espera (RF02 y RF03): registra el ingreso de las motos
 * y las atiende por orden de llegada (FIFO).
 */
public class ControladorCola {

    private final PnlCola vista;
    private final OrdenServicioDAO ordenDAO;
    private final VehiculoDAO vehiculoDAO;
    private final ClienteDAO clienteDAO;
    private final UsuarioDAO usuarioDAO;

    private List<OrdenServicio> cola = new ArrayList<>();
    private Vehiculo vehiculoEncontrado;

    public ControladorCola(PnlCola vista, OrdenServicioDAO ordenDAO, VehiculoDAO vehiculoDAO,
            ClienteDAO clienteDAO, UsuarioDAO usuarioDAO) {
        this.vista = vista;
        this.ordenDAO = ordenDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.clienteDAO = clienteDAO;
        this.usuarioDAO = usuarioDAO;

        vista.getBtnBuscarPlaca().addActionListener(e -> buscarPlaca());
        vista.getTxtPlacaIngreso().addActionListener(e -> buscarPlaca());   // Enter en el campo
        vista.getBtnRegistrarIngreso().addActionListener(e -> registrarIngreso());
        vista.getBtnAsignar().addActionListener(e -> asignarMecanico());
        vista.getBtnIniciar().addActionListener(e -> iniciarReparacion());
        vista.getTblCola().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarOrden();
            }
        });
    }

    /** Se llama cada vez que se abre el módulo. */
    public void cargar() {
        vista.getCboMecanico().setModel(new DefaultComboBoxModel<>(
                usuarioDAO.listarPorRol("Mecanico").toArray(new Usuario[0])));
        limpiarIngreso();
        listarCola();
    }

    private void listarCola() {
        cola = ordenDAO.listarPorEstados(EstadoOrden.EN_ESPERA, EstadoOrden.EN_REPARACION, EstadoOrden.FINALIZADO);
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura(
                "#", "Placa", "Cliente", "Falla / diagnóstico", "Estado", "Mecánico", "Espera");
        int turno = 1;
        int enEspera = 0;
        int enReparacion = 0;
        int listas = 0;
        for (OrdenServicio o : cola) {
            modelo.addRow(new Object[]{turno++, o.getPlaca(), o.getNombreCliente(), o.getDiagnostico(), o.getEstado(),
                o.getNombreMecanico() == null ? "Sin asignar" : o.getNombreMecanico(),
                tiempoDeEspera(o.getFechaIngreso())});
            switch (o.getEstado()) {
                case EstadoOrden.EN_ESPERA -> enEspera++;
                case EstadoOrden.EN_REPARACION -> enReparacion++;
                default -> listas++;
            }
        }
        vista.getTblCola().setModel(modelo);
        Estilos.columnaEstado(vista.getTblCola(), 4);
        int[] anchos = {28, 68, 120, 130, 120, 92, 70};
        for (int i = 0; i < anchos.length; i++) {
            vista.getTblCola().getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        vista.getLblResumen().setText("En espera: " + enEspera + "   ·   En reparación: " + enReparacion
                + "   ·   Listas: " + listas);
        seleccionarOrden();
    }

    /** Tiempo transcurrido desde la llegada, ej: "1 h 20 min". */
    private static String tiempoDeEspera(LocalDateTime llegada) {
        long minutos = Duration.between(llegada, LocalDateTime.now()).toMinutes();
        if (minutos < 60) {
            return minutos + " min";
        }
        if (minutos < 60 * 24) {
            return (minutos / 60) + " h " + (minutos % 60) + " min";
        }
        return (minutos / (60 * 24)) + " días";
    }

    // ------------------------------------------------------------------ Ingreso

    private void buscarPlaca() {
        String placa = Formato.placa(vista.getTxtPlacaIngreso().getText());
        vista.getTxtPlacaIngreso().setText(placa);
        vehiculoEncontrado = Validador.estaVacio(placa) ? null : vehiculoDAO.buscarPorPlaca(placa);
        if (vehiculoEncontrado == null) {
            vista.getLblVehiculo().setForeground(Estilos.ROJO);
            vista.getLblVehiculo().setText("<html>No existe una moto con la placa " + placa
                    + ".<br>Regístrala primero en el módulo Clientes.</html>");
            return;
        }
        Cliente dueno = clienteDAO.buscarPorId(vehiculoEncontrado.getIdCliente());
        vista.getLblVehiculo().setForeground(Estilos.VERDE);
        vista.getLblVehiculo().setText("<html><b>" + vehiculoEncontrado.getMarca() + " "
                + (vehiculoEncontrado.getModelo() == null ? "" : vehiculoEncontrado.getModelo())
                + "</b><br>" + (dueno == null ? "" : dueno.getNombres() + " " + dueno.getApellidos()
                + " · " + dueno.getTelefono()) + "</html>");
        vista.getTxaFalla().requestFocus();
    }

    private void registrarIngreso() {
        if (vehiculoEncontrado == null) {
            Mensajes.aviso(vista, "Primero busca la placa de la moto.");
            return;
        }
        String falla = vista.getTxaFalla().getText().trim();
        if (Validador.estaVacio(falla)) {
            Mensajes.aviso(vista, "Describe la falla que reporta el cliente.");
            return;
        }
        if (ordenDAO.tieneOrdenActiva(vehiculoEncontrado.getIdVehiculo())) {
            Mensajes.aviso(vista, "La moto " + vehiculoEncontrado.getPlaca() + " ya está en la cola del taller.");
            return;
        }
        if (ordenDAO.registrarIngreso(vehiculoEncontrado.getIdVehiculo(), falla)) {
            Mensajes.info(vista, "Moto " + vehiculoEncontrado.getPlaca() + " agregada a la cola.");
            limpiarIngreso();
            listarCola();
        } else {
            Mensajes.error(vista, "No se pudo registrar el ingreso.");
        }
    }

    private void limpiarIngreso() {
        vehiculoEncontrado = null;
        vista.getTxtPlacaIngreso().setText("");
        vista.getTxaFalla().setText("");
        vista.getLblVehiculo().setForeground(Estilos.GRIS);
        vista.getLblVehiculo().setText("Busca la placa para ver la moto y su dueño.");
    }

    // ------------------------------------------------------------------ Atención

    private OrdenServicio ordenSeleccionada() {
        int fila = vista.getTblCola().getSelectedRow();
        return fila >= 0 && fila < cola.size() ? cola.get(fila) : null;
    }

    private void seleccionarOrden() {
        OrdenServicio orden = ordenSeleccionada();
        boolean activa = orden != null && !EstadoOrden.FINALIZADO.equals(orden.getEstado());
        vista.getBtnAsignar().setEnabled(activa);
        vista.getBtnIniciar().setEnabled(orden != null && EstadoOrden.EN_ESPERA.equals(orden.getEstado()));
        if (orden != null && orden.getIdMecanico() != null) {
            for (int i = 0; i < vista.getCboMecanico().getItemCount(); i++) {
                if (vista.getCboMecanico().getItemAt(i).getIdUsuario() == orden.getIdMecanico()) {
                    vista.getCboMecanico().setSelectedIndex(i);
                }
            }
        }
    }

    private void asignarMecanico() {
        OrdenServicio orden = ordenSeleccionada();
        Usuario mecanico = (Usuario) vista.getCboMecanico().getSelectedItem();
        if (orden == null || mecanico == null) {
            return;
        }
        if (ordenDAO.asignarMecanico(orden.getIdOrden(), mecanico.getIdUsuario())) {
            listarCola();
        }
    }

    /** Pasa la orden a "En reparación". Si no tiene mecánico, asigna el seleccionado. */
    private void iniciarReparacion() {
        OrdenServicio orden = ordenSeleccionada();
        if (orden == null) {
            return;
        }
        Usuario mecanico = (Usuario) vista.getCboMecanico().getSelectedItem();
        if (orden.getIdMecanico() == null) {
            if (mecanico == null) {
                Mensajes.aviso(vista, "Asigna un mecánico antes de iniciar la reparación.");
                return;
            }
            ordenDAO.asignarMecanico(orden.getIdOrden(), mecanico.getIdUsuario());
        }
        if (ordenDAO.cambiarEstado(orden.getIdOrden(), EstadoOrden.EN_REPARACION)) {
            listarCola();
        }
    }
}
