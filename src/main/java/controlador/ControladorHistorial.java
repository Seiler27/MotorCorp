package controlador;

import dao.OrdenServicioDAO;
import dao.ProductoDAO;
import dao.UsuarioDAO;
import dao.VehiculoDAO;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.table.DefaultTableModel;
import modelo.AtencionResumen;
import modelo.DetalleOrden;
import modelo.OrdenServicio;
import modelo.Producto;
import modelo.Usuario;
import modelo.Vehiculo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.EstadoOrden;
import util.Formato;
import util.Validador;
import vista.Estilos;
import vista.Mensajes;
import vista.PnlHistorial;

/**
 * Controlador del módulo Historial del taller (RF04 a RF08): diagnóstico, verificación
 * de stock, repuestos usados, cierre de la orden e historial de cada moto.
 */
public class ControladorHistorial {

    private static final Logger LOG = LoggerFactory.getLogger(ControladorHistorial.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PnlHistorial vista;
    private final OrdenServicioDAO ordenDAO;
    private final ProductoDAO productoDAO;
    private final UsuarioDAO usuarioDAO;
    private final VehiculoDAO vehiculoDAO;
    private final Usuario usuario;

    private List<OrdenServicio> activas = new ArrayList<>();
    private OrdenServicio orden;
    /** Repuestos ya guardados en la orden (no se pueden quitar). */
    private List<DetalleOrden> registrados = new ArrayList<>();
    /** Repuestos agregados ahora; se guardan al completar el servicio. */
    private final List<DetalleOrden> nuevos = new ArrayList<>();

    public ControladorHistorial(PnlHistorial vista, OrdenServicioDAO ordenDAO, ProductoDAO productoDAO,
            UsuarioDAO usuarioDAO, VehiculoDAO vehiculoDAO, Usuario usuario) {
        this.vista = vista;
        this.ordenDAO = ordenDAO;
        this.productoDAO = productoDAO;
        this.usuarioDAO = usuarioDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.usuario = usuario;

        vista.getCboEstado().setModel(new DefaultComboBoxModel<>(new String[]{EstadoOrden.EN_ESPERA, EstadoOrden.EN_REPARACION}));
        vista.getCboProducto().setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> lista, Object valor, int i,
                    boolean sel, boolean foco) {
                super.getListCellRendererComponent(lista, valor, i, sel, foco);
                if (valor instanceof Producto p) {
                    setText(p.getNombre() + "  ·  " + Formato.soles(p.getPrecioUnitario()) + "  ·  stock " + p.getStockActual());
                }
                return this;
            }
        });
        vista.getTblActivas().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarOrden();
            }
        });
        vista.getBtnAgregar().addActionListener(e -> agregarRepuesto());
        vista.getBtnQuitarRepuesto().addActionListener(e -> quitarRepuesto());
        vista.getBtnGuardarAvance().addActionListener(e -> guardarAvance());
        vista.getBtnCompletar().addActionListener(e -> completarServicio());
        vista.getBtnBuscarHistorial().addActionListener(e -> buscarHistorial());
        vista.getTxtBuscarPlaca().addActionListener(e -> buscarHistorial());
    }

    /** Se llama cada vez que se abre el módulo. */
    public void cargar() {
        vista.getCboMecanicoAt().setModel(new DefaultComboBoxModel<>(
                usuarioDAO.listarPorRol("Mecanico").toArray(new Usuario[0])));
        vista.getCboProducto().setModel(new DefaultComboBoxModel<>(
                productoDAO.buscar("").stream().filter(p -> !"Motocicleta".equals(p.getCategoria()))
                        .toArray(Producto[]::new)));
        listarActivas();
    }

    private void listarActivas() {
        activas = ordenDAO.listarPorEstados(EstadoOrden.EN_ESPERA, EstadoOrden.EN_REPARACION);
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("N°", "Placa", "Cliente", "Estado");
        for (OrdenServicio o : activas) {
            modelo.addRow(new Object[]{o.getIdOrden(), o.getPlaca(), o.getNombreCliente(), o.getEstado()});
        }
        vista.getTblActivas().setModel(modelo);
        vista.getTblActivas().getColumnModel().getColumn(0).setMaxWidth(40);
        Estilos.columnaEstado(vista.getTblActivas(), 3);
        orden = null;
        mostrarOrden();
    }

    // ------------------------------------------------------------------ Orden seleccionada

    private void seleccionarOrden() {
        int fila = vista.getTblActivas().getSelectedRow();
        if (fila < 0 || fila >= activas.size()) {
            return;
        }
        orden = activas.get(fila);
        mostrarOrden();
        mostrarHistorial(orden.getIdVehiculo(), orden.getPlaca());
    }

    private void mostrarOrden() {
        nuevos.clear();
        vista.habilitarAtencion(orden != null);
        if (orden == null) {
            vista.getLblOrden().setText("Selecciona una orden activa");
            vista.getTxaDiagnostico().setText("");
            registrados = new ArrayList<>();
            mostrarDetalle();
            return;
        }
        vista.getLblOrden().setText("<html>Orden #" + orden.getIdOrden() + "  ·  " + orden.getPlaca() + "  ·  "
                + orden.getMoto() + "<br><span style='font-weight:normal'>Cliente: " + orden.getNombreCliente()
                + "  ·  Cel. " + orden.getTelefonoCliente() + "</span></html>");
        vista.getTxaDiagnostico().setText(orden.getDiagnostico());
        vista.getCboEstado().setSelectedItem(orden.getEstado());
        seleccionarMecanico(orden.getIdMecanico() != null ? orden.getIdMecanico()
                : "Mecanico".equals(usuario.getNombreRol()) ? usuario.getIdUsuario() : -1);
        registrados = ordenDAO.listarDetalle(orden.getIdOrden());
        mostrarDetalle();
    }

    private void seleccionarMecanico(int idMecanico) {
        vista.getCboMecanicoAt().setSelectedIndex(-1);
        for (int i = 0; i < vista.getCboMecanicoAt().getItemCount(); i++) {
            if (vista.getCboMecanicoAt().getItemAt(i).getIdUsuario() == idMecanico) {
                vista.getCboMecanicoAt().setSelectedIndex(i);
            }
        }
    }

    private void mostrarDetalle() {
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("Repuesto / servicio", "Cant.", "Subtotal", "");
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleOrden d : registrados) {
            modelo.addRow(new Object[]{d.getNombreProducto(), d.getCantidad(), Formato.soles(d.getSubtotal()), "Registrado"});
            total = total.add(d.getSubtotal());
        }
        for (DetalleOrden d : nuevos) {
            modelo.addRow(new Object[]{d.getNombreProducto(), d.getCantidad(), Formato.soles(d.getSubtotal()), "Nuevo"});
            total = total.add(d.getSubtotal());
        }
        vista.getTblDetalle().setModel(modelo);
        vista.getTblDetalle().getColumnModel().getColumn(1).setMaxWidth(50);
        vista.getLblTotal().setText("Total repuestos: " + Formato.soles(total));
    }

    // ------------------------------------------------------------------ Repuestos

    /** Verifica el stock antes de agregar el repuesto (RF05); si no alcanza, avisa (RF06). */
    private void agregarRepuesto() {
        Producto elegido = (Producto) vista.getCboProducto().getSelectedItem();
        if (orden == null || elegido == null) {
            return;
        }
        int cantidad = (Integer) vista.getSpnCantidad().getValue();
        Producto actual = productoDAO.buscarPorId(elegido.getIdProducto());   // stock en tiempo real
        int yaAgregado = nuevos.stream().filter(d -> d.getIdProducto() == elegido.getIdProducto())
                .mapToInt(DetalleOrden::getCantidad).sum();
        int disponible = actual == null ? 0 : actual.getStockActual() - yaAgregado;
        if (cantidad > disponible) {
            LOG.warn("Sin stock de {} para la orden {} (pedido {}, disponible {})",
                    elegido.getNombre(), orden.getIdOrden(), cantidad, disponible);
            Mensajes.aviso(vista, "No hay stock suficiente de " + elegido.getNombre()
                    + ".\nDisponible: " + Math.max(disponible, 0) + " unidad(es).\n\n"
                    + "Notifica al cliente: " + orden.getNombreCliente() + " · Cel. " + orden.getTelefonoCliente());
            return;
        }
        DetalleOrden d = new DetalleOrden(orden.getIdOrden(), elegido.getIdProducto(), cantidad,
                actual.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad)));
        d.setNombreProducto(elegido.getNombre());
        nuevos.add(d);
        vista.getSpnCantidad().setValue(1);
        mostrarDetalle();
    }

    private void quitarRepuesto() {
        int fila = vista.getTblDetalle().getSelectedRow() - registrados.size();
        if (fila < 0) {
            Mensajes.aviso(vista, "Selecciona un repuesto nuevo para quitarlo.\nLos ya registrados no se pueden quitar.");
            return;
        }
        nuevos.remove(fila);
        mostrarDetalle();
    }

    // ------------------------------------------------------------------ Guardar / completar

    private Integer mecanicoElegido() {
        Usuario m = (Usuario) vista.getCboMecanicoAt().getSelectedItem();
        return m == null ? null : m.getIdUsuario();
    }

    private void guardarAvance() {
        if (orden == null) {
            return;
        }
        if (!nuevos.isEmpty()) {
            Mensajes.aviso(vista, "Los repuestos nuevos se guardan al completar el servicio,\n"
                    + "porque en ese momento se descuenta el stock.");
        }
        if (ordenDAO.guardarAvance(orden.getIdOrden(), vista.getTxaDiagnostico().getText().trim(),
                (String) vista.getCboEstado().getSelectedItem(), mecanicoElegido())) {
            Mensajes.info(vista, "Avance guardado.");
            listarActivas();
        }
    }

    private void completarServicio() {
        if (orden == null) {
            return;
        }
        String diagnostico = vista.getTxaDiagnostico().getText().trim();
        if (Validador.estaVacio(diagnostico)) {
            Mensajes.aviso(vista, "Escribe el diagnóstico antes de completar el servicio.");
            return;
        }
        if (mecanicoElegido() == null) {
            Mensajes.aviso(vista, "Selecciona el mecánico responsable.");
            return;
        }
        if (!Mensajes.confirmar(vista, "¿Completar el servicio de la moto " + orden.getPlaca() + "?\n"
                + "Se descontará el stock y la moto quedará lista para cobrarse en el punto de venta.")) {
            return;
        }
        if (ordenDAO.completarServicio(orden.getIdOrden(), diagnostico, mecanicoElegido(), nuevos)) {
            Mensajes.info(vista, "Servicio completado. La moto " + orden.getPlaca() + " está lista para cobrarse.");
            int idVehiculo = orden.getIdVehiculo();
            String placa = orden.getPlaca();
            listarActivas();
            productosActualizados();
            mostrarHistorial(idVehiculo, placa);
        } else {
            Mensajes.error(vista, "No se pudo completar el servicio: el stock de algún repuesto cambió.\n"
                    + "No se guardó ningún cambio. Revisa los repuestos e inténtalo de nuevo.");
        }
    }

    private void productosActualizados() {
        vista.getCboProducto().setModel(new DefaultComboBoxModel<>(
                productoDAO.buscar("").stream().filter(p -> !"Motocicleta".equals(p.getCategoria()))
                        .toArray(Producto[]::new)));
    }

    // ------------------------------------------------------------------ Historial de la moto

    private void buscarHistorial() {
        String placa = Formato.placa(vista.getTxtBuscarPlaca().getText());
        Vehiculo v = Validador.estaVacio(placa) ? null : vehiculoDAO.buscarPorPlaca(placa);
        if (v == null) {
            Mensajes.aviso(vista, "No existe una moto con la placa " + placa + ".");
            return;
        }
        mostrarHistorial(v.getIdVehiculo(), v.getPlaca());
    }

    private void mostrarHistorial(int idVehiculo, String placa) {
        vista.getTxtBuscarPlaca().setText(placa);
        vista.getLblHistorialMoto().setText("Historial de " + placa);
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("Fecha", "Diagnóstico", "Estado", "Total");
        for (AtencionResumen a : ordenDAO.historialPorVehiculo(idVehiculo)) {
            modelo.addRow(new Object[]{a.fecha().format(FECHA), a.diagnostico(), a.estado(), Formato.soles(a.totalRepuestos())});
        }
        vista.getTblHistorial().setModel(modelo);
        Estilos.columnaEstado(vista.getTblHistorial(), 2);
    }
}
