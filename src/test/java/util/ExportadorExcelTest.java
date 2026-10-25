package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import modelo.Reporte;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la exportación de reportes a Excel con Apache POI.
 */
class ExportadorExcelTest {

    private Reporte reporteDeEjemplo() {
        return new Reporte("Reporte de ventas", List.of("N°", "Fecha", "Cliente", "Total"),
                List.of(new Object[]{1, LocalDateTime.of(2026, 10, 21, 10, 30), "Juan Perez Rojas", new BigDecimal("46.50")},
                        new Object[]{2, LocalDateTime.of(2026, 10, 24, 16, 0), "Mario Martinez Soto", new BigDecimal("120.00")}),
                "2 comprobante(s)");
    }

    @Test
    void elExcelTieneTituloEncabezadosYDatos() throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ExportadorExcel.exportar(reporteDeEjemplo(), "Del 01/10/2026 al 31/10/2026", salida);

        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(salida.toByteArray()))) {
            XSSFSheet hoja = libro.getSheetAt(0);
            assertEquals("MotorCorp - Reporte de ventas", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Cliente", hoja.getRow(ExportadorExcel.FILA_ENCABEZADO).getCell(2).getStringCellValue());
            assertEquals("Juan Perez Rojas", hoja.getRow(ExportadorExcel.FILA_ENCABEZADO + 1).getCell(2).getStringCellValue());
        }
    }

    @Test
    void losMontosSeGuardanComoNumerosParaPoderSumarlosEnExcel() throws Exception {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ExportadorExcel.exportar(reporteDeEjemplo(), "Periodo", salida);

        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(salida.toByteArray()))) {
            double total = libro.getSheetAt(0).getRow(ExportadorExcel.FILA_ENCABEZADO + 2).getCell(3).getNumericCellValue();
            assertEquals(120.0, total, 0.001);
        }
    }
}
