package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Vehiculo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de VehiculoDAO para PostgreSQL.
 */
public class VehiculoDAOImpl implements VehiculoDAO {

    private static final Logger LOG = LoggerFactory.getLogger(VehiculoDAOImpl.class);

    @Override
    public List<Vehiculo> listarPorCliente(int idCliente) {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT id_vehiculo, placa, marca, modelo, id_cliente FROM vehiculos "
                + "WHERE id_cliente = ? ORDER BY placa";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vehiculo v = new Vehiculo();
                    v.setIdVehiculo(rs.getInt("id_vehiculo"));
                    v.setPlaca(rs.getString("placa"));
                    v.setMarca(rs.getString("marca"));
                    v.setModelo(rs.getString("modelo"));
                    v.setIdCliente(rs.getInt("id_cliente"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al listar las motos del cliente {}", idCliente, e);
        }
        return lista;
    }

    @Override
    public boolean existePlaca(String placa, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM vehiculos WHERE placa = ? AND id_vehiculo <> ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, placa);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.error("Error al verificar la placa {}", placa, e);
            return false;
        }
    }

    @Override
    public boolean registrar(Vehiculo vehiculo) {
        String sql = "INSERT INTO vehiculos (placa, marca, modelo, id_cliente) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo() == null || vehiculo.getModelo().isBlank() ? null : vehiculo.getModelo());
            ps.setInt(4, vehiculo.getIdCliente());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("Moto registrada: {}", vehiculo.getPlaca());
            }
            return ok;
        } catch (SQLException e) {
            LOG.error("Error al registrar la moto {}", vehiculo.getPlaca(), e);
            return false;
        }
    }

    @Override
    public boolean actualizar(Vehiculo vehiculo) {
        String sql = "UPDATE vehiculos SET placa = ?, marca = ?, modelo = ? WHERE id_vehiculo = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo() == null || vehiculo.getModelo().isBlank() ? null : vehiculo.getModelo());
            ps.setInt(4, vehiculo.getIdVehiculo());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("Moto actualizada: {}", vehiculo.getPlaca());
            }
            return ok;
        } catch (SQLException e) {
            LOG.error("Error al actualizar la moto {}", vehiculo.getPlaca(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(int idVehiculo) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM vehiculos WHERE id_vehiculo = ?")) {
            ps.setInt(1, idVehiculo);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("Moto eliminada: id {}", idVehiculo);
            }
            return ok;
        } catch (SQLException e) {
            LOG.warn("No se eliminó la moto {}: tiene órdenes de servicio registradas", idVehiculo);
            return false;
        }
    }
}
