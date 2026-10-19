package com.gselectroCaqueta.sync;

import java.util.List;

/**
 * Cola persistente de lo que la terminal debe enviar. En la app es SQLite (ColaEnvioSqlite); en las
 * pruebas, memoria. Cada elemento pasa de PENDIENTE a ENVIADO o ERROR; un pendiente que falló por red
 * se reprograma (proximo_intento) sin cambiar de estado.
 */
public interface ColaEnvio {

    String LIQUIDACION = "liquidacion";
    String TRACKING = "tracking";
    String LBL = "lbl";
    String FOTO = "foto";

    int PENDIENTE = 0;
    int ENVIADO = 1;
    /** Rechazado por el servidor (dato que no sirve tal como está): queda para revisión, no se reintenta solo. */
    int ERROR = 2;

    final class Elemento {
        public final long id;
        public final String tipo;
        public final String uid;
        /** JSON listo para enviar (ítem, punto) o metadatos del archivo. */
        public final String datos;
        /** Archivo local (fotos y LBL). */
        public final String archivo;
        public final int intentos;

        public Elemento(long id, String tipo, String uid, String datos, String archivo, int intentos) {
            this.id = id;
            this.tipo = tipo;
            this.uid = uid;
            this.datos = datos;
            this.archivo = archivo;
            this.intentos = intentos;
        }
    }

    /** Agrega; si el uid ya existe no hace nada (encolar dos veces la misma cosa es inofensivo). */
    void encolar(String tipo, String uid, String datos, String archivo);

    /** Pendientes listos (proximo_intento <= ahora), en orden de llegada. */
    List<Elemento> pendientes(String tipo, int maximo, long ahoraMs);

    void marcarEnviados(List<Long> ids, long ahoraMs);

    void marcarError(long id, String motivo, long ahoraMs);

    /** Falló por red / servidor no disponible: suma un intento y espera hasta proximoIntentoMs. */
    void reprogramar(List<Long> ids, long proximoIntentoMs, String motivo);

    int contar(String tipo, int estado);

    /** Los que quedaron en ERROR vuelven a PENDIENTE (botón "reintentar errores"). */
    int reintentarErrores(String tipo);

    /** Borra enviados con más de diasRetencion días (los archivos los maneja el que los creó). */
    int purgarEnviados(long antesDeMs);
}
