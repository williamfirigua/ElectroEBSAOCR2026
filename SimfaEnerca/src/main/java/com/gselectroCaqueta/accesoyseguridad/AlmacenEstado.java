package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Estado pequeño de la liquidación que debe sobrevivir a un cierre de la app o un reinicio del equipo
 * (foto pendiente, última lectura, búsqueda y contenedor en curso). En la app son SharedPreferences
 * con commit() (escritura sincrónica); en pruebas, un mapa.
 */
public interface AlmacenEstado {
    String leer(String clave);

    void guardar(String clave, String valor);

    void borrar(String... claves);

    static AlmacenEstado de(Context ctx, String nombre) {
        final SharedPreferences p = ctx.getApplicationContext().getSharedPreferences(nombre, Context.MODE_PRIVATE);
        return new AlmacenEstado() {
            @Override
            public String leer(String clave) {
                return p.getString(clave, null);
            }

            @Override
            public void guardar(String clave, String valor) {
                p.edit().putString(clave, valor).commit();
            }

            @Override
            public void borrar(String... claves) {
                SharedPreferences.Editor e = p.edit();
                for (String c : claves) e.remove(c);
                e.commit();
            }
        };
    }
}
