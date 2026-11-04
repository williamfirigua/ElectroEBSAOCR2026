package com.gselectroCaqueta.accesoyseguridad;

import com.gselectroCaqueta.sync.Json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bandeja de mensajes de la oficina en la terminal. Los baja el servicio de sincronizacion (GET /mensajes) y
 * los lee la pantalla de liquidacion. Se guarda en un archivo JSON propio de la app (escritura atomica: archivo
 * temporal y renombrar), con los ultimos {@link #MAXIMO} mensajes.
 * <p>
 * Al leerlos, {@link #marcarLeidos()} devuelve los ids que pasaron a leidos: la pantalla los manda como evento
 * mensaje_leido y la oficina ve la hora de lectura.
 */
public final class BandejaMensajes {

    public static final int MAXIMO = 200;
    private static final Object LOCK = new Object();
    private static volatile Runnable oyente;

    public static final class Mensaje {
        public final long id;
        public final String remitente, nombre, asunto, texto, enviado;
        public boolean leido;

        Mensaje(long id, String remitente, String nombre, String asunto, String texto, String enviado, boolean leido) {
            this.id = id;
            this.remitente = remitente;
            this.nombre = nombre;
            this.asunto = asunto;
            this.texto = texto;
            this.enviado = enviado;
            this.leido = leido;
        }
    }

    private final File archivo;

    public BandejaMensajes(File directorio) {
        this.archivo = new File(directorio, "bandeja_mensajes.json");
    }

    /** La pantalla en primer plano se registra para refrescar el contador cuando llegan mensajes; null al salir. */
    public static void oyente(Runnable r) {
        oyente = r;
    }

    public long ultimoId() {
        long u = 0;
        for (Mensaje m : todos()) u = Math.max(u, m.id);
        return u;
    }

    public int noLeidos() {
        int n = 0;
        for (Mensaje m : todos()) if (!m.leido) n++;
        return n;
    }

    /** Mas reciente primero. */
    public List<Mensaje> todos() {
        synchronized (LOCK) {
            return leer();
        }
    }

    /** Agrega los que no estaban (por id). Devuelve cuantos agrego y avisa a la pantalla. */
    public int agregar(List<Map<String, Object>> nuevos) {
        int agregados = 0;
        synchronized (LOCK) {
            List<Mensaje> lista = leer();
            for (Map<String, Object> m : nuevos) {
                long id;
                try {
                    id = Long.parseLong(Json.texto(m, "id"));
                } catch (NumberFormatException e) {
                    continue;
                }
                boolean existe = false;
                for (Mensaje x : lista) if (x.id == id) existe = true;
                if (existe) continue;
                lista.add(new Mensaje(id, t(Json.texto(m, "remitente")), t(Json.texto(m, "nombre")), t(Json.texto(m, "asunto")),
                        t(Json.texto(m, "texto")), t(Json.texto(m, "enviado")), "true".equals(Json.texto(m, "leido"))));
                agregados++;
            }
            if (agregados > 0) guardar(lista);
        }
        Runnable r = oyente;
        if (agregados > 0 && r != null) r.run();
        return agregados;
    }

    /** Marca todos como leidos; devuelve los ids que no lo estaban. */
    public List<Long> marcarLeidos() {
        List<Long> ids = new ArrayList<>();
        synchronized (LOCK) {
            List<Mensaje> lista = leer();
            for (Mensaje m : lista) {
                if (!m.leido) {
                    m.leido = true;
                    ids.add(m.id);
                }
            }
            if (!ids.isEmpty()) guardar(lista);
        }
        return ids;
    }

    /** Otro tecnico ingreso en la terminal: lo del anterior no se le muestra. */
    public void vaciar() {
        synchronized (LOCK) {
            archivo.delete();
        }
        Runnable r = oyente;
        if (r != null) r.run();
    }

    // ------------------------------------------------------------------ archivo

    private List<Mensaje> leer() {
        List<Mensaje> r = new ArrayList<>();
        if (!archivo.isFile()) return r;
        try (InputStream in = new FileInputStream(archivo)) {
            byte[] b = new byte[(int) archivo.length()];
            int n = 0;
            while (n < b.length) {
                int k = in.read(b, n, b.length - n);
                if (k < 0) break;
                n += k;
            }
            for (Object o : Json.lista(Json.objeto(new String(b, 0, n, StandardCharsets.UTF_8)), "mensajes")) {
                if (!(o instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>) o;
                try {
                    r.add(new Mensaje(Long.parseLong(Json.texto(m, "id")), t(Json.texto(m, "remitente")), t(Json.texto(m, "nombre")),
                            t(Json.texto(m, "asunto")), t(Json.texto(m, "texto")), t(Json.texto(m, "enviado")),
                            "true".equals(Json.texto(m, "leido"))));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException | RuntimeException e) {
            return r; // archivo danado: se empieza de nuevo (el servidor reenvia desde el ultimo id que quede)
        }
        r.sort((a, b) -> Long.compare(b.id, a.id));
        return r;
    }

    private void guardar(List<Mensaje> lista) {
        lista.sort((a, b) -> Long.compare(b.id, a.id));
        List<Object> salida = new ArrayList<>();
        for (int i = 0; i < lista.size() && i < MAXIMO; i++) {
            Mensaje m = lista.get(i);
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", m.id);
            o.put("remitente", m.remitente);
            o.put("nombre", m.nombre);
            o.put("asunto", m.asunto);
            o.put("texto", m.texto);
            o.put("enviado", m.enviado);
            o.put("leido", m.leido);
            salida.add(o);
        }
        Map<String, Object> raiz = new LinkedHashMap<>();
        raiz.put("mensajes", salida);
        File tmp = new File(archivo.getPath() + ".tmp");
        try (OutputStream out = new FileOutputStream(tmp)) {
            out.write(Json.escribir(raiz).getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            return;
        }
        if (!tmp.renameTo(archivo)) {
            archivo.delete();
            tmp.renameTo(archivo);
        }
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }
}
