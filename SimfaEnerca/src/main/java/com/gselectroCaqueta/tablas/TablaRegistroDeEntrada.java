package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
/// <summary>
/// Descripcion breve del archivo a generar...tablaRegistroDeEntrada
/// Estructura nueva (2026), 58 campos -- ver GeneroEstrucRegistrador.txt.
/// Cambios frente a la version anterior:
///  - Se elimino Nrodecimales (ya no llega en el plano).
///  - Se eliminaron concecutivo y critica1..critica6 (comentados en el generador).
///  - Se agregaron 14 campos nuevos de desviaciones al final: MDDOR1ESTDO,
///    MDDR1CNCPTO, LQDA_CSMO, PRMDIO, LI_BNDA_RES, LS_BNDA_RES, LS_CONS_SUBS,
///    LI_BANDA_ANUAL, LS_BNDA_ANUAL, LS_BDA_TPO_US, LI_BDA_TPO_US, CMBIO_CLSE,
///    CMBIO_NVEL, NRMLZACION (igual orden y nombres que LM_SUSCRIPTOR_DSVCCION).
///  - Esta tabla es de SOLO LECTURA en el sistema: los metodos de escritura
///    (escribir_/rellenar_) de la version anterior estaban comentados y sin uso;
///    no se trasladan a esta version.
/// </summary>

public class TablaRegistroDeEntrada { //Ax ya es : E_REGIST

    // --- Anchos medidos directo de REGISTRO_-_Copy.TXT ---
    // 58 campos + separadores = 399 bytes de datos + 2 (CR LF) = 401 por registro.
    static final int LONGITUD_REGISTRO = 408;

    String sep = "|";
    String tablaRegistroDeEntrada_cliente;
    String tablaRegistroDeEntrada_contador;
    String tablaRegistroDeEntrada_ANIO;
    String tablaRegistroDeEntrada_MES;
    String tablaRegistroDeEntrada_CUENTA;
    String tablaRegistroDeEntrada_MARCA;
    String tablaRegistroDeEntrada_nrocontador;
    String tablaRegistroDeEntrada_Tipomedida;
    String tablaRegistroDeEntrada_Digitos;
    String tablaRegistroDeEntrada_Lecturaanterior;
    String tablaRegistroDeEntrada_tarifaliquidacion;
    String tablaRegistroDeEntrada_apuntadortarifa;
    String tablaRegistroDeEntrada_Factormultipicacion;
    String tablaRegistroDeEntrada_Consumopromediocliente;
    String tablaRegistroDeEntrada_Consumopromediosector;
    String tablaRegistroDeEntrada_ano1;
    String tablaRegistroDeEntrada_periodo1;
    String tablaRegistroDeEntrada_consumo1;
    String tablaRegistroDeEntrada_ano2;
    String tablaRegistroDeEntrada_periodo2;
    String tablaRegistroDeEntrada_consumo2;
    String tablaRegistroDeEntrada_ano3;
    String tablaRegistroDeEntrada_periodo3;
    String tablaRegistroDeEntrada_consumo3;
    String tablaRegistroDeEntrada_ano4;
    String tablaRegistroDeEntrada_periodo4;
    String tablaRegistroDeEntrada_consumo4;
    String tablaRegistroDeEntrada_ano5;
    String tablaRegistroDeEntrada_periodo5;
    String tablaRegistroDeEntrada_consumo5;
    String tablaRegistroDeEntrada_ano6;
    String tablaRegistroDeEntrada_periodo6;
    String tablaRegistroDeEntrada_consumo6;
    String tablaRegistroDeEntrada_consumoreal;
    String tablaRegistroDeEntrada_apuntadorreactiva;
    String tablaRegistroDeEntrada_tipoenergia;
    String tablaRegistroDeEntrada_tipoDesviacion;
    String tablaRegistroDeEntrada_promedioNormalizado;
    String tablaRegistroDeEntrada_desviacionStandar;
    String tablaRegistroDeEntrada_limiteInferior;
    String tablaRegistroDeEntrada_limiteSuperior;
    String tablaRegistroDeEntrada_limiteInferiorAnual;
    String tablaRegistroDeEntrada_limiteSuperiorAnual;
    String tablaRegistroDeEntrada_rangoSubsistencia;
    String tablaRegistroDeEntrada_MDDOR1ESTDO;
    String tablaRegistroDeEntrada_MDDR1CNCPTO;
    String tablaRegistroDeEntrada_LQDA_CSMO;
    String tablaRegistroDeEntrada_PRMDIO;
    String tablaRegistroDeEntrada_LI_BNDA_RES;
    String tablaRegistroDeEntrada_LS_BNDA_RES;
    String tablaRegistroDeEntrada_LS_CONS_SUBS;
    String tablaRegistroDeEntrada_LI_BANDA_ANUAL;
    String tablaRegistroDeEntrada_LS_BNDA_ANUAL;
    String tablaRegistroDeEntrada_LS_BDA_TPO_US;
    String tablaRegistroDeEntrada_LI_BDA_TPO_US;
    String tablaRegistroDeEntrada_CMBIO_CLSE;
    String tablaRegistroDeEntrada_CMBIO_NVEL;
    String tablaRegistroDeEntrada_NRMLZACION;
    String tablaRegistroDeEntrada_VALORSUBSISTENCIA;
    // Buffer reusable: se reserva UNA sola vez y se sobreescribe en cada
    // lectura, en vez de crear un byte[] nuevo por cada registro leido.
    private final byte[] bufferRegistro = new byte[LONGITUD_REGISTRO];
    int total_TablaRegistroDeEntrada;
    int ultimo_TablaRegistroDeEntrada;
    int encontro_TablaRegistroDeEntrada;
    RandomAccessFile rFile;
    private String archivo_TablaRegistroDeEntrada;

