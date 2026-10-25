package dao;

import java.math.BigDecimal;
import java.util.List;
import modelo.DetalleOrden;

/**
 * Operaciones de base de datos del punto de venta (RF09).
 */
public interface ComprobanteDAO {

    /**
     * Cobra una orden en una sola transacción: registra los productos agregados en caja
     * (descontando su stock), emite el comprobante y pasa la orden a "Entregado".
     *
     * @return el número del comprobante generado, o 0 si no se pudo cobrar
     */
    int generar(int idOrden, int idCajero, String tipo, BigDecimal total, List<DetalleOrden> productosAgregados);
}
