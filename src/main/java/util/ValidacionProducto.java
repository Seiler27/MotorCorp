package util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reglas de negocio para registrar o actualizar un producto del inventario.
 */
public final class ValidacionProducto {

    public static final Set<String> CATEGORIAS = Set.of("Repuesto", "Accesorio", "Motocicleta");

    private ValidacionProducto() {
    }

    /** Devuelve la lista de errores (vacía si los datos son correctos). */
    public static List<String> validar(String categoria, String nombre, String stock, String precio) {
        List<String> errores = new ArrayList<>();
        if (!CATEGORIAS.contains(categoria)) {
            errores.add("La categoría debe ser Repuesto, Accesorio o Motocicleta.");
        }
        if (Validador.estaVacio(nombre)) {
            errores.add("El nombre es obligatorio.");
        }
        if (aEntero(stock) == null) {
            errores.add("El stock debe ser un número entero mayor o igual a 0.");
        }
        if (aPrecio(precio) == null) {
            errores.add("El precio debe ser un monto mayor a 0 (ej: 35.50).");
        }
        return errores;
    }

    /** Convierte el texto a un entero no negativo; devuelve null si no es válido. */
    public static Integer aEntero(String texto) {
        if (texto == null || !texto.trim().matches("\\d{1,6}")) {
            return null;
        }
        return Integer.valueOf(texto.trim());
    }

    /** Convierte el texto a un precio positivo con hasta 2 decimales; devuelve null si no es válido. */
    public static BigDecimal aPrecio(String texto) {
        if (texto == null || !texto.trim().replace(",", ".").matches("\\d{1,7}(\\.\\d{1,2})?")) {
            return null;
        }
        BigDecimal precio = new BigDecimal(texto.trim().replace(",", "."));
        return precio.signum() > 0 ? precio : null;
    }
}
