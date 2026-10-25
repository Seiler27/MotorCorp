package util;

/**
 * Tipos de reporte disponibles en el módulo de Reportes (RF10).
 */
public enum TipoReporte {
    VENTAS("Ventas", "Reporte de ventas", true),
    ATENCIONES("Atenciones del taller", "Reporte de atenciones del taller", true),
    INVENTARIO("Inventario", "Reporte de inventario", false);

    private final String nombre;
    private final String titulo;
    private final boolean usaFechas;

    TipoReporte(String nombre, String titulo, boolean usaFechas) {
        this.nombre = nombre;
        this.titulo = titulo;
        this.usaFechas = usaFechas;
    }

    /** Título que aparece en la vista previa y en el Excel. */
    public String getTitulo() {
        return titulo;
    }

    /** Indica si el reporte se filtra por rango de fechas (el inventario muestra el stock actual). */
    public boolean usaFechas() {
        return usaFechas;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
