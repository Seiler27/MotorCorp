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

    @Override
    public boolean existeNombre(String nombre, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM productos WHERE LOWER(nombre) = LOWER(?) AND id_producto <> ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.error("Error al verificar el producto {}", nombre, e);
            return false;
        }
    }

    @Override
    public boolean registrar(Producto p) {
        return ejecutar("INSERT INTO productos (categoria, nombre, stock_actual, precio_unitario) VALUES (?, ?, ?, ?)",
                "Producto registrado: " + p.getNombre() + " (stock " + p.getStockActual() + ")",
                p.getCategoria(), p.getNombre(), p.getStockActual(), p.getPrecioUnitario());
    }

    @Override
    public boolean actualizar(Producto p) {
        return ejecutar("UPDATE productos SET categoria = ?, nombre = ?, stock_actual = ?, precio_unitario = ? WHERE id_producto = ?",
                "Producto actualizado: " + p.getNombre() + " (stock " + p.getStockActual() + ", precio " + p.getPrecioUnitario() + ")",
                p.getCategoria(), p.getNombre(), p.getStockActual(), p.getPrecioUnitario(), p.getIdProducto());
    }

    @Override
    public boolean reponerStock(int idProducto, int cantidad) {
        return ejecutar("UPDATE productos SET stock_actual = stock_actual + ? WHERE id_producto = ?",
                "Reposición de stock: producto " + idProducto + " +" + cantidad, cantidad, idProducto);
    }

    @Override
    public boolean eliminar(int idProducto) {
        return ejecutar("DELETE FROM productos WHERE id_producto = ?", "Producto eliminado: " + idProducto, idProducto);
    }

    /**
     * Ejecuta un INSERT, UPDATE o DELETE con sus parámetros y deja constancia en la bitácora
     * (auditoría del inventario, RNF01).
     */
    private boolean ejecutar(String sql, String mensajeBitacora, Object... parametros) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("[INVENTARIO] {}", mensajeBitacora);
            }
            return ok;
        } catch (SQLException e) {
            LOG.warn("[INVENTARIO] Operación no realizada: {} ({})", mensajeBitacora, e.getMessage());
            return false;
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
