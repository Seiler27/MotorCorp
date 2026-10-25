/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * Los que operan el sistema
 */
public class Usuario {
    
    private int idUsuario;
    private String nombres;
    private String username;
    private String password;
    private String codigoSecreto;
    private int idRol;
    private String nombreRol;

    public Usuario() {
    }

    public Usuario(String nombres, String username, String password, String codigoSecreto, int idRol) {
        this.nombres = nombres;
        this.username = username;
        this.password = password;
        this.codigoSecreto = codigoSecreto;
        this.idRol = idRol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCodigoSecreto() {
        return codigoSecreto;
    }

    public void setCodigoSecreto(String codigoSecreto) {
        this.codigoSecreto = codigoSecreto;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    @Override
    public String toString() {
        return nombres;
    }

    
    
    
    
}
