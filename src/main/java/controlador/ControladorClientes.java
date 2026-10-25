package controlador;

import dao.ClienteDAO;
import dao.VehiculoDAO;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.Vehiculo;
import util.Formato;
import util.ValidacionCliente;
import vista.Mensajes;
import vista.PnlClientes;

/**
 * Controlador del módulo de Clientes: registrar, buscar, editar y eliminar
 * clientes y sus motos (RF01).
 */
public class ControladorClientes {

    private final PnlClientes vista;
    private final ClienteDAO clienteDAO;
    private final VehiculoDAO vehiculoDAO;

    /** Clientes y motos que se muestran en las tablas (en el mismo orden que las filas). */
    private List<Cliente> clientes = new ArrayList<>();
    private List<Vehiculo> motos = new ArrayList<>();
    private Cliente clienteSeleccionado;
    private Vehiculo motoSeleccionada;

    public ControladorClientes(PnlClientes vista, ClienteDAO clienteDAO, VehiculoDAO vehiculoDAO) {
        this.vista = vista;
        this.clienteDAO = clienteDAO;
        this.vehiculoDAO = vehiculoDAO;

        vista.getBtnGuardar().addActionListener(e -> guardar());
        vista.getBtnNuevo().addActionListener(e -> nuevo());
        vista.getBtnEliminar().addActionListener(e -> eliminarCliente());
        vista.getBtnQuitarMoto().addActionListener(e -> quitarMoto());
        vista.getTblClientes().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarCliente();
            }
        });
        vista.getTblMotos().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarMoto();
            }
        });
        // Búsqueda mientras se escribe
        vista.getTxtBuscar().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { listarClientes(); }
            @Override public void removeUpdate(DocumentEvent e) { listarClientes(); }
            @Override public void changedUpdate(DocumentEvent e) { listarClientes(); }
        });
    }

    /** Se llama cada vez que se abre el módulo. */
    public void cargar() {
        nuevo();
    }

    // ------------------------------------------------------------------ Listados

    private void listarClientes() {
        clientes = clienteDAO.buscar(vista.getTxtBuscar().getText());
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("DNI", "Nombres", "Apellidos", "Celular", "Correo");
        for (Cliente c : clientes) {
            modelo.addRow(new Object[]{c.getDni(), c.getNombres(), c.getApellidos(), c.getTelefono(),
                c.getCorreo() == null ? "" : c.getCorreo()});
        }
        vista.getTblClientes().setModel(modelo);
        clienteSeleccionado = null;
        listarMotos();
    }

    private void listarMotos() {
        motos = clienteSeleccionado == null ? new ArrayList<>() : vehiculoDAO.listarPorCliente(clienteSeleccionado.getIdCliente());
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("Placa", "Marca", "Modelo");
        for (Vehiculo v : motos) {
            modelo.addRow(new Object[]{v.getPlaca(), v.getMarca(), v.getModelo() == null ? "" : v.getModelo()});
        }
        vista.getTblMotos().setModel(modelo);
        motoSeleccionada = null;
        vista.getLblMotos().setText(clienteSeleccionado == null ? "Motos del cliente"
                : "Motos de " + clienteSeleccionado.getNombres() + " " + clienteSeleccionado.getApellidos());
        vista.getBtnQuitarMoto().setEnabled(false);
    }

    // ------------------------------------------------------------------ Selección

    /** Al hacer clic en un cliente, sus datos pasan al formulario para editarlos. */
    private void seleccionarCliente() {
        int fila = vista.getTblClientes().getSelectedRow();
        if (fila < 0 || fila >= clientes.size()) {
            return;
        }
        clienteSeleccionado = clientes.get(fila);
        vista.getTxtDni().setText(clienteSeleccionado.getDni());
        vista.getTxtNombres().setText(clienteSeleccionado.getNombres());
        vista.getTxtApellidos().setText(clienteSeleccionado.getApellidos());
        vista.getTxtCelular().setText(clienteSeleccionado.getTelefono());
        vista.getTxtCorreo().setText(clienteSeleccionado.getCorreo());
        vista.limpiarMoto();
        listarMotos();
        vista.getBtnEliminar().setEnabled(true);
    }

    /** Al hacer clic en una moto, sus datos pasan al formulario para editarlos. */
    private void seleccionarMoto() {
        int fila = vista.getTblMotos().getSelectedRow();
        if (fila < 0 || fila >= motos.size()) {
            return;
        }
        motoSeleccionada = motos.get(fila);
        vista.getTxtPlaca().setText(motoSeleccionada.getPlaca());
        vista.getTxtMarca().setText(motoSeleccionada.getMarca());
        vista.getTxtModelo().setText(motoSeleccionada.getModelo());
        vista.getBtnQuitarMoto().setEnabled(true);
    }

    // ------------------------------------------------------------------ Acciones

    private void nuevo() {
        vista.getTxtBuscar().setText("");
        listarClientes();
        vista.limpiarFormulario();
        vista.getBtnEliminar().setEnabled(false);
    }

    /** Registra un cliente nuevo o actualiza el seleccionado; también guarda su moto. */
    private void guardar() {
        String dni = vista.getTxtDni().getText().trim();
        String nombres = Formato.nombre(vista.getTxtNombres().getText());
        String apellidos = Formato.nombre(vista.getTxtApellidos().getText());
        String celular = vista.getTxtCelular().getText().trim();
        String correo = vista.getTxtCorreo().getText().trim().toLowerCase();
        String placa = Formato.placa(vista.getTxtPlaca().getText());
        String marca = Formato.nombre(vista.getTxtMarca().getText());
        String modelo = vista.getTxtModelo().getText().trim();

        // 1. Reglas de negocio
        List<String> errores = ValidacionCliente.validarCliente(dni, nombres, apellidos, celular, correo);
        boolean conMoto = ValidacionCliente.hayDatosDeMoto(placa, marca, modelo);
        if (conMoto) {
            errores.addAll(ValidacionCliente.validarMoto(placa, marca));
        }
        int idCliente = clienteSeleccionado == null ? 0 : clienteSeleccionado.getIdCliente();
        int idMoto = motoSeleccionada == null ? 0 : motoSeleccionada.getIdVehiculo();
        if (errores.isEmpty() && clienteDAO.existeDni(dni, idCliente)) {
            errores.add("Ya existe un cliente con el DNI " + dni + ".");
        }
        if (errores.isEmpty() && conMoto && vehiculoDAO.existePlaca(placa, idMoto)) {
            errores.add("La placa " + placa + " ya está registrada.");
        }
        if (!errores.isEmpty()) {
            Mensajes.aviso(vista, "Revisa los datos:\n• " + String.join("\n• ", errores));
            return;
        }

        Cliente cliente = new Cliente(dni, nombres, apellidos, celular, correo);
        Vehiculo moto = conMoto ? new Vehiculo(placa, marca, modelo, idCliente) : null;

        // 2. Guardar
        boolean ok;
        if (clienteSeleccionado == null) {
            ok = clienteDAO.registrar(cliente, moto) > 0;      // cliente + moto en una transacción
        } else {
            cliente.setIdCliente(idCliente);
            ok = clienteDAO.actualizar(cliente);
            if (ok && moto != null) {
                if (motoSeleccionada == null) {
                    ok = vehiculoDAO.registrar(moto);
                } else {
                    moto.setIdVehiculo(idMoto);
                    ok = vehiculoDAO.actualizar(moto);
                }
            }
        }

        if (ok) {
            Mensajes.info(vista, clienteSeleccionado == null ? "Cliente registrado correctamente." : "Cliente actualizado correctamente.");
            nuevo();
            vista.getTxtBuscar().setText(dni);
        } else {
            Mensajes.error(vista, "No se pudo guardar el cliente. Revisa la bitácora para más detalles.");
        }
    }

    private void eliminarCliente() {
        if (clienteSeleccionado == null) {
            return;
        }
        String nombre = clienteSeleccionado.getNombres() + " " + clienteSeleccionado.getApellidos();
        if (!Mensajes.confirmar(vista, "¿Eliminar al cliente " + nombre + " y sus motos?")) {
            return;
        }
        if (clienteDAO.eliminar(clienteSeleccionado.getIdCliente())) {
            Mensajes.info(vista, "Cliente eliminado.");
            nuevo();
        } else {
            Mensajes.error(vista, "No se puede eliminar: el cliente tiene atenciones registradas en el taller.\n"
                    + "Su historial debe conservarse.");
        }
    }

    private void quitarMoto() {
        if (motoSeleccionada == null) {
            return;
        }
        if (!Mensajes.confirmar(vista, "¿Quitar la moto " + motoSeleccionada.getPlaca() + "?")) {
            return;
        }
        if (vehiculoDAO.eliminar(motoSeleccionada.getIdVehiculo())) {
            vista.limpiarMoto();
            listarMotos();
        } else {
            Mensajes.error(vista, "No se puede quitar: la moto tiene atenciones registradas en el taller.");
        }
    }
}
