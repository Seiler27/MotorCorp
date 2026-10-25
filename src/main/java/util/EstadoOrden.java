package util;

/**
 * Estados de una orden de servicio (los mismos que permite la base de datos).
 */
public final class EstadoOrden {

    public static final String EN_ESPERA = "En espera";
    public static final String EN_REPARACION = "En reparación";
    /** Trabajo terminado: la moto está lista para cobrarse en el punto de venta. */
    public static final String FINALIZADO = "Finalizado";
    /** Cobrada y entregada al cliente. */
    public static final String ENTREGADO = "Entregado";

    private EstadoOrden() {
    }
}
