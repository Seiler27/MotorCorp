/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package util;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * Pruebas de Formato
 */
class FormatoTest {

    @Test
    void losNombresSeGuardanConMayusculaInicial() {
        assertEquals("Jose Luis", Formato.nombre("jOSE luis"));
    }

    @Test
    void seEliminanLosEspaciosSobrantesDeLosNombres() {
        assertEquals("Juan Carlos", Formato.nombre("  juan   carlos "));
    }

    @Test
    void unNombreVacioONuloDevuelveTextoVacio() {
        assertEquals("", Formato.nombre(""));
        assertEquals("", Formato.nombre(null));
    }

    @Test
    void laPlacaSeGuardaEnMayusculasYSinEspacios() {
        assertEquals("MHS-123", Formato.placa(" mhs-123 "));
    }

    @Test
    void losMontosSeMuestranEnSolesConDosDecimales() {
        assertEquals("S/ 298.50", Formato.soles(new BigDecimal("298.5")));
    }

    @Test
    void losMontosGrandesUsanSeparadorDeMiles() {
        assertEquals("S/ 1,234.50", Formato.soles(new BigDecimal("1234.5")));
    }
}
