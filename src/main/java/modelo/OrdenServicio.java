/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDateTime;

/**
 *
 * Orden de servicio del Taller, Cola de espera 
 * y historial de cada vehiculo
 */
public class OrdenServicio {
    
    private int idOrden;
    private int idVehiculo;
    private Integer idMecanico;
    private String diagnostico;
    private String estado;
    private LocalDateTime fechaIngreso;
    
    //Datos de siguiente tabla, para la pantalla
    private String placa;
    private String nombreCliente;
    private String nombreMecanico;
    private String telefonoCliente;
    private String moto;              // marca y modelo, ej: "Honda CB 190R"

    public OrdenServicio() {
    }

    public OrdenServicio(int idVehiculo, String diagnostico) {
        this.idVehiculo = idVehiculo;
        this.diagnostico = diagnostico;
        this.estado = "En espera";
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public int getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(int idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public Integer getIdMecanico() {
        return idMecanico;
    }

    public void setIdMecanico(Integer idMecanico) {
        this.idMecanico = idMecanico;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreMecanico() {
        return nombreMecanico;
    }

    public void setNombreMecanico(String nombreMecanico) {
        this.nombreMecanico = nombreMecanico;
    }

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getMoto() {
        return moto;
    }

    public void setMoto(String moto) {
        this.moto = moto;
    }

    @Override
    public String toString() {
        return "Orden #" + idOrden + " - " + placa;
    }
    
    
    
}
