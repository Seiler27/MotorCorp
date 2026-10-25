package dao;

import java.util.List;
import modelo.AtencionResumen;
import modelo.DetalleOrden;
import modelo.OrdenServicio;

/**
 * Operaciones de base de datos para las órdenes de servicio (cola de espera y taller).
 */
public interface OrdenServicioDAO {

    /** Registra el ingreso de una moto a la cola con la falla reportada (RF02). */
    boolean registrarIngreso(int idVehiculo, String fallaReportada);

    /** Indica si la moto ya tiene una orden sin terminar (en espera o en reparación). */
    boolean tieneOrdenActiva(int idVehiculo);

    /** Órdenes con los estados indicados, por orden de llegada (FIFO). */
    List<OrdenServicio> listarPorEstados(String... estados);

    /** Asigna el mecánico responsable de una orden. */
    boolean asignarMecanico(int idOrden, int idMecanico);

    /** Cambia el estado de una orden (RF03). */
    boolean cambiarEstado(int idOrden, String estado);

    /** Guarda el diagnóstico, el estado y el mecánico sin cerrar la orden (RF04). */
    boolean guardarAvance(int idOrden, String diagnostico, String estado, Integer idMecanico);

    /** Repuestos y servicios ya registrados en una orden. */
    List<DetalleOrden> listarDetalle(int idOrden);

    /**
     * Cierra la orden en una sola transacción (RF07, RF08): registra los repuestos nuevos,
     * descuenta el stock (solo si alcanza), guarda el diagnóstico y pasa la orden a "Finalizado".
     * Si falta stock de algún repuesto, no se guarda nada.
     */
    boolean completarServicio(int idOrden, String diagnostico, Integer idMecanico, List<DetalleOrden> repuestosNuevos);

    /** Atenciones de una moto, de la más reciente a la más antigua (RF08). */
    List<AtencionResumen> historialPorVehiculo(int idVehiculo);
}
