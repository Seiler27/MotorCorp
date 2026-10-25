package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la clase Permisos .
 */
class PermisosTest {

    @Test
    void elAdministradorAccedeATodosLosModulos() {
        for (Modulo modulo : Modulo.values()) {
            assertTrue(Permisos.puedeAcceder("Administrador", modulo));
        }
    }

    @Test
    void recepcionPuedeRegistrarClientesYCobrar() {
        assertTrue(Permisos.puedeAcceder("Recepcion", Modulo.CLIENTES));
        assertTrue(Permisos.puedeAcceder("Recepcion", Modulo.POS));
    }

    @Test
    void recepcionNoAccedeAReportes() {
        assertFalse(Permisos.puedeAcceder("Recepcion", Modulo.REPORTES));
    }

    @Test
    void elMecanicoPuedeVerLaColaYElHistorial() {
        assertTrue(Permisos.puedeAcceder("Mecanico", Modulo.COLA));
        assertTrue(Permisos.puedeAcceder("Mecanico", Modulo.HISTORIAL));
    }

    @Test
    void elMecanicoNoAccedeAClientesReportesNiPuntoDeVenta() {
        assertFalse(Permisos.puedeAcceder("Mecanico", Modulo.CLIENTES));
        assertFalse(Permisos.puedeAcceder("Mecanico", Modulo.REPORTES));
        assertFalse(Permisos.puedeAcceder("Mecanico", Modulo.POS));
    }

    @Test
    void unRolDesconocidoONuloNoAccedeANada() {
        assertFalse(Permisos.puedeAcceder("Invitado", Modulo.INICIO));
        assertFalse(Permisos.puedeAcceder(null, Modulo.INICIO));
    }
}