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
}
