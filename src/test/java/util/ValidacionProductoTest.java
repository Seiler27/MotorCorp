package util;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las reglas del inventario (TDD).
 */
class ValidacionProductoTest {

    @Test
    void unProductoConDatosCorrectosNoTieneErrores() {
        assertTrue(ValidacionProducto.validar("Repuesto", "Bujía NGK", "10", "15.00").isEmpty());
    }

    @Test
    void unaCategoriaDesconocidaEsRechazada() {
        assertEquals(1, ValidacionProducto.validar("Comida", "Bujía", "10", "15").size());
    }

    @Test
    void elStockNoPuedeSerNegativoNiDecimal() {
        assertNull(ValidacionProducto.aEntero("-3"));
        assertNull(ValidacionProducto.aEntero("2.5"));
        assertEquals(0, (int) ValidacionProducto.aEntero("0"));
    }

    @Test
    void elPrecioAceptaComaOPuntoYHastaDosDecimales() {
        assertEquals(new BigDecimal("35.50"), ValidacionProducto.aPrecio("35,50"));
        assertNull(ValidacionProducto.aPrecio("35.555"));
    }

    @Test
    void elPrecioDebeSerMayorACero() {
        assertNull(ValidacionProducto.aPrecio("0"));
        assertNull(ValidacionProducto.aPrecio("abc"));
    }

    @Test
    void unProductoVacioTieneCuatroErrores() {
        assertEquals(4, ValidacionProducto.validar("", "", "", "").size());
    }
}
