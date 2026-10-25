package dao;

import java.util.List;
import modelo.Vehiculo;

/**
 * Operaciones de base de datos para las motos de los clientes.
 */
public interface VehiculoDAO {

    /** Lista las motos de un cliente. */
    List<Vehiculo> listarPorCliente(int idCliente);

    /** Busca una moto por su placa. Devuelve null si no existe. */
    Vehiculo buscarPorPlaca(String placa);

    /** Indica si ya existe otra moto con esa placa (se ignora la moto con idExcluir). */
    boolean existePlaca(String placa, int idExcluir);

    /** Registra una moto para un cliente ya existente. */
    boolean registrar(Vehiculo vehiculo);

    /** Actualiza los datos de una moto. */
    boolean actualizar(Vehiculo vehiculo);

    /** Elimina una moto (falla si ya tiene órdenes de servicio). */
    boolean eliminar(int idVehiculo);
}