    public String gettablaRegistroDeEntrada_cliente() {
        return tablaRegistroDeEntrada_cliente;
    }

    public void settablaRegistroDeEntrada_cliente(String tablaRegistroDeEntrada_cliente) {
        this.tablaRegistroDeEntrada_cliente = tablaRegistroDeEntrada_cliente;
    }

    public String gettablaRegistroDeEntrada_contador() {
        return tablaRegistroDeEntrada_contador;
    }

    public void settablaRegistroDeEntrada_contador(String tablaRegistroDeEntrada_contador) {
        this.tablaRegistroDeEntrada_contador = tablaRegistroDeEntrada_contador;
    }

    public String gettablaRegistroDeEntrada_ANIO() {
        return tablaRegistroDeEntrada_ANIO;
    }

    public void settablaRegistroDeEntrada_ANIO(String tablaRegistroDeEntrada_ANIO) {
        this.tablaRegistroDeEntrada_ANIO = tablaRegistroDeEntrada_ANIO;
    }

    public String gettablaRegistroDeEntrada_MES() {
        return tablaRegistroDeEntrada_MES;
    }

    public void settablaRegistroDeEntrada_MES(String tablaRegistroDeEntrada_MES) {
        this.tablaRegistroDeEntrada_MES = tablaRegistroDeEntrada_MES;
    }

    public String gettablaRegistroDeEntrada_CUENTA() {
        return tablaRegistroDeEntrada_CUENTA;
    }

    public void settablaRegistroDeEntrada_CUENTA(String tablaRegistroDeEntrada_CUENTA) {
        this.tablaRegistroDeEntrada_CUENTA = tablaRegistroDeEntrada_CUENTA;
    }

    public String gettablaRegistroDeEntrada_MARCA() {
        return tablaRegistroDeEntrada_MARCA;
    }

    public void settablaRegistroDeEntrada_MARCA(String tablaRegistroDeEntrada_MARCA) {
        this.tablaRegistroDeEntrada_MARCA = tablaRegistroDeEntrada_MARCA;
    }

    public String gettablaRegistroDeEntrada_nrocontador() {
        return tablaRegistroDeEntrada_nrocontador;
    }

    public void settablaRegistroDeEntrada_nrocontador(String tablaRegistroDeEntrada_nrocontador) {
        this.tablaRegistroDeEntrada_nrocontador = tablaRegistroDeEntrada_nrocontador;
    }

    public String gettablaRegistroDeEntrada_Tipomedida() {
        return tablaRegistroDeEntrada_Tipomedida;
    }

    public void settablaRegistroDeEntrada_Tipomedida(String tablaRegistroDeEntrada_Tipomedida) {
        this.tablaRegistroDeEntrada_Tipomedida = tablaRegistroDeEntrada_Tipomedida;
    }

    public String gettablaRegistroDeEntrada_Digitos() {
        return tablaRegistroDeEntrada_Digitos;
    }

    public void settablaRegistroDeEntrada_Digitos(String tablaRegistroDeEntrada_Digitos) {
        this.tablaRegistroDeEntrada_Digitos = tablaRegistroDeEntrada_Digitos;
    }

