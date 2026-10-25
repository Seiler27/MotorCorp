package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la clase Validador.
 */
class ValidadorTest {

    // ----- DNI -----
    @Test
    void dniConOchoNumerosEsValido() {
        assertTrue(Validador.esDniValido("12345678"));
    }

    @Test
    void dniConMenosNumerosNoEsValido() {
        assertFalse(Validador.esDniValido("1234567"));
    }

    @Test
    void dniConLetrasNoEsValido() {
        assertFalse(Validador.esDniValido("1234567A"));
    }

    @Test
    void dniVacioONuloNoEsValido() {
        assertFalse(Validador.esDniValido(""));
        assertFalse(Validador.esDniValido(null));
    }

    // ----- Celular -----
    @Test
    void celularDeNueveNumerosQueEmpiezaEnNueveEsValido() {
        assertTrue(Validador.esCelularValido("987654321"));
    }

    @Test
    void celularQueNoEmpiezaEnNueveNoEsValido() {
        assertFalse(Validador.esCelularValido("123456789"));
    }

    @Test
    void celularConOchoNumerosNoEsValido() {
        assertFalse(Validador.esCelularValido("98765432"));
    }

    // ----- Placa -----
    @Test
    void placasConFormatoCorrectoSonValidas() {
        assertTrue(Validador.esPlacaValida("MHS-123"));
        assertTrue(Validador.esPlacaValida("4521-AB"));
    }

    @Test
    void placaSinGuionNoEsValida() {
        assertFalse(Validador.esPlacaValida("MHS123"));
    }

    // ----- Correo -----
    @Test
    void correoConArrobaEsValido() {
        assertTrue(Validador.esCorreoValido("juan@gmail.com"));
    }

    @Test
    void correoSinArrobaNoEsValido() {
        assertFalse(Validador.esCorreoValido("juan.gmail.com"));
    }

    @Test
    void correoVacioSeAceptaPorqueEsOpcional() {
        assertTrue(Validador.esCorreoValido(""));
    }

    // ----- Campos obligatorios -----
    @Test
    void textoConSoloEspaciosEstaVacio() {
        assertTrue(Validador.estaVacio("   "));
    }

    @Test
    void textoConLetrasNoEstaVacio() {
        assertFalse(Validador.estaVacio("Juan"));
    }
}