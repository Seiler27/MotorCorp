/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package motorcorp;

import dao.Conexion;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Clase del sistema de gestion
 */
public class MotorCorp {

    private static final Logger LOG = LoggerFactory.getLogger(MotorCorp.class);

    public static void main(String[] args) {
        try (Connection cn = Conexion.getConexion()) {
            LOG.info("Conexión exitosa a {}", cn.getMetaData().getURL());
        } catch (Exception e) {
            LOG.error("No se pudo conectar a la base de datos", e);
        }
    }
}
