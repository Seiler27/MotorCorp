package controlador;

import dao.ComprobanteDAO;
import dao.OrdenServicioDAO;
import dao.ProductoDAO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import modelo.DetalleOrden;
import modelo.OrdenServicio;
import modelo.Producto;
import modelo.Usuario;
import util.EstadoOrden;
import util.Formato;
import util.ValidacionProducto;
import util.Validador;
import vista.Mensajes;
import vista.PnlPos;

/**
 * Controlador del Punto de venta (RF09): cobra las motos terminadas, permite vender
 * productos adicionales y emite la boleta o factura.
 */
public class ControladorPos {

    private final PnlPos vista;
    private final OrdenServicioDAO ordenDAO;
    private final ProductoDAO productoDAO;
    private final ComprobanteDAO comprobanteDAO;
    private final Usuario cajero;

    private List<OrdenServicio> listas = new ArrayList<>();
    private OrdenServicio orden;
    /** Repuestos que el taller ya registró en la orden. */
    private List<DetalleOrden> delTaller = new ArrayList<>();
    /** Productos agregados en caja (se descuentan del stock al cobrar). */
    private final List<DetalleOrden> agregados = new ArrayList<>();

    public ControladorPos(PnlPos vista, OrdenServicioDAO ordenDAO, ProductoDAO productoDAO,
            ComprobanteDAO comprobanteDAO, Usuario cajero) {
        this.vista = vista;
        this.ordenDAO = ordenDAO;
        this.productoDAO = productoDAO;
        this.comprobanteDAO = comprobanteDAO;
        this.cajero = cajero;

        vista.getCboProductoPos().setRenderer(new DefaultListCellRenderer() {
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
        vista.getTblOrdenesPos().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarOrden();
            }
        });
        vista.getBtnAgregarProducto().addActionListener(e -> agregarProducto());
        vista.getBtnQuitarItem().addActionListener(e -> quitarItem());
        vista.getBtnVaciar().addActionListener(e -> {
            agregados.clear();
            vista.getTxtManoObra().setText("");
            mostrarCarrito();
        });
        vista.getBtnGenerar().addActionListener(e -> generarComprobante());
        vista.getTxtManoObra().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { mostrarCarrito(); }
            @Override public void removeUpdate(DocumentEvent e) { mostrarCarrito(); }
            @Override public void changedUpdate(DocumentEvent e) { mostrarCarrito(); }
        });
    }

    /** Se llama cada vez que se abre el módulo. */
    public void cargar() {
        vista.getCboProductoPos().setModel(new DefaultComboBoxModel<>(
                productoDAO.buscar("").stream().filter(p -> p.getStockActual() > 0).toArray(Producto[]::new)));
        listarOrdenes();
    }

    private void listarOrdenes() {
        listas = ordenDAO.listarPorEstados(EstadoOrden.FINALIZADO);
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("N°", "Placa", "Cliente", "Trabajo realizado");
        for (OrdenServicio o : listas) {
            modelo.addRow(new Object[]{o.getIdOrden(), o.getPlaca(), o.getNombreCliente(), o.getDiagnostico()});
        }
        vista.getTblOrdenesPos().setModel(modelo);
        vista.getTblOrdenesPos().getColumnModel().getColumn(0).setMaxWidth(40);
        orden = null;
        delTaller = new ArrayList<>();
        agregados.clear();
        vista.getTxtManoObra().setText("");
        mostrarCarrito();
    }

    private void seleccionarOrden() {
        int fila = vista.getTblOrdenesPos().getSelectedRow();
        if (fila < 0 || fila >= listas.size()) {
            return;
        }
        orden = listas.get(fila);
        delTaller = ordenDAO.listarDetalle(orden.getIdOrden());
        agregados.clear();
        vista.getTxtManoObra().setText("");
        mostrarCarrito();
    }

    private void agregarProducto() {
        Producto elegido = (Producto) vista.getCboProductoPos().getSelectedItem();
        if (orden == null || elegido == null) {
            return;
        }
        int cantidad = (Integer) vista.getSpnCantidadPos().getValue();
        Producto actual = productoDAO.buscarPorId(elegido.getIdProducto());
        int yaAgregado = agregados.stream().filter(d -> d.getIdProducto() == elegido.getIdProducto())
                .mapToInt(DetalleOrden::getCantidad).sum();
        if (actual == null || cantidad > actual.getStockActual() - yaAgregado) {
            Mensajes.aviso(vista, "No hay stock suficiente de " + elegido.getNombre() + ".");
            return;
        }
        DetalleOrden d = new DetalleOrden(orden.getIdOrden(), elegido.getIdProducto(), cantidad,
                actual.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad)));
        d.setNombreProducto(elegido.getNombre());
        agregados.add(d);
        vista.getSpnCantidadPos().setValue(1);
        mostrarCarrito();
    }

    private void quitarItem() {
        int fila = vista.getTblCarrito().getSelectedRow() - delTaller.size();
        if (fila < 0 || fila >= agregados.size()) {
            Mensajes.aviso(vista, "Solo puedes quitar los productos agregados en caja.");
            return;
        }
        agregados.remove(fila);
        mostrarCarrito();
    }

    /** Mano de obra escrita por el cajero (0 si está vacía o no es válida). */
    private BigDecimal manoDeObra() {
        String texto = vista.getTxtManoObra().getText();
        BigDecimal monto = Validador.estaVacio(texto) ? null : ValidacionProducto.aPrecio(texto);
        return monto == null ? BigDecimal.ZERO : monto;
    }

    private void mostrarCarrito() {
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("Descripción", "Cant.", "P. unit.", "Subtotal", "Origen");
        BigDecimal productos = BigDecimal.ZERO;
        List<DetalleOrden> todos = new ArrayList<>(delTaller);
        todos.addAll(agregados);
        for (int i = 0; i < todos.size(); i++) {
            DetalleOrden d = todos.get(i);
            BigDecimal unitario = d.getSubtotal().divide(BigDecimal.valueOf(d.getCantidad()), 2, java.math.RoundingMode.HALF_UP);
            modelo.addRow(new Object[]{d.getNombreProducto(), d.getCantidad(), Formato.soles(unitario),
                Formato.soles(d.getSubtotal()), i < delTaller.size() ? "Taller" : "Caja"});
            productos = productos.add(d.getSubtotal());
        }
        BigDecimal mano = manoDeObra();
        if (mano.signum() > 0) {
            modelo.addRow(new Object[]{"Mano de obra", 1, Formato.soles(mano), Formato.soles(mano), "Servicio"});
        }
        vista.getTblCarrito().setModel(modelo);
        vista.getTblCarrito().getColumnModel().getColumn(1).setMaxWidth(50);
        vista.getLblSubtotalRep().setText("Repuestos y productos: " + Formato.soles(productos));
        vista.getLblManoObraRes().setText("Mano de obra: " + Formato.soles(mano));
        vista.getLblTotalPos().setText(Formato.soles(productos.add(mano)));
        vista.habilitarCobro(orden != null);
        vista.getLblClientePos().setText(orden == null ? "Selecciona una moto lista para cobrar."
                : "<html><b>" + orden.getNombreCliente() + "</b><br>" + orden.getPlaca() + " · " + orden.getMoto() + "</html>");
    }

    private BigDecimal total() {
        BigDecimal t = manoDeObra();
        for (DetalleOrden d : delTaller) {
            t = t.add(d.getSubtotal());
        }
        for (DetalleOrden d : agregados) {
            t = t.add(d.getSubtotal());
        }
        return t;
    }

    private void generarComprobante() {
        if (orden == null) {
            return;
        }
        if (!Validador.estaVacio(vista.getTxtManoObra().getText()) && ValidacionProducto.aPrecio(vista.getTxtManoObra().getText()) == null) {
            Mensajes.aviso(vista, "La mano de obra debe ser un monto válido (ej: 40.00).");
            return;
        }
        BigDecimal total = total();
        if (total.signum() <= 0) {
            Mensajes.aviso(vista, "El total a cobrar debe ser mayor a S/ 0.00.\nIngresa la mano de obra o agrega productos.");
            return;
        }
        String tipo = (String) vista.getCboTipo().getSelectedItem();
        if (!Mensajes.confirmar(vista, "¿Generar " + tipo.toLowerCase() + " por " + Formato.soles(total)
                + " a nombre de " + orden.getNombreCliente() + "?")) {
            return;
        }
        int numero = comprobanteDAO.generar(orden.getIdOrden(), cajero.getIdUsuario(), tipo, total, agregados);
        if (numero > 0) {
            Mensajes.info(vista, tipo + " N° " + String.format("%06d", numero) + " generada.\n\n"
                    + "Cliente: " + orden.getNombreCliente() + "\nMoto: " + orden.getPlaca()
                    + "\nTotal pagado: " + Formato.soles(total) + "\n\nLa moto quedó como «Entregado».");
            cargar();
        } else {
            Mensajes.error(vista, "No se pudo generar el comprobante (stock insuficiente o la orden ya fue cobrada).\n"
                    + "No se guardó ningún cambio.");
        }
    }
}
