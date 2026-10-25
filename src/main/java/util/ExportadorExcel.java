package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import modelo.Reporte;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Exporta un reporte a un archivo Excel (.xlsx) usando Apache POI (RNF07).
 * El archivo lleva título, periodo, encabezados con los colores de MotorCorp,
 * montos con formato de soles y el resumen al final.
 */
public final class ExportadorExcel {

    /** Fila (desde 0) donde van los encabezados de las columnas. */
    public static final int FILA_ENCABEZADO = 3;

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExportadorExcel() {
    }

    /** Guarda el reporte en el archivo indicado. */
    public static void exportar(Reporte reporte, String periodo, File archivo) throws IOException {
        try (OutputStream salida = new FileOutputStream(archivo)) {
            exportar(reporte, periodo, salida);
        }
    }

    /** Escribe el reporte en formato Excel en la salida indicada. */
    public static void exportar(Reporte reporte, String periodo, OutputStream salida) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            XSSFSheet hoja = libro.createSheet("Reporte");
            int columnas = reporte.columnas().size();

            // Estilos
            XSSFCellStyle estiloTitulo = libro.createCellStyle();
            XSSFFont fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 16);
            fuenteTitulo.setColor(color(31, 42, 68));
            estiloTitulo.setFont(fuenteTitulo);

            XSSFCellStyle estiloEncabezado = libro.createCellStyle();
            XSSFFont fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(color(255, 255, 255));
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setFillForegroundColor(color(242, 140, 40));
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);

            XSSFCellStyle estiloSoles = libro.createCellStyle();
            estiloSoles.setDataFormat(libro.createDataFormat().getFormat("\"S/\" #,##0.00"));

            XSSFCellStyle estiloResumen = libro.createCellStyle();
            XSSFFont fuenteResumen = libro.createFont();
            fuenteResumen.setBold(true);
            estiloResumen.setFont(fuenteResumen);

            // Título y periodo
            XSSFCell titulo = hoja.createRow(0).createCell(0);
            titulo.setCellValue("MotorCorp - " + reporte.titulo());
            titulo.setCellStyle(estiloTitulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, columnas - 1));
            hoja.createRow(1).createCell(0).setCellValue(periodo);

            // Encabezados
            XSSFRow encabezado = hoja.createRow(FILA_ENCABEZADO);
            for (int i = 0; i < columnas; i++) {
                XSSFCell celda = encabezado.createCell(i);
                celda.setCellValue(reporte.columnas().get(i));
                celda.setCellStyle(estiloEncabezado);
            }

            // Datos
            int numeroFila = FILA_ENCABEZADO + 1;
            for (Object[] valores : reporte.filas()) {
                XSSFRow fila = hoja.createRow(numeroFila++);
                for (int i = 0; i < valores.length; i++) {
                    XSSFCell celda = fila.createCell(i);
                    Object valor = valores[i];
                    if (valor instanceof BigDecimal monto) {
                        celda.setCellValue(monto.doubleValue());
                        celda.setCellStyle(estiloSoles);
                    } else if (valor instanceof Number numero) {
                        celda.setCellValue(numero.doubleValue());
                    } else if (valor instanceof LocalDateTime fecha) {
                        celda.setCellValue(fecha.format(FECHA_HORA));
                    } else {
                        celda.setCellValue(valor == null ? "" : valor.toString());
                    }
                }
            }

            // Resumen
            XSSFCell resumen = hoja.createRow(numeroFila + 1).createCell(0);
            resumen.setCellValue(reporte.resumen());
            resumen.setCellStyle(estiloResumen);

            // Encabezado fijo al desplazarse y columnas con ancho automático
            hoja.createFreezePane(0, FILA_ENCABEZADO + 1);
            for (int i = 0; i < columnas; i++) {
                hoja.autoSizeColumn(i);
                hoja.setColumnWidth(i, Math.min(hoja.getColumnWidth(i) + 512, 60 * 256));
            }
            libro.write(salida);
        }
    }

    /** Color RGB para Excel. */
    private static XSSFColor color(int rojo, int verde, int azul) {
        return new XSSFColor(new byte[]{(byte) rojo, (byte) verde, (byte) azul}, null);
    }
}
