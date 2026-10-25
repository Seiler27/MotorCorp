package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumen de una venta para mostrar en el panel principal.
 * 
 * 
 */
public record VentaResumen(String comprobante, String cliente, LocalDateTime fecha, BigDecimal total) {
}
