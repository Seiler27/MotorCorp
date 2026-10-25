package controlador;

import dao.ProductoDAO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import modelo.Producto;
import modelo.Usuario;
import util.Formato;
import util.ValidacionProducto;
import vista.Estilos;
import vista.Mensajes;
import vista.PnlInventario;

/**
 * Controlador del módulo de Inventario: registrar, editar, eliminar y reponer productos,
 * con alerta visual de stock bajo. El mecánico solo puede consultar.
 */
public class ControladorInventario {

    private final PnlInventario vista;
    private final ProductoDAO productoDAO;
    private final boolean puedeEditar;

    private List<Producto> productos = new ArrayList<>();
    private Producto seleccionado;

    public ControladorInventario(PnlInventario vista, ProductoDAO productoDAO, Usuario usuario) {
        this.vista = vista;
        this.productoDAO = productoDAO;
        this.puedeEditar = !"Mecanico".equals(usuario.getNombreRol());

        vista.permitirEdicion(puedeEditar);
        vista.getBtnGuardarProd().addActionListener(e -> guardar());
        vista.getBtnNuevoProd().addActionListener(e -> nuevo());
        vista.getBtnEliminarProd().addActionListener(e -> eliminar());
        vista.getBtnReponer().addActionListener(e -> reponer());
        vista.getCboFiltro().addActionListener(e -> listar());
        vista.getTblProductos().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
        vista.getTxtBuscarProd().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { listar(); }
            @Override public void removeUpdate(DocumentEvent e) { listar(); }
            @Override public void changedUpdate(DocumentEvent e) { listar(); }
        });
    }

    /** Se llama cada vez que se abre el módulo. */
    public void cargar() {
        nuevo();
    }

    private void listar() {
        boolean soloStockBajo = vista.getCboFiltro().getSelectedIndex() == 1;
        productos = productoDAO.buscar(vista.getTxtBuscarProd().getText()).stream()
                .filter(p -> !soloStockBajo || p.getStockActual() <= ControladorInicio.LIMITE_STOCK_BAJO)
                .toList();
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura("Código", "Categoría", "Nombre", "Stock", "Precio");
        int stockBajo = 0;
        for (Producto p : productos) {
            modelo.addRow(new Object[]{String.format("P-%03d", p.getIdProducto()), p.getCategoria(), p.getNombre(),
                p.getStockActual(), Formato.soles(p.getPrecioUnitario())});
            if (p.getStockActual() <= ControladorInicio.LIMITE_STOCK_BAJO) {
                stockBajo++;
            }
        }
        vista.getTblProductos().setModel(modelo);
        int[] anchos = {60, 90, 230, 70, 90};
        for (int i = 0; i < anchos.length; i++) {
            vista.getTblProductos().getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        Estilos.columnaStock(vista.getTblProductos(), 3, ControladorInicio.LIMITE_STOCK_BAJO);
        vista.getLblListadoProd().setText("Productos (" + productos.size() + ")  ·  " + stockBajo + " con stock bajo");
        seleccionado = null;
        actualizarBotones();
    }

    private void seleccionar() {
        int fila = vista.getTblProductos().getSelectedRow();
        if (fila < 0 || fila >= productos.size()) {
            return;
        }
        seleccionado = productos.get(fila);
        vista.getCboCategoria().setSelectedItem(seleccionado.getCategoria());
        vista.getTxtNombre().setText(seleccionado.getNombre());
        vista.getTxtStock().setText(String.valueOf(seleccionado.getStockActual()));
        vista.getTxtPrecio().setText(seleccionado.getPrecioUnitario().toPlainString());
        actualizarBotones();
    }

    private void actualizarBotones() {
        vista.getBtnEliminarProd().setEnabled(seleccionado != null);
        vista.getBtnReponer().setEnabled(seleccionado != null);
    }

    private void nuevo() {
        vista.getTxtBuscarProd().setText("");
        vista.limpiarFormulario();
        listar();
    }

    private void guardar() {
        String categoria = (String) vista.getCboCategoria().getSelectedItem();
        String nombre = vista.getTxtNombre().getText().trim();
        String stock = vista.getTxtStock().getText();
        String precio = vista.getTxtPrecio().getText();

        List<String> errores = ValidacionProducto.validar(categoria, nombre, stock, precio);
        int idActual = seleccionado == null ? 0 : seleccionado.getIdProducto();
        if (errores.isEmpty() && productoDAO.existeNombre(nombre, idActual)) {
            errores.add("Ya existe un producto llamado \"" + nombre + "\".");
        }
        if (!errores.isEmpty()) {
            Mensajes.aviso(vista, "Revisa los datos:\n• " + String.join("\n• ", errores));
            return;
        }
        BigDecimal valorPrecio = ValidacionProducto.aPrecio(precio);
        Producto producto = new Producto(categoria, nombre, ValidacionProducto.aEntero(stock), valorPrecio);
        boolean ok;
        if (seleccionado == null) {
            ok = productoDAO.registrar(producto);
        } else {
            producto.setIdProducto(idActual);
            ok = productoDAO.actualizar(producto);
        }
        if (ok) {
            Mensajes.info(vista, seleccionado == null ? "Producto registrado." : "Producto actualizado.");
            nuevo();
        } else {
            Mensajes.error(vista, "No se pudo guardar el producto.");
        }
    }

    private void reponer() {
        if (seleccionado == null) {
            return;
        }
        int cantidad = (Integer) vista.getSpnReponer().getValue();
        if (productoDAO.reponerStock(seleccionado.getIdProducto(), cantidad)) {
            Mensajes.info(vista, "Se agregaron " + cantidad + " unidad(es) de " + seleccionado.getNombre() + ".");
            nuevo();
        }
    }

    private void eliminar() {
        if (seleccionado == null || !Mensajes.confirmar(vista, "¿Eliminar " + seleccionado.getNombre() + " del inventario?")) {
            return;
        }
        if (productoDAO.eliminar(seleccionado.getIdProducto())) {
            nuevo();
        } else {
            Mensajes.error(vista, "No se puede eliminar: el producto ya se usó en atenciones o ventas.\n"
                    + "Si ya no se vende, deja su stock en 0.");
        }
    }
}
