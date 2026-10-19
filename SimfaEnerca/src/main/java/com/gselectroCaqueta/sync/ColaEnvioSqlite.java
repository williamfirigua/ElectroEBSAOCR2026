package com.gselectroCaqueta.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Cola de envío en SQLite propia (ebsa_sync.db), separada de Realm: Realm guarda la configuración de
 * comunicaciones y no tiene migraciones; un cambio de esquema aquí no puede tumbar el arranque de la app.
 *
 * Una sola instancia por proceso (SQLiteOpenHelper es thread-safe con una conexión compartida). WAL para que
 * la liquidación (escritura) no espere al servicio (lectura/actualización).
 */
public final class ColaEnvioSqlite extends SQLiteOpenHelper implements ColaEnvio {

    private static final String BD = "ebsa_sync.db";
    private static final int VERSION = 2;
    private static final String T = "cola";
    private static final int MOTIVO_MAX = 500;

    private static volatile ColaEnvioSqlite instancia;

    public static ColaEnvioSqlite de(Context ctx) {
        ColaEnvioSqlite c = instancia;
        if (c == null) {
            synchronized (ColaEnvioSqlite.class) {
                c = instancia;
                if (c == null) instancia = c = new ColaEnvioSqlite(ctx.getApplicationContext());
            }
        }
        return c;
    }

    private ColaEnvioSqlite(Context ctx) {
        super(ctx, BD, null, VERSION);
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "tipo TEXT NOT NULL,"
                + "uid TEXT NOT NULL,"
                + "datos TEXT NOT NULL,"
                + "archivo TEXT,"
                + "estado INTEGER NOT NULL DEFAULT 0,"
                + "intentos INTEGER NOT NULL DEFAULT 0,"
                + "proximo INTEGER NOT NULL DEFAULT 0,"
                + "motivo TEXT,"
                + "creado INTEGER NOT NULL,"
                + "actualizado INTEGER NOT NULL,"
                + "UNIQUE (tipo, uid))");
        // pendientes() filtra por tipo+estado+proximo y ordena por id; contar() por tipo+estado.
        db.execSQL("CREATE INDEX idx_cola_pendientes ON " + T + " (tipo, estado, proximo, id)");
        db.execSQL("CREATE INDEX idx_cola_purga ON " + T + " (estado, actualizado)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        // Migraciones con ALTER/UPDATE; nunca DROP (se perderían pendientes).
        if (anterior < 2) {
            // v2: el APK del 16-oct mandaba la hora del punto como "timestamp" y el servidor los descartaba
            // respondiendo 200, así que quedaron como enviados. Se reenvían una vez (el servidor no duplica).
            db.execSQL("UPDATE " + T + " SET estado = " + PENDIENTE + ", proximo = 0, intentos = 0, motivo = NULL"
                    + " WHERE tipo = '" + TRACKING + "' AND estado = " + ENVIADO);
        }
    }

    // ------------------------------------------------------------------ ColaEnvio

    @Override
    public void encolar(String tipo, String uid, String datos, String archivo) {
        long ahora = System.currentTimeMillis();
        ContentValues v = new ContentValues();
        v.put("tipo", tipo);
        v.put("uid", uid);
        v.put("datos", datos);
        v.put("archivo", archivo);
        v.put("estado", PENDIENTE);
        v.put("creado", ahora);
        v.put("actualizado", ahora);
        getWritableDatabase().insertWithOnConflict(T, null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    @Override
    public List<Elemento> pendientes(String tipo, int maximo, long ahoraMs) {
        List<Elemento> r = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T,
                new String[]{"id", "tipo", "uid", "datos", "archivo", "intentos"},
                "tipo = ? AND estado = ? AND proximo <= ?",
                new String[]{tipo, String.valueOf(PENDIENTE), String.valueOf(ahoraMs)},
                null, null, "id", String.valueOf(maximo))) {
            while (c.moveToNext()) {
                r.add(new Elemento(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(5)));
            }
        }
        return r;
    }

