/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.List;
import modelo.Usuario;

/**
 *
 * operaciones de base de datos para los usuarios 
 */
public interface UsuarioDAO {
    
    Usuario buscarPorUsername(String username);
    
    List<Usuario> listarPorRol(String nombreRol);
    
    boolean registrar(Usuario usuario);

    /** Indica si el código secreto pertenece a algún administrador del taller. */
    boolean esCodigoSecretoValido(String codigo);
}
