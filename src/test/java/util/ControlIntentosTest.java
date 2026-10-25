package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la clase ControlIntentos (TDD).
 */
class ControlIntentosTest {

    @Test
    void usuarioSinFallosNoEstaBloqueadoYTieneTresIntentos() {
        ControlIntentos control = new ControlIntentos();
        assertFalse(control.estaBloqueado("admin"));
        assertEquals(3, control.intentosRestantes("admin"));
    }

    @Test
    void conDosFallosAunPuedeIntentarUnaVezMas() {
        ControlIntentos control = new ControlIntentos();
        control.registrarFallo("admin");
        control.registrarFallo("admin");
        assertFalse(control.estaBloqueado("admin"));
        assertEquals(1, control.intentosRestantes("admin"));
    }

    @Test
    void conTresFallosElUsuarioQuedaBloqueado() {
        ControlIntentos control = new ControlIntentos();
        control.registrarFallo("admin");
        control.registrarFallo("admin");
        control.registrarFallo("admin");
        assertTrue(control.estaBloqueado("admin"));
    }

    @Test
    void unIngresoCorrectoReiniciaLosIntentos() {
        ControlIntentos control = new ControlIntentos();
        control.registrarFallo("admin");
        control.registrarFallo("admin");
        control.reiniciar("admin");
        assertEquals(3, control.intentosRestantes("admin"));
    }

    @Test
    void elBloqueoAfectaSoloAlUsuarioQueFallo() {
        ControlIntentos control = new ControlIntentos();
        control.registrarFallo("admin");
        control.registrarFallo("admin");
        control.registrarFallo("admin");
        assertFalse(control.estaBloqueado("recepcion"));
    }
}