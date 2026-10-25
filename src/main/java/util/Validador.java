/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * Valida datos ingresador antes de guardar
 */
public final class Validador {

    private Validador() {
    }

    public static boolean esDniValido(String dni) {
        return dni != null && dni.matches("\\d{8}");
    }

    public static boolean esCelularValido(String celular) {
        return celular != null && celular.matches("9\\d{8}");
    }

    public static boolean esPlacaValida(String placa) {
        return placa != null && placa.matches("[A-Z0-9]{3,4}-[A-Z0-9]{2,3}");
    }

    public static boolean esCorreoValido(String correo) {
        if (estaVacio(correo)) {
            return true;
        }
        return correo.matches("[\\w.-]+@[\\w-]+\\.[a-zA-Z]{2,}");
    }

    public static boolean estaVacio(String texto) {
        return StringUtils.isBlank(texto);
    }
}