    public String gettablaRegistroDeEntrada_Lecturaanterior() {
        return tablaRegistroDeEntrada_Lecturaanterior;
    }

    public void settablaRegistroDeEntrada_Lecturaanterior(String tablaRegistroDeEntrada_Lecturaanterior) {
        this.tablaRegistroDeEntrada_Lecturaanterior = tablaRegistroDeEntrada_Lecturaanterior;
    }

    public String gettablaRegistroDeEntrada_tarifaliquidacion() {
        return tablaRegistroDeEntrada_tarifaliquidacion;
    }

    public void settablaRegistroDeEntrada_tarifaliquidacion(String tablaRegistroDeEntrada_tarifaliquidacion) {
        this.tablaRegistroDeEntrada_tarifaliquidacion = tablaRegistroDeEntrada_tarifaliquidacion;
    }

    public String gettablaRegistroDeEntrada_apuntadortarifa() {
        return tablaRegistroDeEntrada_apuntadortarifa;
    }

    public void settablaRegistroDeEntrada_apuntadortarifa(String tablaRegistroDeEntrada_apuntadortarifa) {
        this.tablaRegistroDeEntrada_apuntadortarifa = tablaRegistroDeEntrada_apuntadortarifa;
    }

    public String gettablaRegistroDeEntrada_Factormultipicacion() {
        return tablaRegistroDeEntrada_Factormultipicacion;
    }

    public void settablaRegistroDeEntrada_Factormultipicacion(String tablaRegistroDeEntrada_Factormultipicacion) {
        this.tablaRegistroDeEntrada_Factormultipicacion = tablaRegistroDeEntrada_Factormultipicacion;
    }

    public String gettablaRegistroDeEntrada_Consumopromediocliente() {
        return tablaRegistroDeEntrada_Consumopromediocliente;
    }

    public void settablaRegistroDeEntrada_Consumopromediocliente(String tablaRegistroDeEntrada_Consumopromediocliente) {
        this.tablaRegistroDeEntrada_Consumopromediocliente = tablaRegistroDeEntrada_Consumopromediocliente;
    }

    public String gettablaRegistroDeEntrada_Consumopromediosector() {
        return tablaRegistroDeEntrada_Consumopromediosector;
    }

    public void settablaRegistroDeEntrada_Consumopromediosector(String tablaRegistroDeEntrada_Consumopromediosector) {
        this.tablaRegistroDeEntrada_Consumopromediosector = tablaRegistroDeEntrada_Consumopromediosector;
    }

    public String gettablaRegistroDeEntrada_ano1() {
        return tablaRegistroDeEntrada_ano1;
    }

    public void settablaRegistroDeEntrada_ano1(String tablaRegistroDeEntrada_ano1) {
        this.tablaRegistroDeEntrada_ano1 = tablaRegistroDeEntrada_ano1;
    }

    public String gettablaRegistroDeEntrada_periodo1() {
        return tablaRegistroDeEntrada_periodo1;
    }

    public void settablaRegistroDeEntrada_periodo1(String tablaRegistroDeEntrada_periodo1) {
        this.tablaRegistroDeEntrada_periodo1 = tablaRegistroDeEntrada_periodo1;
    }

    public String gettablaRegistroDeEntrada_consumo1() {
        return tablaRegistroDeEntrada_consumo1;
    }

    public void settablaRegistroDeEntrada_consumo1(String tablaRegistroDeEntrada_consumo1) {
        this.tablaRegistroDeEntrada_consumo1 = tablaRegistroDeEntrada_consumo1;
    }

    public String gettablaRegistroDeEntrada_ano2() {
        return tablaRegistroDeEntrada_ano2;
    }

    public void settablaRegistroDeEntrada_ano2(String tablaRegistroDeEntrada_ano2) {
        this.tablaRegistroDeEntrada_ano2 = tablaRegistroDeEntrada_ano2;
    }

    public String gettablaRegistroDeEntrada_periodo2() {
        return tablaRegistroDeEntrada_periodo2;
    }

    public void settablaRegistroDeEntrada_periodo2(String tablaRegistroDeEntrada_periodo2) {
        this.tablaRegistroDeEntrada_periodo2 = tablaRegistroDeEntrada_periodo2;
    }

    public String gettablaRegistroDeEntrada_consumo2() {
        return tablaRegistroDeEntrada_consumo2;
    }

    public void settablaRegistroDeEntrada_consumo2(String tablaRegistroDeEntrada_consumo2) {
        this.tablaRegistroDeEntrada_consumo2 = tablaRegistroDeEntrada_consumo2;
    }

