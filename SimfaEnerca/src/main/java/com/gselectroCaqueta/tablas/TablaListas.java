package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * LISTA.TXT (DatosSoporte): la lista de dominios de EBSA completa, tal como llega en lm_lista.
 * <pre>
 *   dominio(20)|codigo(8)|descripcion(250)|dato1(1)|dato2(3)|dato3(1)|concepto(6)|prioridad(6)|procesador(6)|
 * </pre>
 * Se consulta por dominio + código ({@code MDLDDCBRO/N} → "Redes Aereas EBSA 21"). Son ~800 registros (~250 KB):
 * se cargan una vez en memoria y se reutilizan mientras el archivo no cambie (misma ruta, fecha y tamaño), así
 * que cada factura no vuelve a leer el disco. Si el archivo no está, la lista queda vacía y quien la usa decide
 * el respaldo (la factura imprime el código o el marcador de faltante).
 * <p>
 * Se lee por separadores y no por posición, así que un cambio de ancho en el servidor no la corre.
 */
public final class TablaListas {

    public static final String ARCHIVO = "LISTA.TXT";

    /** Un registro de la lista. Los campos vienen recortados. */
    public static final class Item {
        public final String dominio, codigo, descripcion, dato1, dato2, dato3, concepto, prioridad, procesador;

        Item(String[] c) {
            dominio = campo(c, 0);
            codigo = campo(c, 1);
            descripcion = campo(c, 2);
            dato1 = campo(c, 3);
            dato2 = campo(c, 4);
            dato3 = campo(c, 5);
            concepto = campo(c, 6);
            prioridad = campo(c, 7);
            procesador = campo(c, 8);
        }

        private static String campo(String[] c, int i) {
            return i < c.length ? c[i].trim() : "";
        }
    }

    private static final TablaListas VACIA = new TablaListas("", 0, 0, Collections.emptyMap());
    private static volatile TablaListas cache = VACIA;

    private final String ruta;
    private final long modificado;
    private final long tamano;
    private final Map<String, Item> items;

    private TablaListas(String ruta, long modificado, long tamano, Map<String, Item> items) {
        this.ruta = ruta;
        this.modificado = modificado;
        this.tamano = tamano;
        this.items = items;
    }

    /** La lista del archivo indicado; vacía si no existe o no se puede leer. */
    public static TablaListas de(File archivo) {
        if (archivo == null || !archivo.isFile()) return VACIA;
        TablaListas actual = cache;
        String ruta = archivo.getAbsolutePath();
        if (actual.ruta.equals(ruta) && actual.modificado == archivo.lastModified() && actual.tamano == archivo.length()) {
            return actual;
        }
        synchronized (TablaListas.class) {
            actual = cache;
            if (actual.ruta.equals(ruta) && actual.modificado == archivo.lastModified() && actual.tamano == archivo.length()) {
                return actual;
            }
            TablaListas leida = leer(archivo);
            cache = leida;
            return leida;
        }
    }

    private static TablaListas leer(File archivo) {
        long modificado = archivo.lastModified();
        long tamano = archivo.length();
        Map<String, Item> items = new HashMap<>(1024);
        // ISO-8859-1: el servidor escribe sin tildes; así cualquier byte se lee sin fallar.
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.ISO_8859_1))) {
            String linea;
            while ((linea = r.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                Item it = new Item(linea.split("\\|", -1));
                if (it.dominio.isEmpty() || it.codigo.isEmpty()) continue;
                items.put(clave(it.dominio, it.codigo), it);
            }
        } catch (IOException e) {
            return VACIA;
        }
        return new TablaListas(archivo.getAbsolutePath(), modificado, tamano, Collections.unmodifiableMap(items));
    }

    private static String clave(String dominio, String codigo) {
        return dominio.trim().toUpperCase(Locale.ROOT) + '\u0000' + codigo.trim().toUpperCase(Locale.ROOT);
    }

    /** El registro, o null si el dominio o el código no están. */
    public Item buscar(String dominio, String codigo) {
        if (dominio == null || codigo == null || codigo.trim().isEmpty()) return null;
        return items.get(clave(dominio, codigo));
    }

    /** Descripción del código en el dominio, o "" si no está. */
    public String descripcion(String dominio, String codigo) {
        Item it = buscar(dominio, codigo);
        return it == null ? "" : it.descripcion;
    }

    public int tamano() {
        return items.size();
    }
}
