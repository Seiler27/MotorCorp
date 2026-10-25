package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de negocio para registrar un cliente y su moto.
 * Devuelve la lista de errores encontrados (vacía si todo está correcto).
 */
public final class ValidacionCliente {

    private ValidacionCliente() {
    }

    /** Valida los datos del cliente. */
    public static List<String> validarCliente(String dni, String nombres, String apellidos,
            String celular, String correo) {
        List<String> errores = new ArrayList<>();
        if (!Validador.esDniValido(dni)) {
            errores.add("El DNI debe tener exactamente 8 dígitos.");
        }
        if (Validador.estaVacio(nombres)) {
            errores.add("Los nombres son obligatorios.");
        }
        if (Validador.estaVacio(apellidos)) {
            errores.add("Los apellidos son obligatorios.");
        }
        if (!Validador.esCelularValido(celular)) {
            errores.add("El celular debe tener 9 dígitos y empezar con 9.");
        }
        if (!Validador.esCorreoValido(correo)) {
            errores.add("El correo no tiene un formato válido.");
        }
        return errores;
    }

    /** Valida los datos de la moto (solo si se escribió una placa o una marca). */
    public static List<String> validarMoto(String placa, String marca) {
        List<String> errores = new ArrayList<>();
        if (!Validador.esPlacaValida(placa)) {
            errores.add("La placa debe tener el formato MHS-123 o 4521-AB.");
        }
        if (Validador.estaVacio(marca)) {
            errores.add("La marca de la moto es obligatoria.");
        }
        return errores;
    }

    /** Indica si el usuario escribió algún dato de la moto. */
    public static boolean hayDatosDeMoto(String placa, String marca, String modelo) {
        return !Validador.estaVacio(placa) || !Validador.estaVacio(marca) || !Validador.estaVacio(modelo);
    }
}
