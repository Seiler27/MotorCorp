package util;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las reglas para registrar clientes y motos (TDD).
 */
class ValidacionClienteTest {

    @Test
    void unClienteConDatosCorrectosNoTieneErrores() {
        List<String> errores = ValidacionCliente.validarCliente("12345678", "Juan", "Perez", "987654321", "");
        assertTrue(errores.isEmpty());
    }

    @Test
    void unClienteSinDatosTieneCuatroErrores() {
        List<String> errores = ValidacionCliente.validarCliente("", "", "", "", "");
        assertEquals(4, errores.size());
    }

    @Test
    void unDniDeSieteDigitosEsRechazado() {
        List<String> errores = ValidacionCliente.validarCliente("1234567", "Juan", "Perez", "987654321", null);
        assertEquals(List.of("El DNI debe tener exactamente 8 dígitos."), errores);
    }

    @Test
    void unCorreoMalEscritoEsRechazado() {
        List<String> errores = ValidacionCliente.validarCliente("12345678", "Juan", "Perez", "987654321", "juan@");
        assertEquals(1, errores.size());
    }

    @Test
    void unaMotoConPlacaYMarcaEsValida() {
        assertTrue(ValidacionCliente.validarMoto("MHS-123", "Honda").isEmpty());
    }

    @Test
    void unaMotoSinMarcaYConPlacaInvalidaTieneDosErrores() {
        assertEquals(2, ValidacionCliente.validarMoto("MHS123", "").size());
    }

    @Test
    void siNoSeEscribeNingunDatoDeMotoNoSeRegistraMoto() {
        assertFalse(ValidacionCliente.hayDatosDeMoto("", " ", null));
        assertTrue(ValidacionCliente.hayDatosDeMoto("MHS-123", "", ""));
    }
}
