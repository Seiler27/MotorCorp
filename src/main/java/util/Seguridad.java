/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.security.SecureRandom;
import java.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * Cifra y verifica la contraseña
 */
public final class Seguridad {
    private Seguridad(){
    }
    
    public static String cifrar(String password) {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        String sal = Base64.getEncoder().encodeToString(bytes);
        return sal + ":"+ DigestUtils.sha256Hex(sal + password);
    }
    
    public static boolean verificar(String password, String guardado){
                if (password == null || guardado == null || !guardado.contains(":")) {
            return false;
        }
        String[] partes = guardado.split(":", 2);
        String sal = partes[0];
        String hash = partes[1];
        return DigestUtils.sha256Hex(sal + password).equals(hash);
    }
}
