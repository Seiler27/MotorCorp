/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * abre coneexion a la base de datos
 * los datos de conexion se leen de db.properties
 */
public final class Conexion {
    
    private static final Properties CONFIG = cargarConfiguracion();
    
    private Conexion() {
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.usuario"),
                CONFIG.getProperty("db.password"));
    }

    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream archivo = Conexion.class.getResourceAsStream("/db.properties")) {
            if (archivo == null) {
                throw new IllegalStateException("No se encontró db.properties en src/main/resources");
            }
            props.load(archivo);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer db.properties", e);
        }
        return props;
    }
    
}
