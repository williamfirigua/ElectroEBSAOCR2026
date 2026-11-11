package com.lecturaia.lector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Junta las lecturas de varios cuadros de la cámara en vivo.
 *
 * Una lectura de un solo cuadro no basta: el cuadro puede salir movido, el rodillo estar a mitad de
 * cambio, o un LCD estar pasando de una pantalla a otra. Aquí un valor se CONFIRMA cuando sale igual
 * en {@code consecutivos} cuadros seguidos, con confianza media mínima. Los LCD que alternan pantallas
 * (activa, reactiva, fecha...) dejan varios valores confirmados, cada uno con cuántas veces se vio.
 *
 * No depende de Android: se prueba en el PC. No es seguro entre hilos: úselo desde un solo hilo
 * (el de análisis de la cámara) y lea {@link #valores()} como copia.
 */
public final class Estabilizador {
    private final int consecutivos;
    private final double confianzaMinima;
    private final int maxValores;

    private String candidato;            // lectura|decimales del último cuadro con lectura
    private int racha;
    private double sumaConfianzaRacha;
    private final Map<String, Valor> confirmados = new LinkedHashMap<>();
    private Valor ultimo;
    private int cuadros;

    /** 3 cuadros seguidos (≈ medio segundo a 6 cuadros/s), confianza media ≥ 0,5, hasta 8 valores distintos. */
    public Estabilizador() {
        this(3, 0.5, 8);
    }

    public Estabilizador(int consecutivos, double confianzaMinima, int maxValores) {
        if (consecutivos < 1) throw new IllegalArgumentException("consecutivos >= 1");
        this.consecutivos = consecutivos;
        this.confianzaMinima = confianzaMinima;
        this.maxValores = maxValores;
    }

    /**
     * Agrega el resultado de un cuadro.
     *
     * @return el valor confirmado con este cuadro (nuevo o que se sigue viendo), o null.
     */
    public Valor agregar(Resultado r, long ahoraMs) {
        cuadros++;
        if (r == null || !r.tieneLectura() || r.lectura.indexOf('?') >= 0) {
            candidato = null;
            racha = 0;
            return null;
        }
        String clave = r.lectura + "|" + r.decimales;
        if (clave.equals(candidato)) {
            racha++;
            sumaConfianzaRacha += r.confianza;
        } else {
            candidato = clave;
            racha = 1;
            sumaConfianzaRacha = r.confianza;
        }
        if (racha < consecutivos || sumaConfianzaRacha / racha < confianzaMinima) return null;

        Valor v = confirmados.get(clave);
        if (v == null) {
            if (confirmados.size() >= maxValores) quitarMenosVisto();
            v = new Valor(r.lectura, r.decimales, r.tipo, ahoraMs);
            confirmados.put(clave, v);
        }
        v.veces++;
        v.confianzaMax = Math.max(v.confianzaMax, r.confianza);
        v.ultimaVez = ahoraMs;
        ultimo = v;
        return v;
    }

    /** El último valor confirmado (lo que se muestra en grande), o null. */
    public Valor actual() {
        return ultimo;
    }

    /** Valores confirmados, del más visto al menos visto (copia). */
    public List<Valor> valores() {
        List<Valor> l = new ArrayList<>();
        for (Valor v : confirmados.values()) l.add(v.copia());
        Collections.sort(l, new Comparator<Valor>() {
            @Override
            public int compare(Valor a, Valor b) {
                return a.veces != b.veces ? Integer.compare(b.veces, a.veces) : Long.compare(a.primeraVez, b.primeraVez);
            }
        });
        return l;
    }

    public int cuadros() {
        return cuadros;
    }

    public void limpiar() {
        confirmados.clear();
        candidato = null;
        racha = 0;
        ultimo = null;
        cuadros = 0;
    }

    private void quitarMenosVisto() {
        String peor = null;
        int min = Integer.MAX_VALUE;
        for (Map.Entry<String, Valor> e : confirmados.entrySet()) {
            if (e.getValue().veces < min) {
                min = e.getValue().veces;
                peor = e.getKey();
            }
        }
        if (peor != null) confirmados.remove(peor);
    }

    /** Un valor confirmado. {@code veces} = cuadros en que se volvió a confirmar. */
    public static final class Valor {
        public final String lectura;
        public final int decimales;
        public final String tipo;
        public final long primeraVez;
        public int veces;
        public double confianzaMax;
        public long ultimaVez;

        Valor(String lectura, int decimales, String tipo, long primeraVez) {
            this.lectura = lectura;
            this.decimales = decimales;
            this.tipo = tipo;
            this.primeraVez = primeraVez;
            this.ultimaVez = primeraVez;
        }

        Valor copia() {
            Valor c = new Valor(lectura, decimales, tipo, primeraVez);
            c.veces = veces;
            c.confianzaMax = confianzaMax;
            c.ultimaVez = ultimaVez;
            return c;
        }

        /** Con coma antes de los decimales: 10463,5 */
        public String conComa() {
            return decimales > 0 ? lectura.substring(0, lectura.length() - decimales) + "," + lectura.substring(lectura.length() - decimales)
                    : lectura;
        }

        public String entera() {
            return decimales > 0 ? lectura.substring(0, lectura.length() - decimales) : lectura;
        }
    }
}
