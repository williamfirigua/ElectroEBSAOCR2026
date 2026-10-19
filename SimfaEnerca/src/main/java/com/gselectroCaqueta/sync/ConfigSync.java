package com.gselectroCaqueta.sync;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Estado de la sincronización que debe sobrevivir al proceso (SharedPreferences privadas) y la clave del
 * técnico, que NO se persiste: vive en memoria desde que la digita en GuiAcceso hasta que muere el proceso.
 * Con el token deslizante de 30 días el técnico rara vez la necesita de nuevo; si el token se pierde y la app
 * se reinició, el servicio avisa (REQUIERE_LOGIN) y espera al próximo ingreso.
 */
public final class ConfigSync implements MotorSincronizacion.Credenciales, MotorSincronizacion.AlmacenToken {

    /**
     * Modo de envío cuando la terminal nunca lo ha configurado. true = API REST /api/v1 (JSON);
     * false = SOAP como antes. Se cambia por terminal en Configurar Web/IP.
     * Orden de despliegue: servidor (60_api_movil.sql + web) antes que el APK.
     */
    public static final boolean MODO_REST_POR_DEFECTO = true;

    private static final String PREFS = "EbsaSync";
    private static final String K_MODO_REST = "modo_rest";
    private static final String K_BASE = "base_api";
    private static final String K_OPERADOR = "cod_operador";
    private static final String K_SERIAL = "serial_terminal";
    private static final String K_VERSION = "version_app";
    private static final String K_TOKEN = "token";
    private static final String K_TOKEN_OPERADOR = "token_operador";

    private static volatile String clave;

    private final SharedPreferences p;

    public ConfigSync(Context ctx) {
        this.p = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ------------------------------------------------------------------ modo

    public static boolean modoRest(Context ctx) {
        return new ConfigSync(ctx).modoRest();
    }

    public boolean modoRest() {
        return p.getBoolean(K_MODO_REST, MODO_REST_POR_DEFECTO);
    }

    public void modoRest(boolean rest) {
        p.edit().putBoolean(K_MODO_REST, rest).apply();
    }

    // ------------------------------------------------------------------ sesión del técnico

    /** GuiAcceso, al ingresar. Si cambió el técnico el token anterior ya no es de él. */
    public static void recordarSesion(Context ctx, String codOperador, String claveDigitada) {
        String cod = limpio(codOperador);
        clave = limpio(claveDigitada).isEmpty() ? null : limpio(claveDigitada);
        new ConfigSync(ctx).p.edit().putString(K_OPERADOR, cod).apply();
    }

    /** MenuDeLiquidacion, al arrancar el servicio (lo que el servicio necesita sin una Activity viva). */
    public void configurar(String baseApi, String serial, String version) {
        SharedPreferences.Editor e = p.edit();
        if (!limpio(baseApi).isEmpty()) e.putString(K_BASE, limpio(baseApi));
        if (!limpio(serial).isEmpty()) e.putString(K_SERIAL, limpio(serial));
        if (!limpio(version).isEmpty()) e.putString(K_VERSION, limpio(version));
        e.apply();
    }

    public String baseApi() {
        return p.getString(K_BASE, "");
    }

    /** El token guardado es de otro técnico (cambió el usuario en la terminal). */
    public boolean tokenDeOtroOperador() {
        String t = p.getString(K_TOKEN, null);
        String de = p.getString(K_TOKEN_OPERADOR, "");
        return t != null && !de.equals(codOperador());
    }

    // ------------------------------------------------------------------ Credenciales

    @Override
    public String codOperador() {
        return p.getString(K_OPERADOR, "");
    }

    @Override
    public String clave() {
        return clave;
    }

    @Override
    public String serialTerminal() {
        return p.getString(K_SERIAL, "");
    }

    @Override
    public String versionApp() {
        return p.getString(K_VERSION, "");
    }

    // ------------------------------------------------------------------ AlmacenToken

    @Override
    public String leer() {
        return p.getString(K_TOKEN, null);
    }

    @Override
    public void guardar(String token) {
        // commit (no apply): si el proceso muere justo después, el token emitido no se pierde; un login
        // nuevo revocaría la sesión y obligaría a la clave.
        p.edit().putString(K_TOKEN, token).putString(K_TOKEN_OPERADOR, codOperador()).commit();
    }

    @Override
    public void borrar() {
        p.edit().remove(K_TOKEN).remove(K_TOKEN_OPERADOR).commit();
    }

    private static String limpio(String s) {
        return s == null ? "" : s.trim();
    }
}
