package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de ProductoDAO para PostgreSQL.
 */
public class ProductoDAOImpl implements ProductoDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ProductoDAOImpl.class);
    private static final String SQL_BASE =
            "SELECT id_producto, categoria, nombre, stock_actual, precio_unitario FROM productos ";

    @Override
    public List<Producto> buscar(String texto) {
        List<Producto> lista = new ArrayList<>();
        String sql = SQL_BASE + "WHERE nombre ILIKE ? OR categoria ILIKE ? ORDER BY categoria, nombre";
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al buscar productos", e);
        }
        return lista;
    }

    @Override
    public Producto buscarPorId(int idProducto) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_BASE + "WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            LOG.error("Error al buscar el producto {}", idProducto, e);
            return null;
        }
    }

    /** Convierte una fila de la consulta en un objeto Producto. */
    protected Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto(rs.getString("categoria"), rs.getString("nombre"),
                rs.getInt("stock_actual"), rs.getBigDecimal("precio_unitario"));
        p.setIdProducto(rs.getInt("id_producto"));
        return p;
    }
}
