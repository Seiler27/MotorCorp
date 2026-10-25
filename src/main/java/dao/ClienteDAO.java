package dao;

import java.util.List;
import modelo.Cliente;
import modelo.Vehiculo;

/**
 * Operaciones de base de datos para los clientes del taller.
 */
public interface ClienteDAO {

    /** Busca clientes por DNI, nombres o apellidos. Si el texto está vacío, lista todos. */
    List<Cliente> buscar(String texto);

    /** Busca un cliente por su id. Devuelve null si no existe. */
    Cliente buscarPorId(int idCliente);

    /** Indica si ya existe otro cliente con ese DNI (se ignora el cliente con idExcluir). */
    boolean existeDni(String dni, int idExcluir);

    /**
     * Registra el cliente y, si se envía, su moto, en una sola transacción:
     * se guardan los dos o ninguno (ACID).
     *
     * @return el id generado del cliente, o 0 si no se pudo registrar
     */
    int registrar(Cliente cliente, Vehiculo vehiculo);

    /** Actualiza los datos del cliente. */
    boolean actualizar(Cliente cliente);

    /**
     * Elimina el cliente y sus motos en una transacción.
     * Falla si alguna moto ya tiene órdenes de servicio (se conserva el historial).
     */
    boolean eliminar(int idCliente);
}