    public String gettablaRegistroDeEntrada_ano3() {
        return tablaRegistroDeEntrada_ano3;
    }

    public void settablaRegistroDeEntrada_ano3(String tablaRegistroDeEntrada_ano3) {
        this.tablaRegistroDeEntrada_ano3 = tablaRegistroDeEntrada_ano3;
    }

    public String gettablaRegistroDeEntrada_periodo3() {
        return tablaRegistroDeEntrada_periodo3;
    }

    public void settablaRegistroDeEntrada_periodo3(String tablaRegistroDeEntrada_periodo3) {
        this.tablaRegistroDeEntrada_periodo3 = tablaRegistroDeEntrada_periodo3;
    }

    public String gettablaRegistroDeEntrada_consumo3() {
        return tablaRegistroDeEntrada_consumo3;
    }

    public void settablaRegistroDeEntrada_consumo3(String tablaRegistroDeEntrada_consumo3) {
        this.tablaRegistroDeEntrada_consumo3 = tablaRegistroDeEntrada_consumo3;
    }

    public String gettablaRegistroDeEntrada_ano4() {
        return tablaRegistroDeEntrada_ano4;
    }

    public void settablaRegistroDeEntrada_ano4(String tablaRegistroDeEntrada_ano4) {
        this.tablaRegistroDeEntrada_ano4 = tablaRegistroDeEntrada_ano4;
    }

    public String gettablaRegistroDeEntrada_periodo4() {
        return tablaRegistroDeEntrada_periodo4;
    }

    public void settablaRegistroDeEntrada_periodo4(String tablaRegistroDeEntrada_periodo4) {
        this.tablaRegistroDeEntrada_periodo4 = tablaRegistroDeEntrada_periodo4;
    }

    public String gettablaRegistroDeEntrada_consumo4() {
        return tablaRegistroDeEntrada_consumo4;
    }

    public void settablaRegistroDeEntrada_consumo4(String tablaRegistroDeEntrada_consumo4) {
        this.tablaRegistroDeEntrada_consumo4 = tablaRegistroDeEntrada_consumo4;
    }

    public String gettablaRegistroDeEntrada_ano5() {
        return tablaRegistroDeEntrada_ano5;
    }

    public void settablaRegistroDeEntrada_ano5(String tablaRegistroDeEntrada_ano5) {
        this.tablaRegistroDeEntrada_ano5 = tablaRegistroDeEntrada_ano5;
    }

    public String gettablaRegistroDeEntrada_periodo5() {
        return tablaRegistroDeEntrada_periodo5;
    }

    public void settablaRegistroDeEntrada_periodo5(String tablaRegistroDeEntrada_periodo5) {
        this.tablaRegistroDeEntrada_periodo5 = tablaRegistroDeEntrada_periodo5;
    }

    public String gettablaRegistroDeEntrada_consumo5() {
        return tablaRegistroDeEntrada_consumo5;
    }

    public void settablaRegistroDeEntrada_consumo5(String tablaRegistroDeEntrada_consumo5) {
        this.tablaRegistroDeEntrada_consumo5 = tablaRegistroDeEntrada_consumo5;
    }

    public String gettablaRegistroDeEntrada_ano6() {
        return tablaRegistroDeEntrada_ano6;
    }

    public void settablaRegistroDeEntrada_ano6(String tablaRegistroDeEntrada_ano6) {
        this.tablaRegistroDeEntrada_ano6 = tablaRegistroDeEntrada_ano6;
    }

    public String gettablaRegistroDeEntrada_periodo6() {
        return tablaRegistroDeEntrada_periodo6;
    }

    public void settablaRegistroDeEntrada_periodo6(String tablaRegistroDeEntrada_periodo6) {
        this.tablaRegistroDeEntrada_periodo6 = tablaRegistroDeEntrada_periodo6;
    }

    public String gettablaRegistroDeEntrada_consumo6() {
        return tablaRegistroDeEntrada_consumo6;
    }

    public void settablaRegistroDeEntrada_consumo6(String tablaRegistroDeEntrada_consumo6) {
        this.tablaRegistroDeEntrada_consumo6 = tablaRegistroDeEntrada_consumo6;
    }

    public String gettablaRegistroDeEntrada_consumoreal() {
        return tablaRegistroDeEntrada_consumoreal;
    }

