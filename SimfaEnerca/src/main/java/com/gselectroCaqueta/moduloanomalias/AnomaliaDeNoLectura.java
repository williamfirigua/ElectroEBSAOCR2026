package com.gselectroCaqueta.moduloanomalias;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/// <summary>
/// Descripcion breve del archivo a generar...anomaliaDeNoLectura
/// </summary>

public class AnomaliaDeNoLectura {
    static final int LONGITUD_REGISTRO = 53;//Ax ya
    String sep = ";";
    String anomaliaDeNoLectura_CODIGO;
    String anomaliaDeNoLectura_DESCRIPCION;
    String anomaliaDeNoLectura_TIPO;
    String anomaliaDeNoLectura_SEIMPRIMEENFACTURA;
    String anomaliaDeNoLectura_FACTURAPROMEDIO;
    String anomaliaDeNoLectura_PERMITELECTURA;
    String anomaliaDeNoLectura_CRNL;
    BufferedReader fin;
    byte[] byteArray;
    int ultimo_AnomaliaDeNoLectura;
    String buscar_AnomaliaDeNoLectura;
    String texto;
    RandomAccessFile rFile;
    private String archivo_AnomaliaDeNoLectura;
    private int total_AnomaliaDeNoLectura;
    private int encontro_AnomaliaDeNoLectura;

    public String getanomaliaDeNoLectura_CODIGO() {
        return anomaliaDeNoLectura_CODIGO;
    }

    public void setanomaliaDeNoLectura_CODIGO(String anomaliaDeNoLectura_CODIGO) {
        this.anomaliaDeNoLectura_CODIGO = anomaliaDeNoLectura_CODIGO;
    }

    public String getanomaliaDeNoLectura_DESCRIPCION() {
        return anomaliaDeNoLectura_DESCRIPCION;
    }

    public void setanomaliaDeNoLectura_DESCRIPCION(String anomaliaDeNoLectura_DESCRIPCION) {
        this.anomaliaDeNoLectura_DESCRIPCION = anomaliaDeNoLectura_DESCRIPCION;
    }

    public String getanomaliaDeNoLectura_TIPO() {
        return anomaliaDeNoLectura_TIPO;
    }

    public void setanomaliaDeNoLectura_TIPO(String anomaliaDeNoLectura_TIPO) {
        this.anomaliaDeNoLectura_TIPO = anomaliaDeNoLectura_TIPO;
    }

    public String getanomaliaDeNoLectura_SEIMPRIMEENFACTURA() {
        return anomaliaDeNoLectura_SEIMPRIMEENFACTURA;
    }

    public void setanomaliaDeNoLectura_SEIMPRIMEENFACTURA(String anomaliaDeNoLectura_SEIMPRIMEENFACTURA) {
        this.anomaliaDeNoLectura_SEIMPRIMEENFACTURA = anomaliaDeNoLectura_SEIMPRIMEENFACTURA;
    }

    public String getanomaliaDeNoLectura_FACTURAPROMEDIO() {
        return anomaliaDeNoLectura_FACTURAPROMEDIO;
    }

    public void setanomaliaDeNoLectura_FACTURAPROMEDIO(String anomaliaDeNoLectura_FACTURAPROMEDIO) {
        this.anomaliaDeNoLectura_FACTURAPROMEDIO = anomaliaDeNoLectura_FACTURAPROMEDIO;
    }

    public String getanomaliaDeNoLectura_PERMITELECTURA() {
        return anomaliaDeNoLectura_PERMITELECTURA;
    }

    public void setanomaliaDeNoLectura_PERMITELECTURA(String anomaliaDeNoLectura_PERMITELECTURA) {
        this.anomaliaDeNoLectura_PERMITELECTURA = anomaliaDeNoLectura_PERMITELECTURA;
    }

    public String getanomaliaDeNoLectura_CRNL() {
        return anomaliaDeNoLectura_CRNL;
    }

    public void setanomaliaDeNoLectura_CRNL(String anomaliaDeNoLectura_CRNL) {
        this.anomaliaDeNoLectura_CRNL = anomaliaDeNoLectura_CRNL;
    }

