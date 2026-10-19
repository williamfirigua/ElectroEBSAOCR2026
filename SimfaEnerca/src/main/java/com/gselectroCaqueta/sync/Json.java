package com.gselectroCaqueta.sync;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON mínimo para la API móvil: escribir objetos/listas de texto y números y leer las respuestas.
 * Sin dependencias (corre igual en Android y en las pruebas de JVM). Objetos = LinkedHashMap,
 * listas = ArrayList, números = Double o Long, true/false = Boolean, null = null.
 */
public final class Json {

    private Json() {
    }

    // ------------------------------------------------------------------ escritura

    public static String escribir(Object valor) {
        StringBuilder sb = new StringBuilder(256);
        escribir(sb, valor);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void escribir(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            cadena(sb, (String) v);
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v.toString());
        } else if (v instanceof Crudo) {
            sb.append(((Crudo) v).json);
        } else if (v instanceof Map) {
            sb.append('{');
            Iterator<Map.Entry<String, Object>> it = ((Map<String, Object>) v).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> e = it.next();
                cadena(sb, e.getKey());
                sb.append(':');
                escribir(sb, e.getValue());
                if (it.hasNext()) sb.append(',');
            }
            sb.append('}');
        } else if (v instanceof Iterable) {
            sb.append('[');
            Iterator<?> it = ((Iterable<?>) v).iterator();
            while (it.hasNext()) {
                escribir(sb, it.next());
                if (it.hasNext()) sb.append(',');
            }
            sb.append(']');
        } else {
            cadena(sb, v.toString());
        }
    }

    private static void cadena(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    /** JSON ya serializado que se inserta tal cual (p. ej. ítems guardados en la cola). */
    public static final class Crudo {
        final String json;

        public Crudo(String json) {
            this.json = json;
        }
    }

    // ------------------------------------------------------------------ lectura

    public static Object leer(String texto) {
        Lector l = new Lector(texto);
        l.espacios();
        Object v = l.valor();
        l.espacios();
        if (l.i != texto.length()) throw new IllegalArgumentException("JSON con texto sobrante en " + l.i);
        return v;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> objeto(String texto) {
        Object v = leer(texto);
        if (!(v instanceof Map)) throw new IllegalArgumentException("se esperaba un objeto JSON");
        return (Map<String, Object>) v;
    }

    /** Texto de un campo (números y booleanos como texto); null si no está. */
    public static String texto(Map<String, Object> o, String clave) {
        Object v = o == null ? null : o.get(clave);
        if (v == null) return null;
        if (v instanceof Double && ((Double) v) == Math.rint((Double) v)) return String.valueOf(((Double) v).longValue());
        return v.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> lista(Map<String, Object> o, String clave) {
        Object v = o == null ? null : o.get(clave);
        return v instanceof List ? (List<Object>) v : new ArrayList<Object>();
    }

    private static final class Lector {
        final String s;
        int i;

        Lector(String s) {
            this.s = s;
        }

        void espacios() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        Object valor() {
            if (i >= s.length()) throw new IllegalArgumentException("JSON incompleto");
            char c = s.charAt(i);
            switch (c) {
                case '{': return objeto();
                case '[': return lista();
                case '"': return cadena();
                case 't': literal("true"); return Boolean.TRUE;
                case 'f': literal("false"); return Boolean.FALSE;
                case 'n': literal("null"); return null;
                default: return numero();
            }
        }

        Map<String, Object> objeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++;
            espacios();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                espacios();
                String k = cadena();
                espacios();
                esperar(':');
                espacios();
                m.put(k, valor());
                espacios();
                if (s.charAt(i) == ',') { i++; continue; }
                esperar('}');
                return m;
            }
        }

        List<Object> lista() {
            List<Object> l = new ArrayList<>();
            i++;
            espacios();
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) {
                espacios();
                l.add(valor());
                espacios();
                if (s.charAt(i) == ',') { i++; continue; }
                esperar(']');
                return l;
            }
        }

        String cadena() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u': sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                    default: sb.append(e);
                }
            }
        }

        Object numero() {
            int ini = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            String n = s.substring(ini, i);
            if (n.isEmpty()) throw new IllegalArgumentException("JSON inválido en " + ini);
            if (n.indexOf('.') < 0 && n.indexOf('e') < 0 && n.indexOf('E') < 0) {
                try {
                    return Long.parseLong(n);
                } catch (NumberFormatException ignorado) {
                    // muy grande: como double
                }
            }
            return Double.parseDouble(n);
        }

        void literal(String l) {
            if (!s.startsWith(l, i)) throw new IllegalArgumentException("JSON inválido en " + i);
            i += l.length();
        }

        void esperar(char c) {
            if (i >= s.length() || s.charAt(i) != c) throw new IllegalArgumentException("se esperaba '" + c + "' en " + i);
            i++;
        }
    }
}
