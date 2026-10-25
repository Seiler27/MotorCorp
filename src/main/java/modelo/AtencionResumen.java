package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una atención pasada de una moto, para mostrar su historial.
 */
public record AtencionResumen(int idOrden, LocalDateTime fecha, String diagnostico, String estado,
        String mecanico, BigDecimal totalRepuestos) {
}
