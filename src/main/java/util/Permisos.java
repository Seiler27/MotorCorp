/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * Indica que modulo usa cada rol
 */
public final class Permisos {
    
    
    private static final Map<String, Set<Modulo>> PERMISOS = Map.of(
            "Administrador", EnumSet.allOf(Modulo.class),
            "Recepcion", EnumSet.of(Modulo.INICIO, Modulo.CLIENTES, Modulo.COLA,
                    Modulo.INVENTARIO, Modulo.HISTORIAL, Modulo.POS),
            "Mecanico", EnumSet.of(Modulo.INICIO, Modulo.COLA,
                    Modulo.INVENTARIO, Modulo.HISTORIAL)
    );
    
    public static boolean puedeAcceder(String rol, Modulo modulo){
        if (rol == null || modulo == null) {
            return false;
        }
        return PERMISOS.getOrDefault(rol, Set.of()).contains(modulo);
    }
    
}
