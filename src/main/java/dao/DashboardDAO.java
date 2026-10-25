package dao;

import java.math.BigDecimal;
import java.util.List;
import modelo.OrdenServicio;
import modelo.VentaResumen;

/**
 * Consultas de resumen para el panel principal (dashboard).
 */
public interface DashboardDAO {

    /** Total cobrado hoy. */
    BigDecimal ventasDeHoy();

    /** Total cobrado en el mes actual. */
    BigDecimal ingresosDelMes();

    /** Cantidad de motos en espera o en reparación. */
    int motosEnTaller();

    /** Cantidad de productos con stock menor o igual al límite. */
    int productosConStockBajo(int limite);

    /** Primeras órdenes de la cola de espera (orden de llegada). */
    List<OrdenServicio> colaDeEspera(int cantidad);

    /** Últimos comprobantes emitidos. */
    List<VentaResumen> ultimasVentas(int cantidad);
}
