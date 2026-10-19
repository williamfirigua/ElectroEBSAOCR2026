package com.gselectroCaqueta.accesoyseguridad;

//import static org.junit.Assert.assertEquals;

import com.gselectroCaqueta.accesoyseguridad.EvaluadorDesviacion.Bandas;
import com.gselectroCaqueta.accesoyseguridad.EvaluadorDesviacion.Decision;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import org.junit.Test;

/** Casos reales de junio 2026: ciclo 0 mensual y ciclo 11 trimestral (liquidación de la competencia). */
public class EvaluadorDesviacionTest {

    private static Bandas r(double liRes, double lsRes, double liAn, double lsAn) {
        return new Bandas("R", liRes, lsRes, 130, liAn, lsAn, -228, 408, false, false, false);
    }

    private static Date f(String s) throws Exception {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).parse(s);
    }

    private static Decision evaluar(double consumo, String anterior, String actual, Bandas b) throws Exception {
        return EvaluadorDesviacion.evaluar(consumo, EvaluadorDesviacion.diasTranscurridos(anterior, f(actual)), b).decision;
    }


    private static Decision evaluarTrimestral(double consumo, String anterior, String actual, Bandas b) throws Exception {
        int dias = EvaluadorDesviacion.diasTranscurridos(anterior, f(actual), 3);
        return EvaluadorDesviacion.evaluar(consumo, dias, 3, b).decision;
    }

    private static Bandas t(double liRes, double lsRes, double liAn, double lsAn) {
        return new Bandas("R", liRes, lsRes, 390, liAn, lsAn, -467, 811, false, false, false);
    }
 /*   @Test
    public void cuenta398661965_828kWh_31dias_esReal() throws Exception {
        assertEquals(Decision.REAL_BANDA_RESOLUCION,
                evaluar(828, "17/05/2026 12:10:44", "17/06/2026 22:07:48", r(618, 822, 644, 770)));
    }

    @Test
    public void cuenta402890821_938kWh_32dias_esReal() throws Exception {
        assertEquals(Decision.REAL_BANDA_RESOLUCION,
                evaluar(938, "17/05/2026 12:44:24", "18/06/2026 13:13:06", r(757, 925, 782, 874)));
    }

    @Test
    public void cuenta405594325_578kWh_31dias_esReal() throws Exception {
        assertEquals(Decision.REAL_BANDA_RESOLUCION,
                evaluar(578, "16/05/2026 15:08:55", "16/06/2026 20:02:57", r(467, 575, 490, 549)));
    }

    @Test
    public void cuenta401339756_consumoBajo_noCobraPromedio() throws Exception {
        assertEquals(Decision.REAL_BAJO_BANDA,
                evaluar(465, "17/05/2026 16:18:12", "17/06/2026 16:49:18", r(552, 696, 585, 683)));
    }

    @Test
    public void cuenta254771968_periodoCorto14dias_cobraPromedio() throws Exception {
        assertEquals(Decision.PROMEDIO,
                evaluar(291, "02/06/2026 00:00:00", "16/06/2026 10:00:00", r(158, 350, 158, 285)));
    }

    @Test
    public void cuenta119789641_29diasCompletos_cobraPromedio() throws Exception {
        // 29,8 días: se cuentan 29 periodos completos de 24 h -> 416 kWh normalizado
        assertEquals(Decision.PROMEDIO,
                evaluar(402, "20/05/2026 19:48:56", "19/06/2026 15:36:51", r(297, 399, 324, 381)));
    }

    @Test
    public void fechaInvalida_noNormaliza() throws Exception {
        assertEquals(EvaluadorDesviacion.DIAS_BASE, EvaluadorDesviacion.diasTranscurridos("", f("16/06/2026 10:00:00")));
        assertEquals(EvaluadorDesviacion.DIAS_BASE, EvaluadorDesviacion.diasTranscurridos("01/01/2024", f("16/06/2026 10:00:00")));
    }
    @Test
    public void trimestral156810483_914kWh_101dias_esReal() throws Exception {
        // 914 * 90 / 101 = 814 -> dentro de la banda de la resolución (744 - 912)
        assertEquals(Decision.REAL_BANDA_RESOLUCION,
                evaluarTrimestral(914, "27/03/2026 19:49:55", "06/07/2026 20:28:41", t(744, 912, 586, 876)));
    }

    @Test
    public void trimestral156958192_2237kWh_61dias_cobraPromedio() throws Exception {
        // 2237 * 90 / 61 = 3300 -> fuera de todas las bandas
        assertEquals(Decision.PROMEDIO,
                evaluarTrimestral(2237, "27/04/2026 00:00:00", "27/06/2026 16:40:13", t(-1340, 2302, 9, 1520)));
    }

    @Test
    public void trimestral_fechaInvalida_usa90dias() throws Exception {
        assertEquals(90, EvaluadorDesviacion.diasTranscurridos("01/01/2024", f("06/07/2026 10:00:00"), 3));
        assertEquals(120, EvaluadorDesviacion.diasTranscurridos("08/03/2026 10:00:00", f("06/07/2026 10:00:00"), 3));
    }

    @Test
    public void estacionaria_siempreReal() {
        Bandas e = new Bandas("E", 0, 10, 130, 0, 10, -228, 408, false, false, false);
        assertEquals(Decision.REAL_ESTACIONARIA_O_NUEVA, EvaluadorDesviacion.evaluar(5000, 30, e).decision);
    }
  */
}