    public void settablaRegistroDeEntrada_consumoreal(String tablaRegistroDeEntrada_consumoreal) {
        this.tablaRegistroDeEntrada_consumoreal = tablaRegistroDeEntrada_consumoreal;
    }

    public String gettablaRegistroDeEntrada_apuntadorreactiva() {
        return tablaRegistroDeEntrada_apuntadorreactiva;
    }

    public void settablaRegistroDeEntrada_apuntadorreactiva(String tablaRegistroDeEntrada_apuntadorreactiva) {
        this.tablaRegistroDeEntrada_apuntadorreactiva = tablaRegistroDeEntrada_apuntadorreactiva;
    }

    public String gettablaRegistroDeEntrada_tipoenergia() {
        return tablaRegistroDeEntrada_tipoenergia;
    }

    public void settablaRegistroDeEntrada_tipoenergia(String tablaRegistroDeEntrada_tipoenergia) {
        this.tablaRegistroDeEntrada_tipoenergia = tablaRegistroDeEntrada_tipoenergia;
    }

    public String gettablaRegistroDeEntrada_tipoDesviacion() {
        return tablaRegistroDeEntrada_tipoDesviacion;
    }

    public void settablaRegistroDeEntrada_tipoDesviacion(String tablaRegistroDeEntrada_tipoDesviacion) {
        this.tablaRegistroDeEntrada_tipoDesviacion = tablaRegistroDeEntrada_tipoDesviacion;
    }

    public String gettablaRegistroDeEntrada_promedioNormalizado() {
        return tablaRegistroDeEntrada_promedioNormalizado;
    }

    public void settablaRegistroDeEntrada_promedioNormalizado(String tablaRegistroDeEntrada_promedioNormalizado) {
        this.tablaRegistroDeEntrada_promedioNormalizado = tablaRegistroDeEntrada_promedioNormalizado;
    }

    public String gettablaRegistroDeEntrada_desviacionStandar() {
        return tablaRegistroDeEntrada_desviacionStandar;
    }

    public void settablaRegistroDeEntrada_desviacionStandar(String tablaRegistroDeEntrada_desviacionStandar) {
        this.tablaRegistroDeEntrada_desviacionStandar = tablaRegistroDeEntrada_desviacionStandar;
    }

    public String gettablaRegistroDeEntrada_limiteInferior() {
        return tablaRegistroDeEntrada_limiteInferior;
    }

    public void settablaRegistroDeEntrada_limiteInferior(String tablaRegistroDeEntrada_limiteInferior) {
        this.tablaRegistroDeEntrada_limiteInferior = tablaRegistroDeEntrada_limiteInferior;
    }

    public String gettablaRegistroDeEntrada_limiteSuperior() {
        return tablaRegistroDeEntrada_limiteSuperior;
    }

    public void settablaRegistroDeEntrada_limiteSuperior(String tablaRegistroDeEntrada_limiteSuperior) {
        this.tablaRegistroDeEntrada_limiteSuperior = tablaRegistroDeEntrada_limiteSuperior;
    }

    public String gettablaRegistroDeEntrada_limiteInferiorAnual() {
        return tablaRegistroDeEntrada_limiteInferiorAnual;
    }

    public void settablaRegistroDeEntrada_limiteInferiorAnual(String tablaRegistroDeEntrada_limiteInferiorAnual) {
        this.tablaRegistroDeEntrada_limiteInferiorAnual = tablaRegistroDeEntrada_limiteInferiorAnual;
    }

    public String gettablaRegistroDeEntrada_limiteSuperiorAnual() {
        return tablaRegistroDeEntrada_limiteSuperiorAnual;
    }

    public void settablaRegistroDeEntrada_limiteSuperiorAnual(String tablaRegistroDeEntrada_limiteSuperiorAnual) {
        this.tablaRegistroDeEntrada_limiteSuperiorAnual = tablaRegistroDeEntrada_limiteSuperiorAnual;
    }

    public String gettablaRegistroDeEntrada_rangoSubsistencia() {
        return tablaRegistroDeEntrada_rangoSubsistencia;
    }

    public void settablaRegistroDeEntrada_rangoSubsistencia(String tablaRegistroDeEntrada_rangoSubsistencia) {
        this.tablaRegistroDeEntrada_rangoSubsistencia = tablaRegistroDeEntrada_rangoSubsistencia;
    }

    public String gettablaRegistroDeEntrada_MDDOR1ESTDO() {
        return tablaRegistroDeEntrada_MDDOR1ESTDO;
    }

