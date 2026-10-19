package com.gselectroCaqueta.tablas;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;

/**
 * Índice en memoria de los campos que se buscan en ModuloBusquedaCuenta (mismo diseño que el de la app
 * de lecturas EMSA, IndiceBusquedaRuta, adaptado a los planos de facturación EBSA).
 * <p>
 * Antes la búsqueda recorría MEDIDOR.TXT / CLIENTE.TXT con un seek + parseo de ~130 campos por registro
 * en el hilo principal, solo al presionar Listo y con tope de 6 resultados. Aquí los dos archivos se leen
 * UNA vez, de corrido y con buffer, conservando solo medidor, cuenta, nombre y dirección ya normalizados;
 * filtrar es un recorrido en memoria (~1 ms con miles de registros) y se puede hacer en cada tecla.
 * <p>
 * Una entrada por medidor (MEDIDOR.TXT), con el nombre y la dirección de su cliente (CLIENTE.TXT por la
 * posición que trae el medidor). El índice se cachea y se revalida por tamaño y fecha de los dos archivos.
 * Offsets tomados de TablaMedidorEntrada.lectura_ y TablaEntradaClientes.lectura_.
 */
public final class IndiceBusquedaClientes {

    private static final String TAG = "IndiceBusquedaClientes";

    // Mismos números que 'opcion' de ModuloBusquedaCuenta.
    public static final int CAMPO_MEDIDOR = 1;
    public static final int CAMPO_CUENTA = 2;
    public static final int CAMPO_DIRECCION = 4;
    public static final int CAMPO_NOMBRE = 5;

    // MEDIDOR.TXT (TablaMedidorEntrada)
    private static final int MED_OFF_CUENTA = 8, MED_LEN_CUENTA = 10;
    private static final int MED_OFF_NUMERO = 30, MED_LEN_NUMERO = 16;
    private static final int MED_OFF_PRIMER_REGISTRO = 75, MED_LEN_PRIMER_REGISTRO = 5;
    private static final int MED_OFF_CLIENTE = 81, MED_LEN_CLIENTE = 5;
    // CLIENTE.TXT (TablaEntradaClientes)
    private static final int CLI_OFF_NOMBRE = 80, CLI_LEN_NOMBRE = 48;
    private static final int CLI_OFF_DIRECCION = 129, CLI_LEN_DIRECCION = 60;

    private static final int BUFFER = 64 * 1024;

    private static IndiceBusquedaClientes cache;

    private final String firma;
    private final int total;

    private final String[] medidor, cuenta, nombre, direccion;
    private final int[] primerRegistro, cliente;
    private final String[] medidorN, cuentaN, nombreN, direccionN;

    // Reducción incremental: al agregar caracteres se filtra sobre las coincidencias previas.
    private String ultimaConsulta = "";
    private int ultimoCampo = 0;
    private int[] ultimasCoincidencias = new int[0];

    private IndiceBusquedaClientes(String firma, int total) {
        this.firma = firma;
        this.total = total;
        medidor = new String[total];
        cuenta = new String[total];
        nombre = new String[total];
        direccion = new String[total];
        primerRegistro = new int[total];
        cliente = new int[total];
        medidorN = new String[total];
        cuentaN = new String[total];
        nombreN = new String[total];
        direccionN = new String[total];
    }

    private static String firmaDe(File med, File cli) {
        return med.getAbsolutePath() + "|" + med.length() + "|" + med.lastModified()
                + "|" + cli.length() + "|" + cli.lastModified();
    }

    /**
     * Índice de los planos del directorio DATOSDEENTRADA indicado; reutiliza el cacheado si no cambiaron.
     * Es operación de disco: llamarla fuera del hilo principal.
     */
    public static synchronized IndiceBusquedaClientes obtener(String dirEntrada) throws IOException {
        File med = new File(dirEntrada, "MEDIDOR.TXT");
        File cli = new File(dirEntrada, "CLIENTE.TXT");
        if (!med.isFile()) throw new IOException("No existe " + med.getAbsolutePath());
        if (!cli.isFile()) throw new IOException("No existe " + cli.getAbsolutePath());
        String f = firmaDe(med, cli);
        if (cache != null && cache.firma.equals(f)) return cache;
        cache = construir(med, cli, f);
        return cache;
    }

