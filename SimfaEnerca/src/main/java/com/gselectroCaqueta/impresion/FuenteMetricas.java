package com.gselectroCaqueta.impresion;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Ancho de texto de las fuentes TTF cargadas en la impresora, para alinear a la derecha y partir
 * líneas por ancho real con SCALE-TEXT (DIN Condensed no es monoespaciada: "1" mide 274 y "0" 403
 * milésimas de em).
 * <p>
 * Tablas generadas de DINCOND.TTF y DINBOLD.TTF (las del diseñador, 2026-10-14): avance de cada
 * carácter Latin-1 32..255 en milésimas de em. Una fuente que no está aquí se estima con 0,5 em.
 * Medida en puntos de impresora: ancho = Σ avance / 1000 × tamaño_pt × 203 / 72 (calibrado en la ZQ-521).
 */
public final class FuenteMetricas {

    private static final double PUNTOS_POR_PT = 203.0 / 72.0;
    private static final Map<String, int[]> TABLAS = new HashMap<>();

    static {
        TABLAS.put("DINCOND.TTF", new int[]{
            175, 204, 280, 462, 372, 691, 519, 150, 259, 259, 348, 420, 150, 375, 160, 296,
            403, 274, 392, 405, 416, 398, 395, 361, 403, 395, 160, 160, 420, 420, 420, 363,
            734, 461, 470, 478, 477, 403, 397, 478, 478, 204, 263, 461, 391, 598, 502, 478,
            440, 478, 466, 426, 368, 480, 423, 655, 416, 392, 385, 253, 296, 252, 420, 416,
            501, 401, 412, 409, 412, 408, 288, 412, 416, 199, 195, 409, 234, 646, 416, 409,
            412, 412, 290, 382, 295, 416, 371, 577, 349, 371, 376, 307, 266, 307, 420, 416,
            416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416,
            416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416, 416,
            175, 204, 409, 383, 584, 389, 266, 428, 501, 746, 294, 422, 420, 375, 472, 501,
            352, 420, 271, 268, 501, 413, 520, 160, 501, 199, 295, 422, 635, 644, 662, 363,
            461, 461, 461, 461, 461, 461, 688, 478, 403, 403, 403, 403, 204, 204, 204, 204,
            505, 502, 478, 478, 478, 478, 478, 420, 478, 480, 480, 480, 480, 392, 440, 413,
            401, 401, 401, 401, 401, 401, 633, 409, 408, 408, 408, 408, 198, 198, 198, 198,
            406, 416, 409, 409, 409, 409, 409, 420, 409, 416, 416, 416, 416, 371, 412, 371
        });
        TABLAS.put("DINBOLD.TTF", new int[]{
            175, 208, 351, 462, 428, 762, 514, 171, 272, 272, 336, 419, 171, 351, 175, 298,
            409, 293, 397, 399, 427, 406, 400, 381, 410, 400, 175, 175, 419, 419, 419, 376,
            714, 485, 456, 468, 463, 375, 371, 468, 460, 214, 266, 460, 367, 571, 474, 468,
            434, 468, 460, 426, 369, 460, 449, 674, 415, 415, 359, 283, 298, 283, 419, 425,
            500, 405, 410, 407, 410, 411, 287, 410, 410, 206, 206, 419, 240, 622, 410, 411,
            410, 410, 306, 392, 302, 410, 409, 609, 380, 409, 384, 319, 264, 319, 419, 410,
            410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410,
            410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410, 410,
            175, 208, 443, 420, 571, 409, 264, 409, 500, 703, 307, 442, 419, 351, 428, 500,
            325, 419, 283, 278, 500, 410, 553, 175, 500, 215, 310, 442, 696, 692, 759, 376,
            485, 485, 485, 485, 485, 485, 679, 468, 375, 375, 375, 375, 214, 214, 214, 214,
            483, 474, 468, 468, 468, 468, 468, 419, 468, 460, 460, 460, 460, 415, 434, 420,
            405, 405, 405, 405, 405, 405, 621, 407, 411, 411, 411, 411, 206, 206, 206, 206,
            399, 410, 411, 411, 411, 411, 411, 419, 411, 410, 410, 410, 410, 409, 410, 409
        });
    }

    private FuenteMetricas() {}

    /** true si hay métricas para la fuente (por nombre de archivo, sin importar mayúsculas). */
    public static boolean conoce(String fuente) {
        return fuente != null && TABLAS.containsKey(fuente.trim().toUpperCase(Locale.ROOT));
    }

    /** Ancho en puntos de impresora del texto escrito con SCALE-TEXT fuente, ancho_pt. */
    public static int ancho(String fuente, double anchoPt, String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        int[] t = fuente == null ? null : TABLAS.get(fuente.trim().toUpperCase(Locale.ROOT));
        long milesimas = 0;
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            if (t != null && ch >= 32 && ch <= 255) milesimas += t[ch - 32];
            else milesimas += 500;
        }
        return (int) Math.round(milesimas / 1000.0 * anchoPt * PUNTOS_POR_PT);
    }
}