    public Boolean abrir_AnomaliaDeNoLectura(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_AnomaliaDeNoLectura(nombreArchivo);
        return abrir(getArchivo_AnomaliaDeNoLectura());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            setTotal_AnomaliaDeNoLectura(fileSize / LONGITUD_REGISTRO);
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_AnomaliaDeNoLectura() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_AnomaliaDeNoLectura(int posicion) {
        posicion_AnomaliaDeNoLectura(posicion, LONGITUD_REGISTRO);
        rellenar_AnomaliaDeNoLectura();
        String texto = anomaliaDeNoLectura_CODIGO + sep + anomaliaDeNoLectura_DESCRIPCION + sep + anomaliaDeNoLectura_TIPO + sep + anomaliaDeNoLectura_SEIMPRIMEENFACTURA + sep
                + anomaliaDeNoLectura_FACTURAPROMEDIO + sep + anomaliaDeNoLectura_PERMITELECTURA + sep + anomaliaDeNoLectura_CRNL;
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamano texto: " + texto.length() + texto);
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_AnomaliaDeNoLectura() {
        try {
            anomaliaDeNoLectura_CODIGO = String.format("%-3s", anomaliaDeNoLectura_CODIGO);
            anomaliaDeNoLectura_DESCRIPCION = String.format("%-38s", anomaliaDeNoLectura_DESCRIPCION);
            anomaliaDeNoLectura_TIPO = String.format("%-1s", anomaliaDeNoLectura_TIPO);
            anomaliaDeNoLectura_SEIMPRIMEENFACTURA = String.format("%-1s", anomaliaDeNoLectura_SEIMPRIMEENFACTURA);
            anomaliaDeNoLectura_FACTURAPROMEDIO = String.format("%-1s", anomaliaDeNoLectura_FACTURAPROMEDIO);
            anomaliaDeNoLectura_PERMITELECTURA = String.format("%-1s", anomaliaDeNoLectura_PERMITELECTURA);
            anomaliaDeNoLectura_CRNL = String.format("%-2s", anomaliaDeNoLectura_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo anomaliaDeNoLectura.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_AnomaliaDeNoLectura(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_AnomaliaDeNoLectura(int registro) {
        String[] campos;
        setEncontro_AnomaliaDeNoLectura(0);
        posicion_AnomaliaDeNoLectura(registro, LONGITUD_REGISTRO);
        try {
            if (byteArray == null || byteArray.length < LONGITUD_REGISTRO) byteArray = new byte[LONGITUD_REGISTRO];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray, 0, LONGITUD_REGISTRO);
            setanomaliaDeNoLectura_CODIGO(texto.substring(0, 3));
            setanomaliaDeNoLectura_DESCRIPCION(texto.substring(4, 42));
            setanomaliaDeNoLectura_TIPO(texto.substring(43, 44));
            setanomaliaDeNoLectura_SEIMPRIMEENFACTURA(texto.substring(45, 46));
            setanomaliaDeNoLectura_FACTURAPROMEDIO(texto.substring(47, 48));
            setanomaliaDeNoLectura_PERMITELECTURA(texto.substring(49, 50));
            setanomaliaDeNoLectura_CRNL(texto.substring(51, 53));
            ultimo_AnomaliaDeNoLectura = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** "25" -> "025" con longitud 3. Si no es numérico o ya tiene la longitud, lo deja igual. */
    public static String rellenarCeros(String valor, int longitud) {
        if (valor == null) return "";
        String v = valor.trim();
        if (v.length() == 0 || v.length() >= longitud) return v;
        for (int i = 0; i < v.length(); i++) {
            if (!Character.isDigit(v.charAt(i))) return v;
        }
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = v.length(); i < longitud; i++) sb.append('0');
        return sb.append(v).toString();
    }
    /**
     * Codigo normalizado para comparar: sin espacios y sin ceros a la izquierda ("  5", "005" y "5" -> "5").
     * CAUSA_NL.TXT se genera con relleno de espacios y algunos llamadores rellenaban con ceros; antes se
     * comparaban como texto y "005" no encontraba "  5".
     */
    public static String normalizarCodigo(String codigo) {
        String t = codigo == null ? "" : codigo.trim();
        int i = 0;
        while (i < t.length() - 1 && t.charAt(i) == '0') i++;
        return t.substring(i).toUpperCase();
    }

    /**
     * Busca la causa por codigo comparando normalizado (ver normalizarCodigo). El catalogo tiene pocas decenas
     * de registros, asi que se recorre completo: no depende de que el archivo venga ordenado ni del relleno.
     * Si la encuentra deja cargados sus campos, posiciona el archivo en ese registro y encontro = posicion (>= 1);
     * si no, encontro = 0.
     */
    public void buscarbinario_AnomaliaDeNoLectura(String codigo) {
        String buscado = normalizarCodigo(codigo);
        int total = getTotal_AnomaliaDeNoLectura();
        if (buscado.isEmpty() || total <= 0) {
            setEncontro_AnomaliaDeNoLectura(0);
            return;
        }
        for (int r = 1; r <= total; r++) {
            lectura_AnomaliaDeNoLectura(r);
            if (normalizarCodigo(anomaliaDeNoLectura_CODIGO).equals(buscado)) {
                posicion_AnomaliaDeNoLectura(r, LONGITUD_REGISTRO);
                setEncontro_AnomaliaDeNoLectura(r);
                return;
            }
        }
        setEncontro_AnomaliaDeNoLectura(0);
    }

    /** Misma busqueda que buscarbinario_AnomaliaDeNoLectura (antes convertia a double y fallaba con codigos no numericos). */
    public void Buscarbinariocausanl(String codigo) {
        buscarbinario_AnomaliaDeNoLectura(codigo);
    }

    public String getArchivo_AnomaliaDeNoLectura() {
        return archivo_AnomaliaDeNoLectura;
    }

    public void setArchivo_AnomaliaDeNoLectura(String archivo_AnomaliaDeNoLectura) {
        this.archivo_AnomaliaDeNoLectura = archivo_AnomaliaDeNoLectura;
    }

    public int getTotal_AnomaliaDeNoLectura() {
        return total_AnomaliaDeNoLectura;
    }

    public void setTotal_AnomaliaDeNoLectura(int total_AnomaliaDeNoLectura) {
        this.total_AnomaliaDeNoLectura = total_AnomaliaDeNoLectura;
    }

    public int getEncontro_AnomaliaDeNoLectura() {
        return encontro_AnomaliaDeNoLectura;
    }

    public void setEncontro_AnomaliaDeNoLectura(int encontro_AnomaliaDeNoLectura) {
        this.encontro_AnomaliaDeNoLectura = encontro_AnomaliaDeNoLectura;
    }
}
