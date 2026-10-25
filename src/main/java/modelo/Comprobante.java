/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author eda
 */
public class Comprobante {
    
    private int idComprobante;
    private int idOrden;
    private int idCajero;
    private String tipo;
    private BigDecimal totalPagado;
    private LocalDateTime fechaEmision;

    public Comprobante() {
    }

    public Comprobante(int idOrden, int idCajero, String tipo, BigDecimal totalPagado) {
        this.idOrden = idOrden;
        this.idCajero = idCajero;
        this.tipo = tipo;
        this.totalPagado = totalPagado;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public int getIdCajero() {
        return idCajero;
    }

    public void setIdCajero(int idCajero) {
        this.idCajero = idCajero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    @Override
    public String toString() {
        return tipo + " N° " + idComprobante;
    }
    
    
    
}
