package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import modelo.OrdenServicio;
import modelo.VentaResumen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de DashboardDAO para PostgreSQL.
 */
public class DashboardDAOImpl implements DashboardDAO {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardDAOImpl.class);

    @Override
    public BigDecimal ventasDeHoy() {
        return sumar("SELECT COALESCE(SUM(total_pagado), 0) FROM comprobantes "
                + "WHERE fecha_emision::date = CURRENT_DATE");
    }

    @Override
    public BigDecimal ingresosDelMes() {
        return sumar("SELECT COALESCE(SUM(total_pagado), 0) FROM comprobantes "
                + "WHERE date_trunc('month', fecha_emision) = date_trunc('month', CURRENT_DATE)");
    }

    @Override
    public int motosEnTaller() {
        return contar("SELECT COUNT(*) FROM ordenes_servicio "
                + "WHERE estado IN ('En espera', 'En reparación')", null);
    }

    @Override
    public int productosConStockBajo(int limite) {
        return contar("SELECT COUNT(*) FROM productos WHERE stock_actual <= ?", limite);
    }

    @Override
    public List<OrdenServicio> colaDeEspera(int cantidad) {
        List<OrdenServicio> lista = new ArrayList<>();
        String sql = "SELECT o.id_orden, o.estado, o.fecha_ingreso, o.diagnostico, v.placa, "
                + "c.nombres || ' ' || c.apellidos AS cliente, u.nombres AS mecanico "
                + "FROM ordenes_servicio o "
                + "JOIN vehiculos v ON o.id_vehiculo = v.id_vehiculo "
                + "JOIN clientes c ON v.id_cliente = c.id_cliente "
                + "LEFT JOIN usuarios u ON o.id_mecanico = u.id_usuario "
                + "WHERE o.estado IN ('En espera', 'En reparación') "
                + "ORDER BY o.fecha_ingreso LIMIT ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrdenServicio o = new OrdenServicio();
                    o.setIdOrden(rs.getInt("id_orden"));
                    o.setEstado(rs.getString("estado"));
                    o.setFechaIngreso(rs.getTimestamp("fecha_ingreso").toLocalDateTime());
                    o.setDiagnostico(rs.getString("diagnostico"));
                    o.setPlaca(rs.getString("placa"));
                    o.setNombreCliente(rs.getString("cliente"));
                    o.setNombreMecanico(rs.getString("mecanico"));
                    lista.add(o);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al consultar la cola de espera", e);
        }
        return lista;
    }

    @Override
    public List<VentaResumen> ultimasVentas(int cantidad) {
        List<VentaResumen> lista = new ArrayList<>();
        String sql = "SELECT cp.id_comprobante, cp.tipo, cp.total_pagado, cp.fecha_emision, "
                + "c.nombres || ' ' || c.apellidos AS cliente "
                + "FROM comprobantes cp "
                + "JOIN ordenes_servicio o ON cp.id_orden = o.id_orden "
                + "JOIN vehiculos v ON o.id_vehiculo = v.id_vehiculo "
                + "JOIN clientes c ON v.id_cliente = c.id_cliente "
                + "ORDER BY cp.fecha_emision DESC LIMIT ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha_emision");
                    lista.add(new VentaResumen(
                            rs.getString("tipo") + " N° " + rs.getInt("id_comprobante"),
                            rs.getString("cliente"),
                            fecha.toLocalDateTime(),
                            rs.getBigDecimal("total_pagado")));
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al consultar las últimas ventas", e);
        }
        return lista;
    }

    /** Ejecuta una consulta que devuelve un solo monto. */
    private BigDecimal sumar(String sql) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException e) {
            LOG.error("Error en la consulta de montos", e);
            return BigDecimal.ZERO;
        }
    }

    /** Ejecuta una consulta COUNT(*) con un parámetro opcional. */
    private int contar(String sql, Integer parametro) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LOG.error("Error en la consulta de conteo", e);
            return 0;
        }
    }
}