    private static IndiceBusquedaClientes construir(File med, File cli, String firma) throws IOException {
        long inicio = System.currentTimeMillis();

        // 1) CLIENTE.TXT completo: nombre y dirección por posición (base 1).
        int lonCli = TablaEntradaClientes.LONGITUD_REGISTRO;
        int totalCli = (int) (cli.length() / lonCli);
        String[] nomCli = new String[totalCli + 1];
        String[] dirCli = new String[totalCli + 1];
        byte[] regCli = new byte[lonCli];
        try (InputStream in = new BufferedInputStream(new FileInputStream(cli), BUFFER)) {
            for (int i = 1; i <= totalCli; i++) {
                if (!leerCompleto(in, regCli)) break;
                nomCli[i] = campo(regCli, CLI_OFF_NOMBRE, CLI_LEN_NOMBRE);
                dirCli[i] = campo(regCli, CLI_OFF_DIRECCION, CLI_LEN_DIRECCION);
            }
        }

        // 2) MEDIDOR.TXT: una entrada por medidor.
        int lonMed = TablaMedidorEntrada.LONGITUD_REGISTRO;
        int totalMed = (int) (med.length() / lonMed);
        IndiceBusquedaClientes ix = new IndiceBusquedaClientes(firma, totalMed);
        byte[] regMed = new byte[lonMed];
        int n = 0;
        try (InputStream in = new BufferedInputStream(new FileInputStream(med), BUFFER)) {
            for (int i = 0; i < totalMed; i++) {
                if (!leerCompleto(in, regMed)) {
                    Log.w(TAG, "MEDIDOR.TXT truncado en el registro " + (i + 1));
                    break;
                }
                int pc = entero(campo(regMed, MED_OFF_CLIENTE, MED_LEN_CLIENTE));
                ix.medidor[n] = campo(regMed, MED_OFF_NUMERO, MED_LEN_NUMERO);
                ix.cuenta[n] = campo(regMed, MED_OFF_CUENTA, MED_LEN_CUENTA);
                ix.primerRegistro[n] = entero(campo(regMed, MED_OFF_PRIMER_REGISTRO, MED_LEN_PRIMER_REGISTRO));
                ix.cliente[n] = pc;
                ix.nombre[n] = pc >= 1 && pc <= totalCli && nomCli[pc] != null ? nomCli[pc] : "";
                ix.direccion[n] = pc >= 1 && pc <= totalCli && dirCli[pc] != null ? dirCli[pc] : "";
                ix.medidorN[n] = normalizar(ix.medidor[n]);
                ix.cuentaN[n] = normalizar(ix.cuenta[n]);
                ix.nombreN[n] = normalizar(ix.nombre[n]);
                ix.direccionN[n] = normalizar(ix.direccion[n]);
                n++;
            }
        }
        Log.i(TAG, "Indice: " + n + " medidores / " + totalCli + " clientes en "
                + (System.currentTimeMillis() - inicio) + " ms");
        return n == totalMed ? ix : ix.recortado(n);
    }

    private IndiceBusquedaClientes recortado(int n) {
        IndiceBusquedaClientes r = new IndiceBusquedaClientes(firma, n);
        System.arraycopy(medidor, 0, r.medidor, 0, n);
        System.arraycopy(cuenta, 0, r.cuenta, 0, n);
        System.arraycopy(nombre, 0, r.nombre, 0, n);
        System.arraycopy(direccion, 0, r.direccion, 0, n);
        System.arraycopy(primerRegistro, 0, r.primerRegistro, 0, n);
        System.arraycopy(cliente, 0, r.cliente, 0, n);
        System.arraycopy(medidorN, 0, r.medidorN, 0, n);
        System.arraycopy(cuentaN, 0, r.cuentaN, 0, n);
        System.arraycopy(nombreN, 0, r.nombreN, 0, n);
        System.arraycopy(direccionN, 0, r.direccionN, 0, n);
        return r;
    }

    private static boolean leerCompleto(InputStream in, byte[] destino) throws IOException {
        int leidos = 0;
        while (leidos < destino.length) {
            int k = in.read(destino, leidos, destino.length - leidos);
            if (k < 0) return false;
            leidos += k;
        }
        return true;
    }

