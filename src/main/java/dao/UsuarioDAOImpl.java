/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.List;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * implementacion de UsuarioDAO para la base de datos
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioDAOImpl.class);

    private static final String SQL_BASE =
            "SELECT u.id_usuario, u.nombres, u.username, u.password, u.codigo_secreto, "
            + "u.id_rol, r.nombre_rol "
            + "FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol ";

    @Override
    public Usuario buscarPorUsername(String username) {
        String sql = SQL_BASE + "WHERE u.username = ?";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al buscar el usuario {}", username, e);
        }
        return null;
    }

    @Override
    public List<Usuario> listarPorRol(String nombreRol) {
        List<Usuario> lista = new ArrayList<>();
        String sql = SQL_BASE + "WHERE r.nombre_rol = ? ORDER BY u.nombres";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            LOG.error("Error al listar usuarios del rol {}", nombreRol, e);
        }
        return lista;
    }

    @Override
    public boolean registrar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombres, username, password, codigo_secreto, id_rol) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombres());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getCodigoSecreto());
            ps.setInt(5, usuario.getIdRol());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                LOG.info("Usuario registrado: {}", usuario.getUsername());
            }
            return ok;
        } catch (SQLException e) {
            LOG.error("Error al registrar el usuario {}", usuario.getUsername(), e);
            return false;
        }
    }

    /** Convierte una fila de la consulta en un objeto Usuario. */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombres(rs.getString("nombres"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setCodigoSecreto(rs.getString("codigo_secreto"));
        u.setIdRol(rs.getInt("id_rol"));
        u.setNombreRol(rs.getString("nombre_rol"));
        return u;
    }
}
