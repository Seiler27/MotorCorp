package modelo;

import java.util.List;

/**
 * Resultado de un reporte: título, nombres de columnas, filas y un resumen.
 * Cada fila es un arreglo de valores (texto, números, montos o fechas).
 */
public record Reporte(String titulo, List<String> columnas, List<Object[]> filas, String resumen) {
}