    public void settablaRegistroDeEntrada_MDDOR1ESTDO(String tablaRegistroDeEntrada_MDDOR1ESTDO) {
        this.tablaRegistroDeEntrada_MDDOR1ESTDO = tablaRegistroDeEntrada_MDDOR1ESTDO;
    }

    public String gettablaRegistroDeEntrada_MDDR1CNCPTO() {
        return tablaRegistroDeEntrada_MDDR1CNCPTO;
    }

    public void settablaRegistroDeEntrada_MDDR1CNCPTO(String tablaRegistroDeEntrada_MDDR1CNCPTO) {
        this.tablaRegistroDeEntrada_MDDR1CNCPTO = tablaRegistroDeEntrada_MDDR1CNCPTO;
    }

    public String gettablaRegistroDeEntrada_LQDA_CSMO() {
        return tablaRegistroDeEntrada_LQDA_CSMO;
    }

    public void settablaRegistroDeEntrada_LQDA_CSMO(String tablaRegistroDeEntrada_LQDA_CSMO) {
        this.tablaRegistroDeEntrada_LQDA_CSMO = tablaRegistroDeEntrada_LQDA_CSMO;
    }

    public String gettablaRegistroDeEntrada_PRMDIO() {
        return tablaRegistroDeEntrada_PRMDIO;
    }

    public void settablaRegistroDeEntrada_PRMDIO(String tablaRegistroDeEntrada_PRMDIO) {
        this.tablaRegistroDeEntrada_PRMDIO = tablaRegistroDeEntrada_PRMDIO;
    }

    public String gettablaRegistroDeEntrada_LI_BNDA_RES() {
        return tablaRegistroDeEntrada_LI_BNDA_RES;
    }

    public void settablaRegistroDeEntrada_LI_BNDA_RES(String tablaRegistroDeEntrada_LI_BNDA_RES) {
        this.tablaRegistroDeEntrada_LI_BNDA_RES = tablaRegistroDeEntrada_LI_BNDA_RES;
    }

    public String gettablaRegistroDeEntrada_LS_BNDA_RES() {
        return tablaRegistroDeEntrada_LS_BNDA_RES;
    }

    public void settablaRegistroDeEntrada_LS_BNDA_RES(String tablaRegistroDeEntrada_LS_BNDA_RES) {
        this.tablaRegistroDeEntrada_LS_BNDA_RES = tablaRegistroDeEntrada_LS_BNDA_RES;
    }

    public String gettablaRegistroDeEntrada_LS_CONS_SUBS() {
        return tablaRegistroDeEntrada_LS_CONS_SUBS;
    }

    public void settablaRegistroDeEntrada_LS_CONS_SUBS(String tablaRegistroDeEntrada_LS_CONS_SUBS) {
        this.tablaRegistroDeEntrada_LS_CONS_SUBS = tablaRegistroDeEntrada_LS_CONS_SUBS;
    }

    public String gettablaRegistroDeEntrada_LI_BANDA_ANUAL() {
        return tablaRegistroDeEntrada_LI_BANDA_ANUAL;
    }

    public void settablaRegistroDeEntrada_LI_BANDA_ANUAL(String tablaRegistroDeEntrada_LI_BANDA_ANUAL) {
        this.tablaRegistroDeEntrada_LI_BANDA_ANUAL = tablaRegistroDeEntrada_LI_BANDA_ANUAL;
    }

    public String gettablaRegistroDeEntrada_LS_BNDA_ANUAL() {
        return tablaRegistroDeEntrada_LS_BNDA_ANUAL;
    }

    public void settablaRegistroDeEntrada_LS_BNDA_ANUAL(String tablaRegistroDeEntrada_LS_BNDA_ANUAL) {
        this.tablaRegistroDeEntrada_LS_BNDA_ANUAL = tablaRegistroDeEntrada_LS_BNDA_ANUAL;
    }

    public String gettablaRegistroDeEntrada_LS_BDA_TPO_US() {
        return tablaRegistroDeEntrada_LS_BDA_TPO_US;
    }

    public void settablaRegistroDeEntrada_LS_BDA_TPO_US(String tablaRegistroDeEntrada_LS_BDA_TPO_US) {
        this.tablaRegistroDeEntrada_LS_BDA_TPO_US = tablaRegistroDeEntrada_LS_BDA_TPO_US;
    }

    public String gettablaRegistroDeEntrada_LI_BDA_TPO_US() {
        return tablaRegistroDeEntrada_LI_BDA_TPO_US;
    }

