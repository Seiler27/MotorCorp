package controlador;

import dao.ReporteDAO;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import modelo.Reporte;
import modelo.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExportadorExcel;
import util.Formato;
import util.TipoReporte;
import vista.Mensajes;
import vista.PnlReportes;

/**
 * Controlador del módulo de Reportes (RF10): vista previa por rango de fechas
 * y exportación a Excel con Apache POI (RNF07).
 */
public class ControladorReportes {

    private static final Logger LOG = LoggerFactory.getLogger(ControladorReportes.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PnlReportes vista;
    private final ReporteDAO reporteDAO;
    private final Usuario usuario;
    private Reporte reporte;

    public ControladorReportes(PnlReportes vista, ReporteDAO reporteDAO, Usuario usuario) {
        this.vista = vista;
        this.reporteDAO = reporteDAO;
        this.usuario = usuario;
        vista.getBtnGenerarRep().addActionListener(e -> generar());
        vista.getBtnExportar().addActionListener(e -> exportar());
        vista.getCboTipoReporte().addActionListener(e -> {
            boolean usaFechas = tipo().usaFechas();
            vista.getSpnDesde().setEnabled(usaFechas);
            vista.getSpnHasta().setEnabled(usaFechas);
            generar();
        });
    }

    /** Se llama cada vez que se abre el módulo: por defecto, el mes actual. */
    public void cargar() {
        LocalDate hoy = LocalDate.now();
        PnlReportes.ponerFecha(vista.getSpnDesde(), hoy.withDayOfMonth(1));
        PnlReportes.ponerFecha(vista.getSpnHasta(), hoy);
        generar();
    }

    private TipoReporte tipo() {
        return (TipoReporte) vista.getCboTipoReporte().getSelectedItem();
    }

    private String periodo() {
        if (!tipo().usaFechas()) {
            return "Stock al " + LocalDate.now().format(FECHA);
        }
        return "Del " + PnlReportes.fecha(vista.getSpnDesde()).format(FECHA)
                + " al " + PnlReportes.fecha(vista.getSpnHasta()).format(FECHA);
    }

    /** Consulta el reporte y lo muestra en la vista previa. */
    private void generar() {
        LocalDate desde = PnlReportes.fecha(vista.getSpnDesde());
        LocalDate hasta = PnlReportes.fecha(vista.getSpnHasta());
        if (tipo().usaFechas() && desde.isAfter(hasta)) {
            Mensajes.aviso(vista, "La fecha «Desde» no puede ser posterior a «Hasta».");
            return;
        }
        reporte = reporteDAO.generar(tipo(), desde, hasta);
        DefaultTableModel modelo = ControladorInicio.modeloSoloLectura(reporte.columnas().toArray(new String[0]));
        for (Object[] fila : reporte.filas()) {
            Object[] texto = new Object[fila.length];
            for (int i = 0; i < fila.length; i++) {
                texto[i] = fila[i] instanceof BigDecimal monto ? Formato.soles(monto)
                        : fila[i] instanceof LocalDateTime fecha ? fecha.format(FECHA_HORA) : fila[i];
            }
            modelo.addRow(texto);
        }
        vista.getTblReporte().setModel(modelo);
        vista.getLblVistaPrevia().setText(reporte.titulo() + "  ·  " + periodo());
        vista.getLblResumenRep().setText(reporte.resumen());
        vista.getBtnExportar().setEnabled(!reporte.filas().isEmpty());
    }

    /** Guarda el reporte como archivo .xlsx en la ubicación que elija el usuario. */
    private void exportar() {
        if (reporte == null || reporte.filas().isEmpty()) {
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte en Excel");
        selector.setFileFilter(new FileNameExtensionFilter("Libro de Excel (*.xlsx)", "xlsx"));
        selector.setSelectedFile(new File("MotorCorp_" + tipo().name().toLowerCase() + "_" + LocalDate.now() + ".xlsx"));
        if (selector.showSaveDialog(vista) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".xlsx");
        }
        try {
            ExportadorExcel.exportar(reporte, periodo(), archivo);
            LOG.info("[REPORTE] {} exportó {} ({}) a {}", usuario.getUsername(), reporte.titulo(), periodo(), archivo.getName());
            if (Mensajes.confirmar(vista, "Reporte guardado en:\n" + archivo.getAbsolutePath() + "\n\n¿Deseas abrirlo ahora?")
                    && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
            }
        } catch (IOException e) {
            LOG.error("Error al exportar el reporte a Excel", e);
            Mensajes.error(vista, "No se pudo guardar el archivo. Verifica que no esté abierto en Excel.");
        }
    }
}