    @Override
    public void marcarEnviados(List<Long> ids, long ahoraMs) {
        if (ids.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (List<Long> parte : partes(ids)) {
                db.execSQL("UPDATE " + T + " SET estado = " + ENVIADO + ", motivo = NULL, actualizado = ? WHERE id IN (" + marcadores(parte.size()) + ")",
                        argumentos(ahoraMs, parte));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void marcarError(long id, String motivo, long ahoraMs) {
        ContentValues v = new ContentValues();
        v.put("estado", ERROR);
        v.put("motivo", recortar(motivo));
        v.put("actualizado", ahoraMs);
        getWritableDatabase().update(T, v, "id = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public void reprogramar(List<Long> ids, long proximoIntentoMs, String motivo) {
        if (ids.isEmpty()) return;
        long ahora = System.currentTimeMillis();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (List<Long> parte : partes(ids)) {
                Object[] a = new Object[parte.size() + 3];
                a[0] = proximoIntentoMs;
                a[1] = recortar(motivo);
                a[2] = ahora;
                for (int i = 0; i < parte.size(); i++) a[i + 3] = parte.get(i);
                db.execSQL("UPDATE " + T + " SET intentos = intentos + 1, proximo = ?, motivo = ?, actualizado = ? WHERE id IN ("
                        + marcadores(parte.size()) + ")", a);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int contar(String tipo, int estado) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T + " WHERE tipo = ? AND estado = ?",
                new String[]{tipo, String.valueOf(estado)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    @Override
    public int reintentarErrores(String tipo) {
        ContentValues v = new ContentValues();
        v.put("estado", PENDIENTE);
        v.put("proximo", 0);
        v.put("actualizado", System.currentTimeMillis());
        return getWritableDatabase().update(T, v, "tipo = ? AND estado = ?", new String[]{tipo, String.valueOf(ERROR)});
    }

    @Override
    public int purgarEnviados(long antesDeMs) {
        return getWritableDatabase().delete(T, "estado = ? AND actualizado < ?",
                new String[]{String.valueOf(ENVIADO), String.valueOf(antesDeMs)});
    }

    // ------------------------------------------------------------------ extras de la app

    /**
     * Descarta pendientes de un tipo creados antes de la fecha. Solo para tracking: si el servidor no recibe
     * puntos por días (API sin desplegar, terminal sin señal) la cola no puede crecer sin límite. Nunca se usa
     * con liquidaciones ni archivos.
     */
    public int descartarPendientesViejos(String tipo, long antesDeMs) {
        if (!TRACKING.equals(tipo)) throw new IllegalArgumentException("solo tracking");
        return getWritableDatabase().delete(T, "tipo = ? AND estado = ? AND creado < ?",
                new String[]{tipo, String.valueOf(PENDIENTE), String.valueOf(antesDeMs)});
    }

    /** Los reprogramados por red vuelven a estar listos ya (llegó señal: no esperar el backoff). */
    public int adelantarReprogramados() {
        ContentValues v = new ContentValues();
        v.put("proximo", 0);
        return getWritableDatabase().update(T, v, "estado = ? AND proximo > 0", new String[]{String.valueOf(PENDIENTE)});
    }

    /** Un elemento no enviado con su estado, para mostrarlo en la consulta de no enviados. */
    public static final class NoEnviado {
        public final Elemento elemento;
        public final int estado;
        public final String motivo;

        NoEnviado(Elemento elemento, int estado, String motivo) {
            this.elemento = elemento;
            this.estado = estado;
            this.motivo = motivo;
        }
    }

    /** Pendientes y rechazados de un tipo: primero los rechazados, luego en orden de llegada. */
    public List<NoEnviado> noEnviados(String tipo, int desde, int cantidad) {
        List<NoEnviado> r = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, tipo, uid, datos, archivo, intentos, estado, motivo FROM " + T
                        + " WHERE tipo = ? AND estado IN (" + PENDIENTE + "," + ERROR + ") ORDER BY estado DESC, id LIMIT ? OFFSET ?",
                new String[]{tipo, String.valueOf(cantidad), String.valueOf(Math.max(0, desde))})) {
            while (c.moveToNext()) {
                r.add(new NoEnviado(new Elemento(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(5)),
                        c.getInt(6), c.isNull(7) ? null : c.getString(7)));
            }
        }
        return r;
    }

    /** Momento del próximo reintento programado (0 = hay pendientes listos ya; -1 = no hay pendientes). */
    public long proximoIntento() {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT MIN(proximo) FROM " + T + " WHERE estado = ?",
                new String[]{String.valueOf(PENDIENTE)})) {
            return c.moveToFirst() && !c.isNull(0) ? c.getLong(0) : -1;
        }
    }

    // ------------------------------------------------------------------ utilidades

    /** SQLite limita las variables por sentencia (999 en versiones viejas). */
    private static List<List<Long>> partes(List<Long> ids) {
        List<List<Long>> r = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += 500) r.add(ids.subList(i, Math.min(ids.size(), i + 500)));
        return r;
    }

    private static String marcadores(int n) {
        StringBuilder sb = new StringBuilder(n * 2);
        for (int i = 0; i < n; i++) sb.append(i == 0 ? "?" : ",?");
        return sb.toString();
    }

    private static Object[] argumentos(Object primero, List<Long> ids) {
        Object[] a = new Object[ids.size() + 1];
        a[0] = primero;
        for (int i = 0; i < ids.size(); i++) a[i + 1] = ids.get(i);
        return a;
    }

    private static String recortar(String s) {
        if (s == null) return null;
        return s.length() > MOTIVO_MAX ? s.substring(0, MOTIVO_MAX) : s;
    }
}