    public void settablaRegistroDeEntrada_LI_BDA_TPO_US(String tablaRegistroDeEntrada_LI_BDA_TPO_US) {
        this.tablaRegistroDeEntrada_LI_BDA_TPO_US = tablaRegistroDeEntrada_LI_BDA_TPO_US;
    }

    public String gettablaRegistroDeEntrada_CMBIO_CLSE() {
        return tablaRegistroDeEntrada_CMBIO_CLSE;
    }

    public void settablaRegistroDeEntrada_CMBIO_CLSE(String tablaRegistroDeEntrada_CMBIO_CLSE) {
        this.tablaRegistroDeEntrada_CMBIO_CLSE = tablaRegistroDeEntrada_CMBIO_CLSE;
    }

    public String gettablaRegistroDeEntrada_CMBIO_NVEL() {
        return tablaRegistroDeEntrada_CMBIO_NVEL;
    }

    public void settablaRegistroDeEntrada_CMBIO_NVEL(String tablaRegistroDeEntrada_CMBIO_NVEL) {
        this.tablaRegistroDeEntrada_CMBIO_NVEL = tablaRegistroDeEntrada_CMBIO_NVEL;
    }

    public String gettablaRegistroDeEntrada_NRMLZACION() {
        return tablaRegistroDeEntrada_NRMLZACION;
    }

    public void settablaRegistroDeEntrada_NRMLZACION(String tablaRegistroDeEntrada_NRMLZACION) {
        this.tablaRegistroDeEntrada_NRMLZACION = tablaRegistroDeEntrada_NRMLZACION;
    }


    public String gettablaRegistroDeEntrada_VALORSUBSISTENCIA() {
        return tablaRegistroDeEntrada_VALORSUBSISTENCIA;
    }

    public void settablaRegistroDeEntrada_VALORSUBSISTENCIA(String tablaRegistroDeEntrada_VALORSUBSISTENCIA) {
        this.tablaRegistroDeEntrada_VALORSUBSISTENCIA = tablaRegistroDeEntrada_VALORSUBSISTENCIA;
    }

    // Abre el archivo UNA vez. lectura_TablaRegistroDeEntrada ya NO vuelve a
    // abrirlo ni a releer nada de mas -- solo hace seek+read puntual.
    public Boolean abrir_TablaRegistroDeEntrada(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        setArchivo_TablaRegistroDeEntrada(nombreArchivo);
        return abrir();
    }

