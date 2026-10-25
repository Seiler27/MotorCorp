package controlador;

import dao.DashboardDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import modelo.OrdenServicio;
import modelo.Usuario;
import modelo.VentaResumen;
import org.apache.commons.lang3.StringUtils;
import util.Formato;
import vista.PnlInicio;

/**
 * Controlador del panel principal (dashboard): llena las tarjetas y las tablas
 * con los datos que entrega DashboardDAO.
 */
public class ControladorInicio {

    /** Un producto con este stock o menos se considera stock bajo. */
    public static final int LIMITE_STOCK_BAJO = 5;

    private static final Locale PERU = Locale.of("es", "PE");
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", PERU);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final PnlInicio vista;
    private final DashboardDAO dao;
    private final Usuario usuario;

    public ControladorInicio(PnlInicio vista, DashboardDAO dao, Usuario usuario) {
        this.vista = vista;
        this.dao = dao;
        this.usuario = usuario;
    }

    /** Consulta la base de datos y actualiza el panel. */
    public void cargar() {
        vista.getLblBienvenida().setText("Bienvenido, " + usuario.getNombres());
        vista.getLblFecha().setText(StringUtils.capitalize(LocalDate.now().format(FECHA_LARGA)));

        vista.getLblVentasHoyValor().setText(Formato.soles(dao.ventasDeHoy()));
        vista.getLblIngresosMesValor().setText(Formato.soles(dao.ingresosDelMes()));
        vista.getLblEnColaValor().setText(String.valueOf(dao.motosEnTaller()));
        vista.getLblStockBajoValor().setText(String.valueOf(dao.productosConStockBajo(LIMITE_STOCK_BAJO)));

        DefaultTableModel cola = modeloSoloLectura("N°", "Placa", "Cliente", "Estado", "Mecánico", "Ingreso");
        for (OrdenServicio o : dao.colaDeEspera(10)) {
            cola.addRow(new Object[]{o.getIdOrden(), o.getPlaca(), o.getNombreCliente(), o.getEstado(),
                o.getNombreMecanico() == null ? "Sin asignar" : o.getNombreMecanico(),
                o.getFechaIngreso().format(FECHA_HORA)});
        }
        vista.getTblCola().setModel(cola);
        vista.getTblCola().getColumnModel().getColumn(0).setMaxWidth(45);

        DefaultTableModel ventas = modeloSoloLectura("Comprobante", "Cliente", "Total");
        for (VentaResumen v : dao.ultimasVentas(10)) {
            ventas.addRow(new Object[]{v.comprobante(), v.cliente(), Formato.soles(v.total())});
        }
        vista.getTblVentas().setModel(ventas);
    }

    /** Crea un modelo de tabla que no permite editar las celdas. */
    static DefaultTableModel modeloSoloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }
}