    private static String campo(byte[] registro, int offset, int longitud) {
        return new String(registro, offset, longitud, StandardCharsets.UTF_8).trim();
    }

    private static int entero(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /** Mayúsculas, sin tildes y espacios simples: "MARIA" encuentra "MARÍA". */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String s = Normalizer.normalize(texto.trim().toUpperCase(), Normalizer.Form.NFD);
        return s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("\\s+", " ");
    }

    /**
     * Posiciones (base 0) que coinciden: lo digitado aparece en cualquier parte del campo ("4854" encuentra
     * "0174854005"). En medidor y cuenta salen primero los que empiezan por lo digitado. En dirección y
     * nombre se muestra un resultado por cliente (un cliente con varios medidores no se repite).
     */
    public synchronized int[] filtrar(String consulta, int campo, int maxResultados) {
        String buscado = normalizar(consulta);
        if (buscado.isEmpty()) {
            ultimaConsulta = "";
            ultimoCampo = campo;
            ultimasCoincidencias = new int[0];
            return ultimasCoincidencias;
        }
        String[] valores = valoresDe(campo);

        // Solo se agregaron caracteres: basta revisar las coincidencias anteriores.
        boolean incremental = campo == ultimoCampo && !ultimaConsulta.isEmpty() && buscado.startsWith(ultimaConsulta);
        int[] candidatos = incremental ? ultimasCoincidencias : null;
        int aRevisar = incremental ? candidatos.length : total;

        int[] encontrados = new int[aRevisar];
        int n = 0;
        for (int k = 0; k < aRevisar; k++) {
            int i = incremental ? candidatos[k] : k;
            if (valores[i].contains(buscado)) encontrados[n++] = i;
        }
        ultimaConsulta = buscado;
        ultimoCampo = campo;
        ultimasCoincidencias = Arrays.copyOf(encontrados, n); // siempre en orden de archivo

        int tope = Math.max(0, maxResultados);
        if (campo == CAMPO_MEDIDOR || campo == CAMPO_CUENTA) {
            return prefijosPrimero(ultimasCoincidencias, valores, buscado, tope);
        }
        return unoPorCliente(ultimasCoincidencias, tope);
    }

    private static int[] prefijosPrimero(int[] coincidencias, String[] valores, String buscado, int tope) {
        int[] salida = new int[Math.min(tope, coincidencias.length)];
        int n = 0;
        for (int i : coincidencias) {
            if (n == salida.length) return salida;
            if (valores[i].startsWith(buscado)) salida[n++] = i;
        }
        for (int i : coincidencias) {
            if (n == salida.length) break;
            if (!valores[i].startsWith(buscado)) salida[n++] = i;
        }
        return n == salida.length ? salida : Arrays.copyOf(salida, n);
    }

    private int[] unoPorCliente(int[] coincidencias, int tope) {
        int[] salida = new int[Math.min(tope, coincidencias.length)];
        java.util.HashSet<Integer> vistos = new java.util.HashSet<>();
        int n = 0;
        for (int i : coincidencias) {
            if (n == salida.length) break;
            if (vistos.add(cliente[i])) salida[n++] = i;
        }
        return n == salida.length ? salida : Arrays.copyOf(salida, n);
    }

    /** Coincidencias totales del último filtrar(), aunque se hayan devuelto menos. */
    public synchronized int totalCoincidencias() {
        return ultimasCoincidencias.length;
    }

    private String[] valoresDe(int campo) {
        switch (campo) {
            case CAMPO_MEDIDOR: return medidorN;
            case CAMPO_DIRECCION: return direccionN;
            case CAMPO_NOMBRE: return nombreN;
            case CAMPO_CUENTA:
            default: return cuentaN;
        }
    }

    public int total() { return total; }
    public String getMedidor(int i) { return medidor[i]; }
    public String getCuenta(int i) { return cuenta[i]; }
    public String getNombre(int i) { return nombre[i]; }
    public String getDireccion(int i) { return direccion[i]; }
    /** Primer registro del medidor (lo que devuelve la búsqueda por medidor). */
    public int getPrimerRegistro(int i) { return primerRegistro[i]; }
    /** Posición del cliente en CLIENTE.TXT (lo que devuelven cuenta, dirección y nombre). */
    public int getCliente(int i) { return cliente[i]; }
}
