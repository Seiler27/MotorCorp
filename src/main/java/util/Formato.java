/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author eda
 */
public final class Formato {
    
    private Formato(){
    }
    
    public static String nombre(String texto){
    if(StringUtils.isBlank(texto)){
        return"";
    }
    String[] palabras = StringUtils.normalizeSpace(texto).toLowerCase().split(" ");
    for (int i = 0; i< palabras.length; i++){
        palabras[i] = StringUtils.capitalize(palabras[i]);
    }
    return String.join(" ", palabras);
    }
    
    public static String placa(String texto){
        if (texto == null) {
            return "";
        }
        return StringUtils.deleteWhitespace(texto).toUpperCase();
    }
    
    public static String soles (BigDecimal monto){
        if (monto == null) {
            monto = BigDecimal.ZERO;
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "S/ " + formato.format(monto);
    }
}
