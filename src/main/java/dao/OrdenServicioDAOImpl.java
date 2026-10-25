package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.AtencionResumen;
import modelo.DetalleOrden;
import modelo.OrdenServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.EstadoOrden;

/**
 * Implementación de OrdenServicioDAO para PostgreSQL.
 */
public class OrdenServicioDAOImpl implements OrdenServicioDAO {

    private static final Logger LOG = LoggerFactory.getLogger(OrdenServicioDAOImpl.class);

    private static final String SQL_BASE =
            "SELECT o.id_orden, o.id_vehiculo, o.id_mecanico, o.diagnostico, o.estado, o.fecha_ingreso, "
            + "v.placa, v.marca || ' ' || COALESCE(v.modelo, '') AS moto, "
            + "c.nombres || ' ' || c.apellidos AS cliente, c.telefono, u.nombres AS mecanico "
            + "FROM ordenes_servicio o "
            + "JOIN vehiculos v ON o.id_vehiculo = v.id_vehiculo "
            + "JOIN clientes c ON v.id_cliente = c.id_cliente "
            + "LEFT JOIN usuarios u ON o.id_mecanico = u.id_usuario ";

    @Override
    public boolean registrarIngreso(int idVehiculo, String fallaReportada) {
        String sql = "INSERT INTO ordenes_servicio (id_vehiculo, diagnostico, estado) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            ps.setString(2, fallaReportada);
            ps.setString(3, EstadoOrden.EN_ESPERA);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("Ingreso a la cola: vehículo {}", idVehiculo);
            }
            return ok;
        } catch (SQLException e) {
            LOG.error("Error al registrar el ingreso del vehículo {}", idVehiculo, e);
            return false;
        }
    }

    @Override
    public boolean tieneOrdenActiva(int idVehiculo) {
        String sql = "SELECT COUNT(*) FROM ordenes_servicio WHERE id_vehiculo = ? AND estado IN (?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            ps.setString(2, EstadoOrden.EN_ESPERA);
            ps.setString(3, EstadoOrden.EN_REPARACION);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.error("Error al verificar órdenes activas del vehículo {}", idVehiculo, e);
            return false;
        }
    }

    @Override
    public List<OrdenServicio> listarPorEstados(String... estados) {
        List<OrdenServicio> lista = new ArrayList<>();
        String marcas = String.join(", ", Collections.nCopies(estados.length, "?"));
        String sql = SQL_BASE + "WHERE o.estado IN (" + marcas + ") ORDER BY o.fecha_ingreso";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < estados.length; i++) {
                ps.setString(i + 1, estados[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al listar órdenes", e);
        }
        return lista;
    }

    @Override
    public boolean asignarMecanico(int idOrden, int idMecanico) {
        return actualizar("UPDATE ordenes_servicio SET id_mecanico = ? WHERE id_orden = ?", idMecanico, idOrden,
                "Mecánico asignado a la orden " + idOrden);
    }

    @Override
    public boolean cambiarEstado(int idOrden, String estado) {
        return actualizar("UPDATE ordenes_servicio SET estado = ? WHERE id_orden = ?", estado, idOrden,
                "Orden " + idOrden + " pasó a " + estado);
    }

    @Override
    public boolean guardarAvance(int idOrden, String diagnostico, String estado, Integer idMecanico) {
        String sql = "UPDATE ordenes_servicio SET diagnostico = ?, estado = ?, id_mecanico = ? WHERE id_orden = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, diagnostico);
            ps.setString(2, estado);
            if (idMecanico == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, idMecanico);
            }
            ps.setInt(4, idOrden);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("Avance guardado en la orden {}", idOrden);
            }
            return ok;
        } catch (SQLException e) {
            LOG.error("Error al guardar el avance de la orden {}", idOrden, e);
            return false;
        }
    }

    @Override
    public List<DetalleOrden> listarDetalle(int idOrden) {
        List<DetalleOrden> lista = new ArrayList<>();
        String sql = "SELECT d.id_detalle, d.id_orden, d.id_producto, d.cantidad, d.subtotal, p.nombre "
                + "FROM detalle_orden d JOIN productos p ON d.id_producto = p.id_producto "
                + "WHERE d.id_orden = ? ORDER BY d.id_detalle";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleOrden d = new DetalleOrden(rs.getInt("id_orden"), rs.getInt("id_producto"),
                            rs.getInt("cantidad"), rs.getBigDecimal("subtotal"));
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setNombreProducto(rs.getString("nombre"));
                    lista.add(d);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al listar el detalle de la orden {}", idOrden, e);
        }
        return lista;
    }

    @Override
    public boolean completarServicio(int idOrden, String diagnostico, Integer idMecanico, List<DetalleOrden> repuestosNuevos) {
        String sqlStock = "UPDATE productos SET stock_actual = stock_actual - ? WHERE id_producto = ? AND stock_actual >= ?";
        String sqlDetalle = "INSERT INTO detalle_orden (id_orden, id_producto, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlOrden = "UPDATE ordenes_servicio SET diagnostico = ?, id_mecanico = ?, estado = ? WHERE id_orden = ?";
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);                       // inicio de la transacción
            try (PreparedStatement ps = cn.prepareStatement(sqlStock);
                 PreparedStatement pd = cn.prepareStatement(sqlDetalle);
                 PreparedStatement po = cn.prepareStatement(sqlOrden)) {
                for (DetalleOrden d : repuestosNuevos) {
                    // 1. Descontar stock solo si alcanza
                    ps.setInt(1, d.getCantidad());
                    ps.setInt(2, d.getIdProducto());
                    ps.setInt(3, d.getCantidad());
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Stock insuficiente de " + d.getNombreProducto());
                    }
                    // 2. Registrar el repuesto usado
                    pd.setInt(1, idOrden);
                    pd.setInt(2, d.getIdProducto());
                    pd.setInt(3, d.getCantidad());
                    pd.setBigDecimal(4, d.getSubtotal());
                    pd.executeUpdate();
                }
                // 3. Cerrar la orden
                po.setString(1, diagnostico);
                if (idMecanico == null) {
                    po.setNull(2, Types.INTEGER);
                } else {
                    po.setInt(2, idMecanico);
                }
                po.setString(3, EstadoOrden.FINALIZADO);
                po.setInt(4, idOrden);
                po.executeUpdate();
                cn.commit();                               // todo se confirma junto
                LOG.info("Servicio completado: orden {} con {} repuesto(s) nuevo(s)", idOrden, repuestosNuevos.size());
                return true;
            } catch (SQLException e) {
                cn.rollback();                             // si algo falla, no se descuenta nada
                LOG.warn("No se completó la orden {}: {}", idOrden, e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Error al completar la orden {}", idOrden, e);
            return false;
        }
    }

    @Override
    public List<AtencionResumen> historialPorVehiculo(int idVehiculo) {
        List<AtencionResumen> lista = new ArrayList<>();
        String sql = "SELECT o.id_orden, o.fecha_ingreso, o.diagnostico, o.estado, u.nombres AS mecanico, "
                + "COALESCE(SUM(d.subtotal), 0) AS total "
                + "FROM ordenes_servicio o "
                + "LEFT JOIN usuarios u ON o.id_mecanico = u.id_usuario "
                + "LEFT JOIN detalle_orden d ON d.id_orden = o.id_orden "
                + "WHERE o.id_vehiculo = ? "
                + "GROUP BY o.id_orden, o.fecha_ingreso, o.diagnostico, o.estado, u.nombres "
                + "ORDER BY o.fecha_ingreso DESC";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AtencionResumen(rs.getInt("id_orden"),
                            rs.getTimestamp("fecha_ingreso").toLocalDateTime(), rs.getString("diagnostico"),
                            rs.getString("estado"), rs.getString("mecanico"), rs.getBigDecimal("total")));
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al consultar el historial del vehículo {}", idVehiculo, e);
        }
        return lista;
    }

    /** Ejecuta un UPDATE con dos parámetros y registra el resultado en la bitácora. */
    private boolean actualizar(String sql, Object valor, int idOrden, String mensajeBitacora) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            ps.setInt(2, idOrden);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info(mensajeBitacora);
            }
            return ok;
        } catch (SQLException e) {
            LOG.error("Error al actualizar la orden {}", idOrden, e);
            return false;
        }
    }

    /** Convierte una fila de la consulta en un objeto OrdenServicio. */
    private OrdenServicio mapear(ResultSet rs) throws SQLException {
        OrdenServicio o = new OrdenServicio();
        o.setIdOrden(rs.getInt("id_orden"));
        o.setIdVehiculo(rs.getInt("id_vehiculo"));
        int idMecanico = rs.getInt("id_mecanico");
        o.setIdMecanico(rs.wasNull() ? null : idMecanico);
        o.setDiagnostico(rs.getString("diagnostico"));
        o.setEstado(rs.getString("estado"));
        o.setFechaIngreso(rs.getTimestamp("fecha_ingreso").toLocalDateTime());
        o.setPlaca(rs.getString("placa"));
        o.setMoto(rs.getString("moto").trim());
        o.setNombreCliente(rs.getString("cliente"));
        o.setTelefonoCliente(rs.getString("telefono"));
        o.setNombreMecanico(rs.getString("mecanico"));
        return o;
    }
}
