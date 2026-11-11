package com.lecturaia.lector;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Lo que el modelo concluye de una foto. Mismo contenido que inferencia.Resultado en Python. */
public final class Resultado {
    public final String tipo;              // rodillos, agujas, lcd, led, no_medidor, ilegible
    public final double confianzaTipo;
    public final double[][] esquinas;      // 4 × (x, y) normalizadas 0..1 en orden de lectura; null si no aplica
    public final String lectura;           // todos los dígitos, decimales incluidos ("" si no leyó)
    public final int decimales;
    public final String entera;            // lo que se compara con la lectura del técnico
    public final double confianza;         // confianza de la lectura completa (producto por dígito)
    public final List<Decodificador.Digito> digitos;
    public final String version;

    Resultado(String tipo, double confianzaTipo, double[][] esquinas, String lectura, int decimales,
              double confianza, List<Decodificador.Digito> digitos, String version) {
        this.tipo = tipo;
        this.confianzaTipo = confianzaTipo;
        this.esquinas = esquinas;
        this.lectura = lectura;
        this.decimales = decimales;
        this.entera = decimales > 0 ? lectura.substring(0, lectura.length() - decimales) : lectura;
        this.confianza = confianza;
        this.digitos = digitos == null ? Collections.<Decodificador.Digito>emptyList() : digitos;
        this.version = version;
    }

    public boolean esMedidor() {
        return !"no_medidor".equals(tipo);
    }

    public boolean tieneLectura() {
        return !entera.isEmpty();
    }

    /** JSON compacto, para guardar o enviar (sin depender de librerías). */
    public String aJson() {
        StringBuilder s = new StringBuilder(160);
        s.append("{\"tipo\":\"").append(tipo).append("\",\"confianza_tipo\":").append(num(confianzaTipo))
         .append(",\"lectura\":\"").append(lectura).append("\",\"decimales\":").append(decimales)
         .append(",\"entera\":\"").append(entera).append("\",\"confianza\":").append(num(confianza))
         .append(",\"version\":\"").append(version.replace("\"", "")).append("\",\"esquinas\":");
        if (esquinas == null) {
            s.append("null");
        } else {
            s.append('[');
            for (int i = 0; i < 4; i++) {
                if (i > 0) s.append(',');
                s.append('[').append(num(esquinas[i][0])).append(',').append(num(esquinas[i][1])).append(']');
            }
            s.append(']');
        }
        return s.append('}').toString();
    }

    private static String num(double v) {
        return String.format(Locale.ROOT, "%.4f", v);
    }
}
