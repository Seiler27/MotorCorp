package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import modelo.DetalleOrden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.EstadoOrden;

/**
 * Implementación de ComprobanteDAO para PostgreSQL.
 */
public class ComprobanteDAOImpl implements ComprobanteDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ComprobanteDAOImpl.class);

    @Override
    public int generar(int idOrden, int idCajero, String tipo, BigDecimal total, List<DetalleOrden> productosAgregados) {
        String sqlStock = "UPDATE productos SET stock_actual = stock_actual - ? WHERE id_producto = ? AND stock_actual >= ?";
        String sqlDetalle = "INSERT INTO detalle_orden (id_orden, id_producto, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlComprobante = "INSERT INTO comprobantes (id_orden, id_cajero, tipo, total_pagado) VALUES (?, ?, ?, ?)";
        String sqlOrden = "UPDATE ordenes_servicio SET estado = ? WHERE id_orden = ? AND estado = ?";
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);                                   // inicio de la transacción
            try (PreparedStatement ps = cn.prepareStatement(sqlStock);
                 PreparedStatement pd = cn.prepareStatement(sqlDetalle);
                 PreparedStatement pc = cn.prepareStatement(sqlComprobante, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement po = cn.prepareStatement(sqlOrden)) {
                // 1. Productos vendidos en caja: descontar stock y registrar
                for (DetalleOrden d : productosAgregados) {
                    ps.setInt(1, d.getCantidad());
                    ps.setInt(2, d.getIdProducto());
                    ps.setInt(3, d.getCantidad());
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Stock insuficiente de " + d.getNombreProducto());
                    }
                    pd.setInt(1, idOrden);
                    pd.setInt(2, d.getIdProducto());
                    pd.setInt(3, d.getCantidad());
                    pd.setBigDecimal(4, d.getSubtotal());
                    pd.executeUpdate();
                }
                // 2. Pasar la orden a "Entregado" (solo si seguía lista para cobrar)
                po.setString(1, EstadoOrden.ENTREGADO);
                po.setInt(2, idOrden);
                po.setString(3, EstadoOrden.FINALIZADO);
                if (po.executeUpdate() == 0) {
                    throw new SQLException("La orden " + idOrden + " ya fue cobrada");
                }
                // 3. Emitir el comprobante
                pc.setInt(1, idOrden);
                pc.setInt(2, idCajero);
                pc.setString(3, tipo);
                pc.setBigDecimal(4, total);
                pc.executeUpdate();
                int numero;
                try (ResultSet rs = pc.getGeneratedKeys()) {
                    rs.next();
                    numero = rs.getInt(1);
                }
                cn.commit();                                           // todo se confirma junto
                LOG.info("[VENTA] {} N° {} emitida por el usuario {}: orden {}, total S/ {}",
                        tipo, numero, idCajero, idOrden, total);
                return numero;
            } catch (SQLException e) {
                cn.rollback();                                         // nada se guarda
                LOG.warn("[VENTA] No se cobró la orden {}: {}", idOrden, e.getMessage());
                return 0;
            }
        } catch (SQLException e) {
            LOG.error("Error al generar el comprobante de la orden {}", idOrden, e);
            return 0;
        }
    }
}
