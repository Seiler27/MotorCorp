/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

/**
 *
 * Controla los intentos fallidos de inicio de sesion,
 * 3 fallos y el usuario sera bloqueado por 5 minutos
 */
public class ControlIntentos {
    
    public static final int MAX_INTENTOS = 3;
    
    private final Cache<String, Integer> fallos = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    
    public void registrarFallo(String usuario){
        Integer actual = fallos.getIfPresent(usuario);
        fallos.put(usuario, actual == null ? 1 : actual + 1);
    }
    
    public boolean estaBloqueado(String usuario){
        return intentosRestantes(usuario) == 0;
    }
    
    public int intentosRestantes(String usuario){
        Integer actual = fallos.getIfPresent(usuario);
        int usados = actual == null ? 0 : actual;
        return Math.max(MAX_INTENTOS - usados, 0);
    }
    
    public void reiniciar(String usuario){
        fallos.invalidate(usuario);
    }
}
