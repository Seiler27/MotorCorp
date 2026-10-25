package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la clase Seguridad.
 */
class SeguridadTest {

    @Test
    void laContrasenaCifradaNoSeGuardaEnTextoPlano() {
        String cifrada = Seguridad.cifrar("Admin123");
        assertFalse(cifrada.contains("Admin123"));
    }

    @Test
    void laMismaContrasenaGeneraCifradosDistintosPorLaSal() {
        assertNotEquals(Seguridad.cifrar("Admin123"), Seguridad.cifrar("Admin123"));
    }

    @Test
    void verificarConContrasenaCorrectaDevuelveTrue() {
        String cifrada = Seguridad.cifrar("Admin123");
        assertTrue(Seguridad.verificar("Admin123", cifrada));
    }

    @Test
    void verificarConContrasenaIncorrectaDevuelveFalse() {
        String cifrada = Seguridad.cifrar("Admin123");
        assertFalse(Seguridad.verificar("otraClave", cifrada));
    }

    @Test
    void verificaLaContrasenaDelAdminGuardadaEnLaBaseDeDatos() {
        String guardadaEnBD = "V8ZjJOe98bBWyRuqiD6A3A==:0b23b1c8e3def3894c5a236ae61e7c158584773c3ef78e7d407d063e9e585ce9";
        assertTrue(Seguridad.verificar("Admin123", guardadaEnBD));
    }

    @Test
    void verificarConValoresNulosDevuelveFalse() {
        assertFalse(Seguridad.verificar(null, null));
    }
}