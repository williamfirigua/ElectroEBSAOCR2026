package com.gselectroCaqueta.accesoyseguridad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Valida un supervisor contra LECTOR.TXT (DATOSDEENTRADA), el mismo archivo del ingreso a la app:
 * <pre>
 *   codigo(10)|clave(12)|nombre(30)|ind(1)|movil(15)|version(15)|
 *   '       122|        1122|JOSE CHAVEZ TORREZ            |S|              0|              0|'
 * </pre>
 * Solo cuenta indicador 'S'. Código y clave se comparan como números (así lo hace el ingreso:
 * "0122" y "122" son el mismo código). Sin estado; lee el archivo en cada intento (es corto y se usa
 * solo cuando hay que desbloquear).
 */
public final class ClaveSupervisor {

    public static final class Supervisor {
        public final String codigo;
        public final String nombre;

        Supervisor(String codigo, String nombre) {
            this.codigo = codigo;
            this.nombre = nombre;
        }
    }

    private ClaveSupervisor() {
    }

    /** @return el supervisor si código y clave coinciden con una fila 'S'; null si no (o si falta el archivo). */
    public static Supervisor validar(File lectorTxt, String codigo, String clave) {
        Long cod = numero(codigo);
        Long cla = numero(clave);
        if (cod == null || cla == null || lectorTxt == null || !lectorTxt.isFile()) return null;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(lectorTxt), StandardCharsets.ISO_8859_1))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                String[] c = linea.split("\\|", -1);
                if (c.length < 4 || !"S".equalsIgnoreCase(c[3].trim())) continue;
                if (cod.equals(numero(c[0])) && cla.equals(numero(c[1]))) {
                    return new Supervisor(c[0].trim(), c[2].trim());
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private static Long numero(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || t.length() > 18) return null;
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) return null;
        }
        return Long.parseLong(t);
    }
}