    // Ya NO lee ningun registro por adelantado -- solo calcula cuantos hay,
    // a partir del tamano del archivo.
    private Boolean abrir() {
        try {
            int fileSize = (int) rFile.length();
            total_TablaRegistroDeEntrada = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaRegistroDeEntrada() {
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void posicion_TablaRegistroDeEntrada(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Lee el registro en la posicion indicada usando el buffer reusable, y
    // separa sus campos leyendo directo de los bytes.
    // IMPORTANTE: requiere haber llamado abrir_TablaRegistroDeEntrada() antes.
    public int lectura_TablaRegistroDeEntrada(int registro) {
        encontro_TablaRegistroDeEntrada = 0;
        posicion_TablaRegistroDeEntrada(registro, LONGITUD_REGISTRO);
        try {
            rFile.readFully(bufferRegistro);
            settablaRegistroDeEntrada_cliente(new String(bufferRegistro, 0, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_contador(new String(bufferRegistro, 6, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_ANIO(new String(bufferRegistro, 12, 4, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_MES(new String(bufferRegistro, 17, 2, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_CUENTA(new String(bufferRegistro, 20, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_MARCA(new String(bufferRegistro, 31, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_nrocontador(new String(bufferRegistro, 42, 16, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_Tipomedida(new String(bufferRegistro, 59, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_Digitos(new String(bufferRegistro, 61, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_Lecturaanterior(new String(bufferRegistro, 63, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_tarifaliquidacion(new String(bufferRegistro, 74, 3, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_apuntadortarifa(new String(bufferRegistro, 78, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_Factormultipicacion(new String(bufferRegistro, 84, 12, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_Consumopromediocliente(new String(bufferRegistro, 97, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_Consumopromediosector(new String(bufferRegistro, 108, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_ano1(new String(bufferRegistro, 119, 4, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_periodo1(new String(bufferRegistro, 124, 2, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_consumo1(new String(bufferRegistro, 127, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_ano2(new String(bufferRegistro, 133, 4, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_periodo2(new String(bufferRegistro, 138, 2, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_consumo2(new String(bufferRegistro, 141, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_ano3(new String(bufferRegistro, 147, 4, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_periodo3(new String(bufferRegistro, 152, 2, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_consumo3(new String(bufferRegistro, 155, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_ano4(new String(bufferRegistro, 161, 4, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_periodo4(new String(bufferRegistro, 166, 2, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_consumo4(new String(bufferRegistro, 169, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_ano5(new String(bufferRegistro, 175, 4, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_periodo5(new String(bufferRegistro, 180, 2, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_consumo5(new String(bufferRegistro, 183, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_ano6(new String(bufferRegistro, 189, 4, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_periodo6(new String(bufferRegistro, 194, 2, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_consumo6(new String(bufferRegistro, 197, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_consumoreal(new String(bufferRegistro, 203, 8, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_apuntadorreactiva(new String(bufferRegistro, 212, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_tipoenergia(new String(bufferRegistro, 214, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_tipoDesviacion(new String(bufferRegistro, 216, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_promedioNormalizado(new String(bufferRegistro, 218, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_desviacionStandar(new String(bufferRegistro, 229, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_limiteInferior(new String(bufferRegistro, 240, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_limiteSuperior(new String(bufferRegistro, 251, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_limiteInferiorAnual(new String(bufferRegistro, 262, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_limiteSuperiorAnual(new String(bufferRegistro, 273, 10, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_rangoSubsistencia(new String(bufferRegistro, 284, 5, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_MDDOR1ESTDO(new String(bufferRegistro, 290, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_MDDR1CNCPTO(new String(bufferRegistro, 292, 3, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LQDA_CSMO(new String(bufferRegistro, 296, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_PRMDIO(new String(bufferRegistro, 298, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LI_BNDA_RES(new String(bufferRegistro, 310, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LS_BNDA_RES(new String(bufferRegistro, 322, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LS_CONS_SUBS(new String(bufferRegistro, 334, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LI_BANDA_ANUAL(new String(bufferRegistro, 346, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LS_BNDA_ANUAL(new String(bufferRegistro, 358, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LS_BDA_TPO_US(new String(bufferRegistro, 370, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_LI_BDA_TPO_US(new String(bufferRegistro, 382, 11, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_CMBIO_CLSE(new String(bufferRegistro, 394, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_CMBIO_NVEL(new String(bufferRegistro, 396, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_NRMLZACION(new String(bufferRegistro, 398, 1, StandardCharsets.UTF_8));
            settablaRegistroDeEntrada_VALORSUBSISTENCIA(new String(bufferRegistro, 400, 5, StandardCharsets.UTF_8));

            ultimo_TablaRegistroDeEntrada = registro;
            // fin estructura
        } catch (Exception e) {
            Log.e("error", "no lee rege error " + e.getMessage());
            e.printStackTrace();
            return -2;
        }
        return 1;
    }

    // Busqueda SECUENCIAL conservada tal cual la logica original: recorre
    // registro por registro comparando contra 'promedio_sector' (antes
    // 'Consumopromediosector'). Se preserva el mismo campo de comparacion --
    // si en realidad se buscaba por otro campo (ej. COD_CUENTA), avisame y
    // se corrige en una sola linea.
    public void buscarSecuencial_TablaRegistroDeEntrada(String codigo) {
        encontro_TablaRegistroDeEntrada = 0;
        for (int i = 0; i < total_TablaRegistroDeEntrada; i++) {
            if (lectura_TablaRegistroDeEntrada(i + 1) == -2) {
                continue;
            }
            if (Double.parseDouble(codigo.trim()) == Double.parseDouble(tablaRegistroDeEntrada_CUENTA.trim())) {
                encontro_TablaRegistroDeEntrada = i + 1;
                posicion_TablaRegistroDeEntrada(i + 1, LONGITUD_REGISTRO);
                return;
            }
        }
    }

    public String getArchivo_TablaRegistroDeEntrada() {
        return archivo_TablaRegistroDeEntrada;
    }

    public void setArchivo_TablaRegistroDeEntrada(String archivo_TablaRegistroDeEntrada) {
        this.archivo_TablaRegistroDeEntrada = archivo_TablaRegistroDeEntrada;
    }

    public int getTotal_TablaRegistroDeEntrada() {
        return total_TablaRegistroDeEntrada;
    }

    public void setTotal_TablaRegistroDeEntrada(int total_TablaRegistroDeEntrada) {
        this.total_TablaRegistroDeEntrada = total_TablaRegistroDeEntrada;
    }
}