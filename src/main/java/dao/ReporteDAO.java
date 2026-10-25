package dao;

import java.time.LocalDate;
import modelo.Reporte;
import util.TipoReporte;

/**
 * Consultas de los reportes de ventas, inventario y atenciones (RF10).
 */
public interface ReporteDAO {

    /** Genera el reporte del tipo indicado entre las fechas desde y hasta (inclusive). */
    Reporte generar(TipoReporte tipo, LocalDate desde, LocalDate hasta);
}
