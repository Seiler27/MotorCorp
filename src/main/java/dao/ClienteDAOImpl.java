package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.Vehiculo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de ClienteDAO para PostgreSQL.
 */
public class ClienteDAOImpl implements ClienteDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteDAOImpl.class);

    @Override
    public List<Cliente> buscar(String texto) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, dni, nombres, apellidos, telefono, correo FROM clientes "
                + "WHERE dni LIKE ? OR nombres ILIKE ? OR apellidos ILIKE ? "
                + "ORDER BY apellidos, nombres";
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente();
                    c.setIdCliente(rs.getInt("id_cliente"));
                    c.setDni(rs.getString("dni"));
                    c.setNombres(rs.getString("nombres"));
                    c.setApellidos(rs.getString("apellidos"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setCorreo(rs.getString("correo"));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al buscar clientes", e);
        }
        return lista;
    }

    @Override
    public boolean existeDni(String dni, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE dni = ? AND id_cliente <> ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.error("Error al verificar el DNI {}", dni, e);
            return false;
        }
    }

    @Override
    public int registrar(Cliente cliente, Vehiculo vehiculo) {
        String sqlCliente = "INSERT INTO clientes (dni, nombres, apellidos, telefono, correo) VALUES (?, ?, ?, ?, ?)";
        String sqlMoto = "INSERT INTO vehiculos (placa, marca, modelo, id_cliente) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);   // inicio de la transacción
            try (PreparedStatement ps = cn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cliente.getDni());
                ps.setString(2, cliente.getNombres());
                ps.setString(3, cliente.getApellidos());
                ps.setString(4, cliente.getTelefono());
                ps.setString(5, vacioANulo(cliente.getCorreo()));
                ps.executeUpdate();
                int idCliente;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idCliente = rs.getInt(1);
                }
                if (vehiculo != null) {
                    try (PreparedStatement pm = cn.prepareStatement(sqlMoto)) {
                        pm.setString(1, vehiculo.getPlaca());
                        pm.setString(2, vehiculo.getMarca());
                        pm.setString(3, vacioANulo(vehiculo.getModelo()));
                        pm.setInt(4, idCliente);
                        pm.executeUpdate();
                    }
                }
                cn.commit();           // se confirman los dos registros
                LOG.info("Cliente registrado: DNI {}{}", cliente.getDni(),
                        vehiculo != null ? " con moto " + vehiculo.getPlaca() : "");
                return idCliente;
            } catch (SQLException e) {
                cn.rollback();         // si algo falla, no se guarda nada
                throw e;
            }
        } catch (SQLException e) {
            LOG.error("Error al registrar el cliente {}", cliente.getDni(), e);
            return 0;
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET dni = ?, nombres = ?, apellidos = ?, telefono = ?, correo = ? "
                + "WHERE id_cliente = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cliente.getDni());
            ps.setString(2, cliente.getNombres());
            ps.setString(3, cliente.getApellidos());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, vacioANulo(cliente.getCorreo()));
            ps.setInt(6, cliente.getIdCliente());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("Cliente actualizado: DNI {}", cliente.getDni());
            }
            return ok;
        } catch (SQLException e) {
            LOG.error("Error al actualizar el cliente {}", cliente.getDni(), e);
            return false;
        }
    }

    @Override
    public boolean eliminar(int idCliente) {
        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement pm = cn.prepareStatement("DELETE FROM vehiculos WHERE id_cliente = ?");
                 PreparedStatement pc = cn.prepareStatement("DELETE FROM clientes WHERE id_cliente = ?")) {
                pm.setInt(1, idCliente);
                pm.executeUpdate();
                pc.setInt(1, idCliente);
                boolean ok = pc.executeUpdate() > 0;
                cn.commit();
                LOG.info("Cliente eliminado: id {}", idCliente);
                return ok;
            } catch (SQLException e) {
                cn.rollback();
                LOG.warn("No se eliminó el cliente {}: tiene órdenes de servicio registradas", idCliente);
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Error al eliminar el cliente {}", idCliente, e);
            return false;
        }
    }

    /** Guarda NULL en la base cuando un campo opcional está vacío. */
    private static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto;
    }
}
