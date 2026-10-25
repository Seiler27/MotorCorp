package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.Reporte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.EstadoOrden;
import util.Formato;
import util.TipoReporte;

/**
 * Implementación de ReporteDAO para PostgreSQL.
 */
public class ReporteDAOImpl implements ReporteDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteDAOImpl.class);
    private static final int LIMITE_STOCK_BAJO = 5;

    @Override
    public Reporte generar(TipoReporte tipo, LocalDate desde, LocalDate hasta) {
        return switch (tipo) {
            case VENTAS -> ventas(desde, hasta);
            case ATENCIONES -> atenciones(desde, hasta);
            case INVENTARIO -> inventario();
        };
    }

    private Reporte ventas(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT cp.id_comprobante, cp.fecha_emision, cp.tipo, "
                + "c.nombres || ' ' || c.apellidos AS cliente, v.placa, u.nombres AS cajero, cp.total_pagado "
                + "FROM comprobantes cp "
                + "JOIN ordenes_servicio o ON cp.id_orden = o.id_orden "
                + "JOIN vehiculos v ON o.id_vehiculo = v.id_vehiculo "
                + "JOIN clientes c ON v.id_cliente = c.id_cliente "
                + "JOIN usuarios u ON cp.id_cajero = u.id_usuario "
                + "WHERE cp.fecha_emision::date BETWEEN ? AND ? ORDER BY cp.fecha_emision";
        List<Object[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal monto = rs.getBigDecimal("total_pagado");
                    filas.add(new Object[]{rs.getInt("id_comprobante"), rs.getTimestamp("fecha_emision").toLocalDateTime(),
                        rs.getString("tipo"), rs.getString("cliente"), rs.getString("placa"), rs.getString("cajero"), monto});
                    total = total.add(monto);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error en el reporte de ventas", e);
        }
        return new Reporte(TipoReporte.VENTAS.getTitulo(),
                List.of("N°", "Fecha", "Tipo", "Cliente", "Placa", "Cajero", "Total"), filas,
                filas.size() + " comprobante(s)  ·  Total vendido: " + Formato.soles(total));
    }

    private Reporte atenciones(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT o.id_orden, o.fecha_ingreso, v.placa, c.nombres || ' ' || c.apellidos AS cliente, "
                + "u.nombres AS mecanico, o.estado, o.diagnostico, COALESCE(SUM(d.subtotal), 0) AS repuestos "
                + "FROM ordenes_servicio o "
                + "JOIN vehiculos v ON o.id_vehiculo = v.id_vehiculo "
                + "JOIN clientes c ON v.id_cliente = c.id_cliente "
                + "LEFT JOIN usuarios u ON o.id_mecanico = u.id_usuario "
                + "LEFT JOIN detalle_orden d ON d.id_orden = o.id_orden "
                + "WHERE o.fecha_ingreso::date BETWEEN ? AND ? "
                + "GROUP BY o.id_orden, o.fecha_ingreso, v.placa, c.nombres, c.apellidos, u.nombres, o.estado, o.diagnostico "
                + "ORDER BY o.fecha_ingreso";
        List<Object[]> filas = new ArrayList<>();
        int entregadas = 0;
        BigDecimal repuestos = BigDecimal.ZERO;
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    BigDecimal monto = rs.getBigDecimal("repuestos");
                    filas.add(new Object[]{rs.getInt("id_orden"), rs.getTimestamp("fecha_ingreso").toLocalDateTime(),
                        rs.getString("placa"), rs.getString("cliente"),
                        rs.getString("mecanico") == null ? "Sin asignar" : rs.getString("mecanico"),
                        estado, rs.getString("diagnostico"), monto});
                    if (EstadoOrden.ENTREGADO.equals(estado)) {
                        entregadas++;
                    }
                    repuestos = repuestos.add(monto);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error en el reporte de atenciones", e);
        }
        return new Reporte(TipoReporte.ATENCIONES.getTitulo(),
                List.of("Orden", "Ingreso", "Placa", "Cliente", "Mecánico", "Estado", "Diagnóstico", "Repuestos"), filas,
                filas.size() + " atención(es)  ·  " + entregadas + " entregada(s)  ·  Repuestos usados: " + Formato.soles(repuestos));
    }

    private Reporte inventario() {
        String sql = "SELECT id_producto, categoria, nombre, stock_actual, precio_unitario, "
                + "stock_actual * precio_unitario AS valor FROM productos ORDER BY categoria, nombre";
        List<Object[]> filas = new ArrayList<>();
        int stockBajo = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int stock = rs.getInt("stock_actual");
                BigDecimal valor = rs.getBigDecimal("valor");
                boolean bajo = stock <= LIMITE_STOCK_BAJO;
                filas.add(new Object[]{String.format("P-%03d", rs.getInt("id_producto")), rs.getString("categoria"),
                    rs.getString("nombre"), stock, rs.getBigDecimal("precio_unitario"), valor, bajo ? "Stock bajo" : "Disponible"});
                if (bajo) {
                    stockBajo++;
                }
                valorTotal = valorTotal.add(valor);
            }
        } catch (SQLException e) {
            LOG.error("Error en el reporte de inventario", e);
        }
        return new Reporte(TipoReporte.INVENTARIO.getTitulo(),
                List.of("Código", "Categoría", "Producto", "Stock", "Precio unitario", "Valor en stock", "Estado"), filas,
                filas.size() + " producto(s)  ·  " + stockBajo + " con stock bajo  ·  Valor del inventario: " + Formato.soles(valorTotal));
    }
}
