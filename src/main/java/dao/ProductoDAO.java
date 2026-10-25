package dao;

import java.util.List;
import modelo.Producto;

/**
 * Operaciones de base de datos para el inventario (repuestos, accesorios y motos).
 */
public interface ProductoDAO {

    /** Busca productos por nombre o categoría. Si el texto está vacío, lista todos. */
    List<Producto> buscar(String texto);

    /** Busca un producto por su id (para conocer su stock actual). */
    Producto buscarPorId(int idProducto);

    /** Indica si ya existe otro producto con el mismo nombre (se ignora idExcluir). */
    boolean existeNombre(String nombre, int idExcluir);

    /** Registra un producto nuevo. */
    boolean registrar(Producto producto);

    /** Actualiza categoría, nombre, stock y precio. */
    boolean actualizar(Producto producto);

    /** Suma unidades al stock (ingreso de mercadería). */
    boolean reponerStock(int idProducto, int cantidad);

    /** Elimina un producto (falla si ya se usó en alguna orden, para conservar el historial). */
    boolean eliminar(int idProducto);
}
