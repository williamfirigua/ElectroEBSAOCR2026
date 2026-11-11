package com.lecturaia.lector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lee un campo de primer nivel (texto o número) de un JSON pequeño y confiable (modelo.json, la
 * respuesta del servidor), sin depender de librerías. No es un lector JSON general.
 */
public final class JsonPlano {
    private JsonPlano() {
    }

    public static String texto(String json, String campo) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        return m.find() ? m.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : null;
    }

    public static Double numero(String json, String campo) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(campo) + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?)").matcher(json);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    /** Texto como valor JSON entre comillas (con escapes), o null. */
    public static String comillas(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char ch : s.toCharArray()) {
            if (ch == '"' || ch == '\\') b.append('\\').append(ch);
            else if (ch < 0x20) b.append(String.format("\\u%04x", (int) ch));
            else b.append(ch);
        }
        return b.append('"').toString();
    }
}
