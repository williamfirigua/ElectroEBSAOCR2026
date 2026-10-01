package com.gselectroCaqueta.tablas;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
/// <summary>
/// Descripcion breve del archivo a generar...tablaMedidorEntrada
/// Estructura nueva (2026), 111 campos -- ver generadorplanosMedidorEntrada.txt
/// y MEDIDOR.TXT. Los 16 primeros campos ya existian en la version anterior y
/// se dejaron con el nombre EXACTO que ya tenian (misma mayuscula/minuscula).
/// Todo lo demas (aseo, convenios) es nuevo, sin equivalente previo.
/// </summary>

public class TablaMedidorEntrada { //Ax ya es: E_CONTAD

    // 111 campos + separadores = 1115 bytes de datos
    // + 2 bytes de fin de linea (CR LF) = 1117 bytes por registro.
    static final int LONGITUD_REGISTRO = 1119;

    String sep = "|";
    String tablaMedidorEntrada_anio;  // ANO
    String tablaMedidorEntrada_mes;
    String tablaMedidorEntrada_Cuenta;  // cod_cliente
    String tablaMedidorEntrada_Marca;  // marca
    String tablaMedidorEntrada_NUMero;  // Contador
    String tablaMedidorEntrada_Tipomedidor;  // tipomedidor
    String tablaMedidorEntrada_Indfacturacionpr;  // ind_fact_promedio
    String tablaMedidorEntrada_Estadomedidor;  // estado_medidor
    String tablaMedidorEntrada_Fechalectanterior;  // fecha_lect_anterior
    String tablaMedidorEntrada_Nroregistros;  // numeroreg
    String tablaMedidorEntrada_Fechamedcambiado;  // fecha_retiro
    String tablaMedidorEntrada_primerregistro;  // (literal 00000)
    String tablaMedidorEntrada_cliente;  // IDclient
    String tablaMedidorEntrada_idcortado;  // consumo_retiro
    String tablaMedidorEntrada_lecturacorte;  // lectura_retiro
    String tablaMedidorEntrada_obsercorte;  // contador_retiro(2)
    String tablaMedidorEntrada_dpto;
    String tablaMedidorEntrada_municipio;
    String tablaMedidorEntrada_convenio;
    String tablaMedidorEntrada_ciclo;
    String tablaMedidorEntrada_CUENTA_ASEO;
    String tablaMedidorEntrada_DIAS_LIQUIDADOS;
    String tablaMedidorEntrada_periodo;
    String tablaMedidorEntrada_TIPO_PRODUCTOR;
    String tablaMedidorEntrada_estrato;
    String tablaMedidorEntrada_CVRNAR;
    String tablaMedidorEntrada_CVRNANR;
    String tablaMedidorEntrada_VBARAUA;
    String tablaMedidorEntrada_VBARAUNA;
    String tablaMedidorEntrada_COSTO_FIJO;
    String tablaMedidorEntrada_VOLUMEN;
    String tablaMedidorEntrada_DENSIDAD;
    String tablaMedidorEntrada_URO;
    String tablaMedidorEntrada_UNRO;
    String tablaMedidorEntrada_URD;
    String tablaMedidorEntrada_UNRD;
    String tablaMedidorEntrada_contribucion;
    String tablaMedidorEntrada_subsidio;
    String tablaMedidorEntrada_BARRIDO;
    String tablaMedidorEntrada_RECOLECCION;
    String tablaMedidorEntrada_PODA;
    String tablaMedidorEntrada_CORTE_CESPED;
    String tablaMedidorEntrada_ESCOMBROS;
    String tablaMedidorEntrada_TRBL;
    String tablaMedidorEntrada_TRLU;
    String tablaMedidorEntrada_TRRA;
    String tablaMedidorEntrada_TAFA;
    String tablaMedidorEntrada_TRA;
    String tablaMedidorEntrada_TRNA;
    String tablaMedidorEntrada_TAFNA;
    String tablaMedidorEntrada_TTSM;
    String tablaMedidorEntrada_TRBLM1;
    String tablaMedidorEntrada_TRLUM1;
    String tablaMedidorEntrada_TRRAM1;
    String tablaMedidorEntrada_TAFAM1;
    String tablaMedidorEntrada_TRAM1;
    String tablaMedidorEntrada_TRNAM1;
    String tablaMedidorEntrada_TAFNAM1;
    String tablaMedidorEntrada_TTSMM1;
    String tablaMedidorEntrada_TRBLM2;
    String tablaMedidorEntrada_TRLUM2;
    String tablaMedidorEntrada_TRRAM2;
    String tablaMedidorEntrada_TAFAM2;
    String tablaMedidorEntrada_TRAM2;
    String tablaMedidorEntrada_TRNAM2;
    String tablaMedidorEntrada_TAFNAM2;
    String tablaMedidorEntrada_TTSMM2;
    String tablaMedidorEntrada_MES_1;
    String tablaMedidorEntrada_MES_2;
    String tablaMedidorEntrada_MES_3;
    String tablaMedidorEntrada_MES_4;
    String tablaMedidorEntrada_MES_5;
    String tablaMedidorEntrada_VALOR_FACTURA_1;
    String tablaMedidorEntrada_VALOR_FACTURA_2;
    String tablaMedidorEntrada_VALOR_FACTURA_3;
    String tablaMedidorEntrada_VALOR_FACTURA_4;
    String tablaMedidorEntrada_VALOR_FACTURA_5;
    String tablaMedidorEntrada_SALDO_COVID;
    String tablaMedidorEntrada_DEUDA_CASTIGADA;
    String tablaMedidorEntrada_DIFERIDO_COVID;
    String tablaMedidorEntrada_SALDO_NO_COVID;
    String tablaMedidorEntrada_RECLAMACION;
    String tablaMedidorEntrada_DIFERIDO_NO_COVID;
    String tablaMedidorEntrada_NOTA1;
    String tablaMedidorEntrada_NOTA2;
    String tablaMedidorEntrada_MENSAJE;
    String tablaMedidorEntrada_PPARTICIPACION;
    String tablaMedidorEntrada_CNVNIO1CNCPTO;
    String tablaMedidorEntrada_CNVNIO1TPOVLOR;
    String tablaMedidorEntrada_CNVNIO1TPOPRCNTJE;
    String tablaMedidorEntrada_CNVNIO1VLOR;
    String tablaMedidorEntrada_CNVNIO1RNGOMNMO;
    String tablaMedidorEntrada_CNVNIO1RNGOMXMO;
    String tablaMedidorEntrada_CNVNIO1TPEMNMO;
    String tablaMedidorEntrada_CNVNIO1TPEMXMO;
    String tablaMedidorEntrada_CNVNIO2CNCPTO;
    String tablaMedidorEntrada_CNVNIO2TPOVLOR;
    String tablaMedidorEntrada_CNVNIO2TPOPRCNTJE;
    String tablaMedidorEntrada_CNVNIO2VLOR;
    String tablaMedidorEntrada_CNVNIO2RNGOMNMO;
    String tablaMedidorEntrada_CNVNIO2RNGOMXMO;
    String tablaMedidorEntrada_CNVNIO2TPEMNMO;
    String tablaMedidorEntrada_CNVNIO2TPEMXMO;
    String tablaMedidorEntrada_CNVNIO3CNCPTO;
    String tablaMedidorEntrada_CNVNIO3TPOVLOR;
    String tablaMedidorEntrada_CNVNIO3TPOPRCNTJE;
    String tablaMedidorEntrada_CNVNIO3VLOR;
    String tablaMedidorEntrada_CNVNIO3RNGOMNMO;
    String tablaMedidorEntrada_CNVNIO3RNGOMXMO;
    String tablaMedidorEntrada_CNVNIO3TPEMNMO;
    String tablaMedidorEntrada_CNVNIO3TPEMXMO;

    // Buffer reusable: se reserva UNA sola vez y se sobreescribe en cada
    // lectura, en vez de crear un byte[] nuevo por cada registro leido.
    private final byte[] bufferRegistro = new byte[LONGITUD_REGISTRO];
    int total_TablaMedidorEntrada;
    int ultimo_TablaMedidorEntrada;
    int encontro_TablaMedidorEntrada;
    RandomAccessFile rFile;
    private String archivo_TablaMedidorEntrada;

    public String gettablaMedidorEntrada_anio() {
        return tablaMedidorEntrada_anio;
    }

    public void settablaMedidorEntrada_anio(String tablaMedidorEntrada_anio) {
        this.tablaMedidorEntrada_anio = tablaMedidorEntrada_anio;
    }



    public int getEncontro_TablaMedidorEntrada() {
        return encontro_TablaMedidorEntrada;
    }

    public void setEncontro_TablaMedidorEntrada(int encontro_TablaMedidorEntrada) {
        this.encontro_TablaMedidorEntrada = encontro_TablaMedidorEntrada;
    }



    public String gettablaMedidorEntrada_mes() {
        return tablaMedidorEntrada_mes;
    }

    public void settablaMedidorEntrada_mes(String tablaMedidorEntrada_mes) {
        this.tablaMedidorEntrada_mes = tablaMedidorEntrada_mes;
    }

    public String gettablaMedidorEntrada_Cuenta() {
        return tablaMedidorEntrada_Cuenta;
    }

    public void settablaMedidorEntrada_Cuenta(String tablaMedidorEntrada_Cuenta) {
        this.tablaMedidorEntrada_Cuenta = tablaMedidorEntrada_Cuenta;
    }

    public String gettablaMedidorEntrada_Marca() {
        return tablaMedidorEntrada_Marca;
    }

    public void settablaMedidorEntrada_Marca(String tablaMedidorEntrada_Marca) {
        this.tablaMedidorEntrada_Marca = tablaMedidorEntrada_Marca;
    }

    public String gettablaMedidorEntrada_NUMero() {
        return tablaMedidorEntrada_NUMero;
    }

    public void settablaMedidorEntrada_NUMero(String tablaMedidorEntrada_NUMero) {
        this.tablaMedidorEntrada_NUMero = tablaMedidorEntrada_NUMero;
    }

    public String gettablaMedidorEntrada_Tipomedidor() {
        return tablaMedidorEntrada_Tipomedidor;
    }

    public void settablaMedidorEntrada_Tipomedidor(String tablaMedidorEntrada_Tipomedidor) {
        this.tablaMedidorEntrada_Tipomedidor = tablaMedidorEntrada_Tipomedidor;
    }

    public String gettablaMedidorEntrada_Indfacturacionpr() {
        return tablaMedidorEntrada_Indfacturacionpr;
    }

    public void settablaMedidorEntrada_Indfacturacionpr(String tablaMedidorEntrada_Indfacturacionpr) {
        this.tablaMedidorEntrada_Indfacturacionpr = tablaMedidorEntrada_Indfacturacionpr;
    }

    public String gettablaMedidorEntrada_Estadomedidor() {
        return tablaMedidorEntrada_Estadomedidor;
    }

    public void settablaMedidorEntrada_Estadomedidor(String tablaMedidorEntrada_Estadomedidor) {
        this.tablaMedidorEntrada_Estadomedidor = tablaMedidorEntrada_Estadomedidor;
    }

    public String gettablaMedidorEntrada_Fechalectanterior() {
        return tablaMedidorEntrada_Fechalectanterior;
    }

    public void settablaMedidorEntrada_Fechalectanterior(String tablaMedidorEntrada_Fechalectanterior) {
        this.tablaMedidorEntrada_Fechalectanterior = tablaMedidorEntrada_Fechalectanterior;
    }

    public String gettablaMedidorEntrada_Nroregistros() {
        return tablaMedidorEntrada_Nroregistros;
    }

    public void settablaMedidorEntrada_Nroregistros(String tablaMedidorEntrada_Nroregistros) {
        this.tablaMedidorEntrada_Nroregistros = tablaMedidorEntrada_Nroregistros;
    }

    public String gettablaMedidorEntrada_Fechamedcambiado() {
        return tablaMedidorEntrada_Fechamedcambiado;
    }

    public void settablaMedidorEntrada_Fechamedcambiado(String tablaMedidorEntrada_Fechamedcambiado) {
        this.tablaMedidorEntrada_Fechamedcambiado = tablaMedidorEntrada_Fechamedcambiado;
    }

    public String gettablaMedidorEntrada_primerregistro() {
        return tablaMedidorEntrada_primerregistro;
    }

    public void settablaMedidorEntrada_primerregistro(String tablaMedidorEntrada_primerregistro) {
        this.tablaMedidorEntrada_primerregistro = tablaMedidorEntrada_primerregistro;
    }

    public String gettablaMedidorEntrada_cliente() {
        return tablaMedidorEntrada_cliente;
    }

    public void settablaMedidorEntrada_cliente(String tablaMedidorEntrada_cliente) {
        this.tablaMedidorEntrada_cliente = tablaMedidorEntrada_cliente;
    }

    public String gettablaMedidorEntrada_idcortado() {
        return tablaMedidorEntrada_idcortado;
    }

    public void settablaMedidorEntrada_idcortado(String tablaMedidorEntrada_idcortado) {
        this.tablaMedidorEntrada_idcortado = tablaMedidorEntrada_idcortado;
    }

    public String gettablaMedidorEntrada_lecturacorte() {
        return tablaMedidorEntrada_lecturacorte;
    }

    public void settablaMedidorEntrada_lecturacorte(String tablaMedidorEntrada_lecturacorte) {
        this.tablaMedidorEntrada_lecturacorte = tablaMedidorEntrada_lecturacorte;
    }

    public String gettablaMedidorEntrada_obsercorte() {
        return tablaMedidorEntrada_obsercorte;
    }

    public void settablaMedidorEntrada_obsercorte(String tablaMedidorEntrada_obsercorte) {
        this.tablaMedidorEntrada_obsercorte = tablaMedidorEntrada_obsercorte;
    }

    public String gettablaMedidorEntrada_dpto() {
        return tablaMedidorEntrada_dpto;
    }

    public void settablaMedidorEntrada_dpto(String tablaMedidorEntrada_dpto) {
        this.tablaMedidorEntrada_dpto = tablaMedidorEntrada_dpto;
    }

    public String gettablaMedidorEntrada_municipio() {
        return tablaMedidorEntrada_municipio;
    }

    public void settablaMedidorEntrada_municipio(String tablaMedidorEntrada_municipio) {
        this.tablaMedidorEntrada_municipio = tablaMedidorEntrada_municipio;
    }

    public String gettablaMedidorEntrada_convenio() {
        return tablaMedidorEntrada_convenio;
    }

    public void settablaMedidorEntrada_convenio(String tablaMedidorEntrada_convenio) {
        this.tablaMedidorEntrada_convenio = tablaMedidorEntrada_convenio;
    }

    public String gettablaMedidorEntrada_ciclo() {
        return tablaMedidorEntrada_ciclo;
    }

    public void settablaMedidorEntrada_ciclo(String tablaMedidorEntrada_ciclo) {
        this.tablaMedidorEntrada_ciclo = tablaMedidorEntrada_ciclo;
    }

    public String gettablaMedidorEntrada_CUENTA_ASEO() {
        return tablaMedidorEntrada_CUENTA_ASEO;
    }

    public void settablaMedidorEntrada_CUENTA_ASEO(String tablaMedidorEntrada_CUENTA_ASEO) {
        this.tablaMedidorEntrada_CUENTA_ASEO = tablaMedidorEntrada_CUENTA_ASEO;
    }

    public String gettablaMedidorEntrada_DIAS_LIQUIDADOS() {
        return tablaMedidorEntrada_DIAS_LIQUIDADOS;
    }

    public void settablaMedidorEntrada_DIAS_LIQUIDADOS(String tablaMedidorEntrada_DIAS_LIQUIDADOS) {
        this.tablaMedidorEntrada_DIAS_LIQUIDADOS = tablaMedidorEntrada_DIAS_LIQUIDADOS;
    }

    public String gettablaMedidorEntrada_periodo() {
        return tablaMedidorEntrada_periodo;
    }

    public void settablaMedidorEntrada_periodo(String tablaMedidorEntrada_periodo) {
        this.tablaMedidorEntrada_periodo = tablaMedidorEntrada_periodo;
    }

    public String gettablaMedidorEntrada_TIPO_PRODUCTOR() {
        return tablaMedidorEntrada_TIPO_PRODUCTOR;
    }

    public void settablaMedidorEntrada_TIPO_PRODUCTOR(String tablaMedidorEntrada_TIPO_PRODUCTOR) {
        this.tablaMedidorEntrada_TIPO_PRODUCTOR = tablaMedidorEntrada_TIPO_PRODUCTOR;
    }

    public String gettablaMedidorEntrada_estrato() {
        return tablaMedidorEntrada_estrato;
    }

    public void settablaMedidorEntrada_estrato(String tablaMedidorEntrada_estrato) {
        this.tablaMedidorEntrada_estrato = tablaMedidorEntrada_estrato;
    }

    public String gettablaMedidorEntrada_CVRNAR() {
        return tablaMedidorEntrada_CVRNAR;
    }

    public void settablaMedidorEntrada_CVRNAR(String tablaMedidorEntrada_CVRNAR) {
        this.tablaMedidorEntrada_CVRNAR = tablaMedidorEntrada_CVRNAR;
    }

    public String gettablaMedidorEntrada_CVRNANR() {
        return tablaMedidorEntrada_CVRNANR;
    }

    public void settablaMedidorEntrada_CVRNANR(String tablaMedidorEntrada_CVRNANR) {
        this.tablaMedidorEntrada_CVRNANR = tablaMedidorEntrada_CVRNANR;
    }

    public String gettablaMedidorEntrada_VBARAUA() {
        return tablaMedidorEntrada_VBARAUA;
    }

    public void settablaMedidorEntrada_VBARAUA(String tablaMedidorEntrada_VBARAUA) {
        this.tablaMedidorEntrada_VBARAUA = tablaMedidorEntrada_VBARAUA;
    }

    public String gettablaMedidorEntrada_VBARAUNA() {
        return tablaMedidorEntrada_VBARAUNA;
    }

    public void settablaMedidorEntrada_VBARAUNA(String tablaMedidorEntrada_VBARAUNA) {
        this.tablaMedidorEntrada_VBARAUNA = tablaMedidorEntrada_VBARAUNA;
    }

    public String gettablaMedidorEntrada_COSTO_FIJO() {
        return tablaMedidorEntrada_COSTO_FIJO;
    }

    public void settablaMedidorEntrada_COSTO_FIJO(String tablaMedidorEntrada_COSTO_FIJO) {
        this.tablaMedidorEntrada_COSTO_FIJO = tablaMedidorEntrada_COSTO_FIJO;
    }

    public String gettablaMedidorEntrada_VOLUMEN() {
        return tablaMedidorEntrada_VOLUMEN;
    }

    public void settablaMedidorEntrada_VOLUMEN(String tablaMedidorEntrada_VOLUMEN) {
        this.tablaMedidorEntrada_VOLUMEN = tablaMedidorEntrada_VOLUMEN;
    }

    public String gettablaMedidorEntrada_DENSIDAD() {
        return tablaMedidorEntrada_DENSIDAD;
    }

    public void settablaMedidorEntrada_DENSIDAD(String tablaMedidorEntrada_DENSIDAD) {
        this.tablaMedidorEntrada_DENSIDAD = tablaMedidorEntrada_DENSIDAD;
    }

    public String gettablaMedidorEntrada_URO() {
        return tablaMedidorEntrada_URO;
    }

    public void settablaMedidorEntrada_URO(String tablaMedidorEntrada_URO) {
        this.tablaMedidorEntrada_URO = tablaMedidorEntrada_URO;
    }

    public String gettablaMedidorEntrada_UNRO() {
        return tablaMedidorEntrada_UNRO;
    }

    public void settablaMedidorEntrada_UNRO(String tablaMedidorEntrada_UNRO) {
        this.tablaMedidorEntrada_UNRO = tablaMedidorEntrada_UNRO;
    }

    public String gettablaMedidorEntrada_URD() {
        return tablaMedidorEntrada_URD;
    }

    public void settablaMedidorEntrada_URD(String tablaMedidorEntrada_URD) {
        this.tablaMedidorEntrada_URD = tablaMedidorEntrada_URD;
    }

    public String gettablaMedidorEntrada_UNRD() {
        return tablaMedidorEntrada_UNRD;
    }

    public void settablaMedidorEntrada_UNRD(String tablaMedidorEntrada_UNRD) {
        this.tablaMedidorEntrada_UNRD = tablaMedidorEntrada_UNRD;
    }

    public String gettablaMedidorEntrada_contribucion() {
        return tablaMedidorEntrada_contribucion;
    }

    public void settablaMedidorEntrada_contribucion(String tablaMedidorEntrada_contribucion) {
        this.tablaMedidorEntrada_contribucion = tablaMedidorEntrada_contribucion;
    }

    public String gettablaMedidorEntrada_subsidio() {
        return tablaMedidorEntrada_subsidio;
    }

    public void settablaMedidorEntrada_subsidio(String tablaMedidorEntrada_subsidio) {
        this.tablaMedidorEntrada_subsidio = tablaMedidorEntrada_subsidio;
    }

    public String gettablaMedidorEntrada_BARRIDO() {
        return tablaMedidorEntrada_BARRIDO;
    }

    public void settablaMedidorEntrada_BARRIDO(String tablaMedidorEntrada_BARRIDO) {
        this.tablaMedidorEntrada_BARRIDO = tablaMedidorEntrada_BARRIDO;
    }

    public String gettablaMedidorEntrada_RECOLECCION() {
        return tablaMedidorEntrada_RECOLECCION;
    }

    public void settablaMedidorEntrada_RECOLECCION(String tablaMedidorEntrada_RECOLECCION) {
        this.tablaMedidorEntrada_RECOLECCION = tablaMedidorEntrada_RECOLECCION;
    }

    public String gettablaMedidorEntrada_PODA() {
        return tablaMedidorEntrada_PODA;
    }

    public void settablaMedidorEntrada_PODA(String tablaMedidorEntrada_PODA) {
        this.tablaMedidorEntrada_PODA = tablaMedidorEntrada_PODA;
    }

    public String gettablaMedidorEntrada_CORTE_CESPED() {
        return tablaMedidorEntrada_CORTE_CESPED;
    }

    public void settablaMedidorEntrada_CORTE_CESPED(String tablaMedidorEntrada_CORTE_CESPED) {
        this.tablaMedidorEntrada_CORTE_CESPED = tablaMedidorEntrada_CORTE_CESPED;
    }

    public String gettablaMedidorEntrada_ESCOMBROS() {
        return tablaMedidorEntrada_ESCOMBROS;
    }

    public void settablaMedidorEntrada_ESCOMBROS(String tablaMedidorEntrada_ESCOMBROS) {
        this.tablaMedidorEntrada_ESCOMBROS = tablaMedidorEntrada_ESCOMBROS;
    }

    public String gettablaMedidorEntrada_TRBL() {
        return tablaMedidorEntrada_TRBL;
    }

    public void settablaMedidorEntrada_TRBL(String tablaMedidorEntrada_TRBL) {
        this.tablaMedidorEntrada_TRBL = tablaMedidorEntrada_TRBL;
    }

    public String gettablaMedidorEntrada_TRLU() {
        return tablaMedidorEntrada_TRLU;
    }

    public void settablaMedidorEntrada_TRLU(String tablaMedidorEntrada_TRLU) {
        this.tablaMedidorEntrada_TRLU = tablaMedidorEntrada_TRLU;
    }

    public String gettablaMedidorEntrada_TRRA() {
        return tablaMedidorEntrada_TRRA;
    }

    public void settablaMedidorEntrada_TRRA(String tablaMedidorEntrada_TRRA) {
        this.tablaMedidorEntrada_TRRA = tablaMedidorEntrada_TRRA;
    }

    public String gettablaMedidorEntrada_TAFA() {
        return tablaMedidorEntrada_TAFA;
    }

    public void settablaMedidorEntrada_TAFA(String tablaMedidorEntrada_TAFA) {
        this.tablaMedidorEntrada_TAFA = tablaMedidorEntrada_TAFA;
    }

    public String gettablaMedidorEntrada_TRA() {
        return tablaMedidorEntrada_TRA;
    }

    public void settablaMedidorEntrada_TRA(String tablaMedidorEntrada_TRA) {
        this.tablaMedidorEntrada_TRA = tablaMedidorEntrada_TRA;
    }

    public String gettablaMedidorEntrada_TRNA() {
        return tablaMedidorEntrada_TRNA;
    }

    public void settablaMedidorEntrada_TRNA(String tablaMedidorEntrada_TRNA) {
        this.tablaMedidorEntrada_TRNA = tablaMedidorEntrada_TRNA;
    }

    public String gettablaMedidorEntrada_TAFNA() {
        return tablaMedidorEntrada_TAFNA;
    }

    public void settablaMedidorEntrada_TAFNA(String tablaMedidorEntrada_TAFNA) {
        this.tablaMedidorEntrada_TAFNA = tablaMedidorEntrada_TAFNA;
    }

    public String gettablaMedidorEntrada_TTSM() {
        return tablaMedidorEntrada_TTSM;
    }

    public void settablaMedidorEntrada_TTSM(String tablaMedidorEntrada_TTSM) {
        this.tablaMedidorEntrada_TTSM = tablaMedidorEntrada_TTSM;
    }

    public String gettablaMedidorEntrada_TRBLM1() {
        return tablaMedidorEntrada_TRBLM1;
    }

    public void settablaMedidorEntrada_TRBLM1(String tablaMedidorEntrada_TRBLM1) {
        this.tablaMedidorEntrada_TRBLM1 = tablaMedidorEntrada_TRBLM1;
    }

    public String gettablaMedidorEntrada_TRLUM1() {
        return tablaMedidorEntrada_TRLUM1;
    }

    public void settablaMedidorEntrada_TRLUM1(String tablaMedidorEntrada_TRLUM1) {
        this.tablaMedidorEntrada_TRLUM1 = tablaMedidorEntrada_TRLUM1;
    }

    public String gettablaMedidorEntrada_TRRAM1() {
        return tablaMedidorEntrada_TRRAM1;
    }

    public void settablaMedidorEntrada_TRRAM1(String tablaMedidorEntrada_TRRAM1) {
        this.tablaMedidorEntrada_TRRAM1 = tablaMedidorEntrada_TRRAM1;
    }

    public String gettablaMedidorEntrada_TAFAM1() {
        return tablaMedidorEntrada_TAFAM1;
    }

    public void settablaMedidorEntrada_TAFAM1(String tablaMedidorEntrada_TAFAM1) {
        this.tablaMedidorEntrada_TAFAM1 = tablaMedidorEntrada_TAFAM1;
    }

    public String gettablaMedidorEntrada_TRAM1() {
        return tablaMedidorEntrada_TRAM1;
    }

    public void settablaMedidorEntrada_TRAM1(String tablaMedidorEntrada_TRAM1) {
        this.tablaMedidorEntrada_TRAM1 = tablaMedidorEntrada_TRAM1;
    }

    public String gettablaMedidorEntrada_TRNAM1() {
        return tablaMedidorEntrada_TRNAM1;
    }

    public void settablaMedidorEntrada_TRNAM1(String tablaMedidorEntrada_TRNAM1) {
        this.tablaMedidorEntrada_TRNAM1 = tablaMedidorEntrada_TRNAM1;
    }

    public String gettablaMedidorEntrada_TAFNAM1() {
        return tablaMedidorEntrada_TAFNAM1;
    }

    public void settablaMedidorEntrada_TAFNAM1(String tablaMedidorEntrada_TAFNAM1) {
        this.tablaMedidorEntrada_TAFNAM1 = tablaMedidorEntrada_TAFNAM1;
    }

    public String gettablaMedidorEntrada_TTSMM1() {
        return tablaMedidorEntrada_TTSMM1;
    }

    public void settablaMedidorEntrada_TTSMM1(String tablaMedidorEntrada_TTSMM1) {
        this.tablaMedidorEntrada_TTSMM1 = tablaMedidorEntrada_TTSMM1;
    }

    public String gettablaMedidorEntrada_TRBLM2() {
        return tablaMedidorEntrada_TRBLM2;
    }

    public void settablaMedidorEntrada_TRBLM2(String tablaMedidorEntrada_TRBLM2) {
        this.tablaMedidorEntrada_TRBLM2 = tablaMedidorEntrada_TRBLM2;
    }

    public String gettablaMedidorEntrada_TRLUM2() {
        return tablaMedidorEntrada_TRLUM2;
    }

    public void settablaMedidorEntrada_TRLUM2(String tablaMedidorEntrada_TRLUM2) {
        this.tablaMedidorEntrada_TRLUM2 = tablaMedidorEntrada_TRLUM2;
    }

    public String gettablaMedidorEntrada_TRRAM2() {
        return tablaMedidorEntrada_TRRAM2;
    }

    public void settablaMedidorEntrada_TRRAM2(String tablaMedidorEntrada_TRRAM2) {
        this.tablaMedidorEntrada_TRRAM2 = tablaMedidorEntrada_TRRAM2;
    }

    public String gettablaMedidorEntrada_TAFAM2() {
        return tablaMedidorEntrada_TAFAM2;
    }

    public void settablaMedidorEntrada_TAFAM2(String tablaMedidorEntrada_TAFAM2) {
        this.tablaMedidorEntrada_TAFAM2 = tablaMedidorEntrada_TAFAM2;
    }

    public String gettablaMedidorEntrada_TRAM2() {
        return tablaMedidorEntrada_TRAM2;
    }

    public void settablaMedidorEntrada_TRAM2(String tablaMedidorEntrada_TRAM2) {
        this.tablaMedidorEntrada_TRAM2 = tablaMedidorEntrada_TRAM2;
    }

    public String gettablaMedidorEntrada_TRNAM2() {
        return tablaMedidorEntrada_TRNAM2;
    }

    public void settablaMedidorEntrada_TRNAM2(String tablaMedidorEntrada_TRNAM2) {
        this.tablaMedidorEntrada_TRNAM2 = tablaMedidorEntrada_TRNAM2;
    }

    public String gettablaMedidorEntrada_TAFNAM2() {
        return tablaMedidorEntrada_TAFNAM2;
    }

    public void settablaMedidorEntrada_TAFNAM2(String tablaMedidorEntrada_TAFNAM2) {
        this.tablaMedidorEntrada_TAFNAM2 = tablaMedidorEntrada_TAFNAM2;
    }

    public String gettablaMedidorEntrada_TTSMM2() {
        return tablaMedidorEntrada_TTSMM2;
    }

    public void settablaMedidorEntrada_TTSMM2(String tablaMedidorEntrada_TTSMM2) {
        this.tablaMedidorEntrada_TTSMM2 = tablaMedidorEntrada_TTSMM2;
    }

    public String gettablaMedidorEntrada_MES_1() {
        return tablaMedidorEntrada_MES_1;
    }

    public void settablaMedidorEntrada_MES_1(String tablaMedidorEntrada_MES_1) {
        this.tablaMedidorEntrada_MES_1 = tablaMedidorEntrada_MES_1;
    }

    public String gettablaMedidorEntrada_MES_2() {
        return tablaMedidorEntrada_MES_2;
    }

    public void settablaMedidorEntrada_MES_2(String tablaMedidorEntrada_MES_2) {
        this.tablaMedidorEntrada_MES_2 = tablaMedidorEntrada_MES_2;
    }

    public String gettablaMedidorEntrada_MES_3() {
        return tablaMedidorEntrada_MES_3;
    }

    public void settablaMedidorEntrada_MES_3(String tablaMedidorEntrada_MES_3) {
        this.tablaMedidorEntrada_MES_3 = tablaMedidorEntrada_MES_3;
    }

    public String gettablaMedidorEntrada_MES_4() {
        return tablaMedidorEntrada_MES_4;
    }

    public void settablaMedidorEntrada_MES_4(String tablaMedidorEntrada_MES_4) {
        this.tablaMedidorEntrada_MES_4 = tablaMedidorEntrada_MES_4;
    }

    public String gettablaMedidorEntrada_MES_5() {
        return tablaMedidorEntrada_MES_5;
    }

    public void settablaMedidorEntrada_MES_5(String tablaMedidorEntrada_MES_5) {
        this.tablaMedidorEntrada_MES_5 = tablaMedidorEntrada_MES_5;
    }

    public String gettablaMedidorEntrada_VALOR_FACTURA_1() {
        return tablaMedidorEntrada_VALOR_FACTURA_1;
    }

    public void settablaMedidorEntrada_VALOR_FACTURA_1(String tablaMedidorEntrada_VALOR_FACTURA_1) {
        this.tablaMedidorEntrada_VALOR_FACTURA_1 = tablaMedidorEntrada_VALOR_FACTURA_1;
    }

    public String gettablaMedidorEntrada_VALOR_FACTURA_2() {
        return tablaMedidorEntrada_VALOR_FACTURA_2;
    }

    public void settablaMedidorEntrada_VALOR_FACTURA_2(String tablaMedidorEntrada_VALOR_FACTURA_2) {
        this.tablaMedidorEntrada_VALOR_FACTURA_2 = tablaMedidorEntrada_VALOR_FACTURA_2;
    }

    public String gettablaMedidorEntrada_VALOR_FACTURA_3() {
        return tablaMedidorEntrada_VALOR_FACTURA_3;
    }

    public void settablaMedidorEntrada_VALOR_FACTURA_3(String tablaMedidorEntrada_VALOR_FACTURA_3) {
        this.tablaMedidorEntrada_VALOR_FACTURA_3 = tablaMedidorEntrada_VALOR_FACTURA_3;
    }

    public String gettablaMedidorEntrada_VALOR_FACTURA_4() {
        return tablaMedidorEntrada_VALOR_FACTURA_4;
    }

    public void settablaMedidorEntrada_VALOR_FACTURA_4(String tablaMedidorEntrada_VALOR_FACTURA_4) {
        this.tablaMedidorEntrada_VALOR_FACTURA_4 = tablaMedidorEntrada_VALOR_FACTURA_4;
    }

    public String gettablaMedidorEntrada_VALOR_FACTURA_5() {
        return tablaMedidorEntrada_VALOR_FACTURA_5;
    }

    public void settablaMedidorEntrada_VALOR_FACTURA_5(String tablaMedidorEntrada_VALOR_FACTURA_5) {
        this.tablaMedidorEntrada_VALOR_FACTURA_5 = tablaMedidorEntrada_VALOR_FACTURA_5;
    }

    public String gettablaMedidorEntrada_SALDO_COVID() {
        return tablaMedidorEntrada_SALDO_COVID;
    }

    public void settablaMedidorEntrada_SALDO_COVID(String tablaMedidorEntrada_SALDO_COVID) {
        this.tablaMedidorEntrada_SALDO_COVID = tablaMedidorEntrada_SALDO_COVID;
    }

    public String gettablaMedidorEntrada_DEUDA_CASTIGADA() {
        return tablaMedidorEntrada_DEUDA_CASTIGADA;
    }

    public void settablaMedidorEntrada_DEUDA_CASTIGADA(String tablaMedidorEntrada_DEUDA_CASTIGADA) {
        this.tablaMedidorEntrada_DEUDA_CASTIGADA = tablaMedidorEntrada_DEUDA_CASTIGADA;
    }

    public String gettablaMedidorEntrada_DIFERIDO_COVID() {
        return tablaMedidorEntrada_DIFERIDO_COVID;
    }

    public void settablaMedidorEntrada_DIFERIDO_COVID(String tablaMedidorEntrada_DIFERIDO_COVID) {
        this.tablaMedidorEntrada_DIFERIDO_COVID = tablaMedidorEntrada_DIFERIDO_COVID;
    }

    public String gettablaMedidorEntrada_SALDO_NO_COVID() {
        return tablaMedidorEntrada_SALDO_NO_COVID;
    }

    public void settablaMedidorEntrada_SALDO_NO_COVID(String tablaMedidorEntrada_SALDO_NO_COVID) {
        this.tablaMedidorEntrada_SALDO_NO_COVID = tablaMedidorEntrada_SALDO_NO_COVID;
    }

    public String gettablaMedidorEntrada_RECLAMACION() {
        return tablaMedidorEntrada_RECLAMACION;
    }

    public void settablaMedidorEntrada_RECLAMACION(String tablaMedidorEntrada_RECLAMACION) {
        this.tablaMedidorEntrada_RECLAMACION = tablaMedidorEntrada_RECLAMACION;
    }

    public String gettablaMedidorEntrada_DIFERIDO_NO_COVID() {
        return tablaMedidorEntrada_DIFERIDO_NO_COVID;
    }

    public void settablaMedidorEntrada_DIFERIDO_NO_COVID(String tablaMedidorEntrada_DIFERIDO_NO_COVID) {
        this.tablaMedidorEntrada_DIFERIDO_NO_COVID = tablaMedidorEntrada_DIFERIDO_NO_COVID;
    }

    public String gettablaMedidorEntrada_NOTA1() {
        return tablaMedidorEntrada_NOTA1;
    }

    public void settablaMedidorEntrada_NOTA1(String tablaMedidorEntrada_NOTA1) {
        this.tablaMedidorEntrada_NOTA1 = tablaMedidorEntrada_NOTA1;
    }

    public String gettablaMedidorEntrada_NOTA2() {
        return tablaMedidorEntrada_NOTA2;
    }

    public void settablaMedidorEntrada_NOTA2(String tablaMedidorEntrada_NOTA2) {
        this.tablaMedidorEntrada_NOTA2 = tablaMedidorEntrada_NOTA2;
    }

    public String gettablaMedidorEntrada_MENSAJE() {
        return tablaMedidorEntrada_MENSAJE;
    }

    public void settablaMedidorEntrada_MENSAJE(String tablaMedidorEntrada_MENSAJE) {
        this.tablaMedidorEntrada_MENSAJE = tablaMedidorEntrada_MENSAJE;
    }

    public String gettablaMedidorEntrada_PPARTICIPACION() {
        return tablaMedidorEntrada_PPARTICIPACION;
    }

    public void settablaMedidorEntrada_PPARTICIPACION(String tablaMedidorEntrada_PPARTICIPACION) {
        this.tablaMedidorEntrada_PPARTICIPACION = tablaMedidorEntrada_PPARTICIPACION;
    }

    public String gettablaMedidorEntrada_CNVNIO1CNCPTO() {
        return tablaMedidorEntrada_CNVNIO1CNCPTO;
    }

    public void settablaMedidorEntrada_CNVNIO1CNCPTO(String tablaMedidorEntrada_CNVNIO1CNCPTO) {
        this.tablaMedidorEntrada_CNVNIO1CNCPTO = tablaMedidorEntrada_CNVNIO1CNCPTO;
    }

    public String gettablaMedidorEntrada_CNVNIO1TPOVLOR() {
        return tablaMedidorEntrada_CNVNIO1TPOVLOR;
    }

    public void settablaMedidorEntrada_CNVNIO1TPOVLOR(String tablaMedidorEntrada_CNVNIO1TPOVLOR) {
        this.tablaMedidorEntrada_CNVNIO1TPOVLOR = tablaMedidorEntrada_CNVNIO1TPOVLOR;
    }

    public String gettablaMedidorEntrada_CNVNIO1TPOPRCNTJE() {
        return tablaMedidorEntrada_CNVNIO1TPOPRCNTJE;
    }

    public void settablaMedidorEntrada_CNVNIO1TPOPRCNTJE(String tablaMedidorEntrada_CNVNIO1TPOPRCNTJE) {
        this.tablaMedidorEntrada_CNVNIO1TPOPRCNTJE = tablaMedidorEntrada_CNVNIO1TPOPRCNTJE;
    }

    public String gettablaMedidorEntrada_CNVNIO1VLOR() {
        return tablaMedidorEntrada_CNVNIO1VLOR;
    }

    public void settablaMedidorEntrada_CNVNIO1VLOR(String tablaMedidorEntrada_CNVNIO1VLOR) {
        this.tablaMedidorEntrada_CNVNIO1VLOR = tablaMedidorEntrada_CNVNIO1VLOR;
    }

    public String gettablaMedidorEntrada_CNVNIO1RNGOMNMO() {
        return tablaMedidorEntrada_CNVNIO1RNGOMNMO;
    }

    public void settablaMedidorEntrada_CNVNIO1RNGOMNMO(String tablaMedidorEntrada_CNVNIO1RNGOMNMO) {
        this.tablaMedidorEntrada_CNVNIO1RNGOMNMO = tablaMedidorEntrada_CNVNIO1RNGOMNMO;
    }

    public String gettablaMedidorEntrada_CNVNIO1RNGOMXMO() {
        return tablaMedidorEntrada_CNVNIO1RNGOMXMO;
    }

    public void settablaMedidorEntrada_CNVNIO1RNGOMXMO(String tablaMedidorEntrada_CNVNIO1RNGOMXMO) {
        this.tablaMedidorEntrada_CNVNIO1RNGOMXMO = tablaMedidorEntrada_CNVNIO1RNGOMXMO;
    }

    public String gettablaMedidorEntrada_CNVNIO1TPEMNMO() {
        return tablaMedidorEntrada_CNVNIO1TPEMNMO;
    }

    public void settablaMedidorEntrada_CNVNIO1TPEMNMO(String tablaMedidorEntrada_CNVNIO1TPEMNMO) {
        this.tablaMedidorEntrada_CNVNIO1TPEMNMO = tablaMedidorEntrada_CNVNIO1TPEMNMO;
    }

    public String gettablaMedidorEntrada_CNVNIO1TPEMXMO() {
        return tablaMedidorEntrada_CNVNIO1TPEMXMO;
    }

    public void settablaMedidorEntrada_CNVNIO1TPEMXMO(String tablaMedidorEntrada_CNVNIO1TPEMXMO) {
        this.tablaMedidorEntrada_CNVNIO1TPEMXMO = tablaMedidorEntrada_CNVNIO1TPEMXMO;
    }

    public String gettablaMedidorEntrada_CNVNIO2CNCPTO() {
        return tablaMedidorEntrada_CNVNIO2CNCPTO;
    }

    public void settablaMedidorEntrada_CNVNIO2CNCPTO(String tablaMedidorEntrada_CNVNIO2CNCPTO) {
        this.tablaMedidorEntrada_CNVNIO2CNCPTO = tablaMedidorEntrada_CNVNIO2CNCPTO;
    }

    public String gettablaMedidorEntrada_CNVNIO2TPOVLOR() {
        return tablaMedidorEntrada_CNVNIO2TPOVLOR;
    }

    public void settablaMedidorEntrada_CNVNIO2TPOVLOR(String tablaMedidorEntrada_CNVNIO2TPOVLOR) {
        this.tablaMedidorEntrada_CNVNIO2TPOVLOR = tablaMedidorEntrada_CNVNIO2TPOVLOR;
    }

    public String gettablaMedidorEntrada_CNVNIO2TPOPRCNTJE() {
        return tablaMedidorEntrada_CNVNIO2TPOPRCNTJE;
    }

    public void settablaMedidorEntrada_CNVNIO2TPOPRCNTJE(String tablaMedidorEntrada_CNVNIO2TPOPRCNTJE) {
        this.tablaMedidorEntrada_CNVNIO2TPOPRCNTJE = tablaMedidorEntrada_CNVNIO2TPOPRCNTJE;
    }

    public String gettablaMedidorEntrada_CNVNIO2VLOR() {
        return tablaMedidorEntrada_CNVNIO2VLOR;
    }

    public void settablaMedidorEntrada_CNVNIO2VLOR(String tablaMedidorEntrada_CNVNIO2VLOR) {
        this.tablaMedidorEntrada_CNVNIO2VLOR = tablaMedidorEntrada_CNVNIO2VLOR;
    }

    public String gettablaMedidorEntrada_CNVNIO2RNGOMNMO() {
        return tablaMedidorEntrada_CNVNIO2RNGOMNMO;
    }

    public void settablaMedidorEntrada_CNVNIO2RNGOMNMO(String tablaMedidorEntrada_CNVNIO2RNGOMNMO) {
        this.tablaMedidorEntrada_CNVNIO2RNGOMNMO = tablaMedidorEntrada_CNVNIO2RNGOMNMO;
    }

    public String gettablaMedidorEntrada_CNVNIO2RNGOMXMO() {
        return tablaMedidorEntrada_CNVNIO2RNGOMXMO;
    }

    public void settablaMedidorEntrada_CNVNIO2RNGOMXMO(String tablaMedidorEntrada_CNVNIO2RNGOMXMO) {
        this.tablaMedidorEntrada_CNVNIO2RNGOMXMO = tablaMedidorEntrada_CNVNIO2RNGOMXMO;
    }

    public String gettablaMedidorEntrada_CNVNIO2TPEMNMO() {
        return tablaMedidorEntrada_CNVNIO2TPEMNMO;
    }

    public void settablaMedidorEntrada_CNVNIO2TPEMNMO(String tablaMedidorEntrada_CNVNIO2TPEMNMO) {
        this.tablaMedidorEntrada_CNVNIO2TPEMNMO = tablaMedidorEntrada_CNVNIO2TPEMNMO;
    }

    public String gettablaMedidorEntrada_CNVNIO2TPEMXMO() {
        return tablaMedidorEntrada_CNVNIO2TPEMXMO;
    }

    public void settablaMedidorEntrada_CNVNIO2TPEMXMO(String tablaMedidorEntrada_CNVNIO2TPEMXMO) {
        this.tablaMedidorEntrada_CNVNIO2TPEMXMO = tablaMedidorEntrada_CNVNIO2TPEMXMO;
    }

    public String gettablaMedidorEntrada_CNVNIO3CNCPTO() {
        return tablaMedidorEntrada_CNVNIO3CNCPTO;
    }

    public void settablaMedidorEntrada_CNVNIO3CNCPTO(String tablaMedidorEntrada_CNVNIO3CNCPTO) {
        this.tablaMedidorEntrada_CNVNIO3CNCPTO = tablaMedidorEntrada_CNVNIO3CNCPTO;
    }

    public String gettablaMedidorEntrada_CNVNIO3TPOVLOR() {
        return tablaMedidorEntrada_CNVNIO3TPOVLOR;
    }

    public void settablaMedidorEntrada_CNVNIO3TPOVLOR(String tablaMedidorEntrada_CNVNIO3TPOVLOR) {
        this.tablaMedidorEntrada_CNVNIO3TPOVLOR = tablaMedidorEntrada_CNVNIO3TPOVLOR;
    }

    public String gettablaMedidorEntrada_CNVNIO3TPOPRCNTJE() {
        return tablaMedidorEntrada_CNVNIO3TPOPRCNTJE;
    }

    public void settablaMedidorEntrada_CNVNIO3TPOPRCNTJE(String tablaMedidorEntrada_CNVNIO3TPOPRCNTJE) {
        this.tablaMedidorEntrada_CNVNIO3TPOPRCNTJE = tablaMedidorEntrada_CNVNIO3TPOPRCNTJE;
    }

    public String gettablaMedidorEntrada_CNVNIO3VLOR() {
        return tablaMedidorEntrada_CNVNIO3VLOR;
    }

    public void settablaMedidorEntrada_CNVNIO3VLOR(String tablaMedidorEntrada_CNVNIO3VLOR) {
        this.tablaMedidorEntrada_CNVNIO3VLOR = tablaMedidorEntrada_CNVNIO3VLOR;
    }

    public String gettablaMedidorEntrada_CNVNIO3RNGOMNMO() {
        return tablaMedidorEntrada_CNVNIO3RNGOMNMO;
    }

    public void settablaMedidorEntrada_CNVNIO3RNGOMNMO(String tablaMedidorEntrada_CNVNIO3RNGOMNMO) {
        this.tablaMedidorEntrada_CNVNIO3RNGOMNMO = tablaMedidorEntrada_CNVNIO3RNGOMNMO;
    }

    public String gettablaMedidorEntrada_CNVNIO3RNGOMXMO() {
        return tablaMedidorEntrada_CNVNIO3RNGOMXMO;
    }

    public void settablaMedidorEntrada_CNVNIO3RNGOMXMO(String tablaMedidorEntrada_CNVNIO3RNGOMXMO) {
        this.tablaMedidorEntrada_CNVNIO3RNGOMXMO = tablaMedidorEntrada_CNVNIO3RNGOMXMO;
    }

    public String gettablaMedidorEntrada_CNVNIO3TPEMNMO() {
        return tablaMedidorEntrada_CNVNIO3TPEMNMO;
    }

    public void settablaMedidorEntrada_CNVNIO3TPEMNMO(String tablaMedidorEntrada_CNVNIO3TPEMNMO) {
        this.tablaMedidorEntrada_CNVNIO3TPEMNMO = tablaMedidorEntrada_CNVNIO3TPEMNMO;
    }

    public String gettablaMedidorEntrada_CNVNIO3TPEMXMO() {
        return tablaMedidorEntrada_CNVNIO3TPEMXMO;
    }

    public void settablaMedidorEntrada_CNVNIO3TPEMXMO(String tablaMedidorEntrada_CNVNIO3TPEMXMO) {
        this.tablaMedidorEntrada_CNVNIO3TPEMXMO = tablaMedidorEntrada_CNVNIO3TPEMXMO;
    }

    public Boolean abrir_TablaMedidorEntrada(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        setArchivo_TablaMedidorEntrada(nombreArchivo);
        return abrir();
    }

    private Boolean abrir() {
        try {
            int fileSize = (int) rFile.length();
            total_TablaMedidorEntrada = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaMedidorEntrada() {
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void posicion_TablaMedidorEntrada(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // IMPORTANTE: requiere haber llamado abrir_TablaMedidorEntrada() antes.
    public int lectura_TablaMedidorEntrada(int registro) {
        encontro_TablaMedidorEntrada = 0;
        posicion_TablaMedidorEntrada(registro, LONGITUD_REGISTRO);
        try {
            rFile.readFully(bufferRegistro);
            settablaMedidorEntrada_anio(new String(bufferRegistro, 0, 4, StandardCharsets.UTF_8));
            settablaMedidorEntrada_mes(new String(bufferRegistro, 5, 2, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Cuenta(new String(bufferRegistro, 8, 10, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Marca(new String(bufferRegistro, 19, 10, StandardCharsets.UTF_8));
            settablaMedidorEntrada_NUMero(new String(bufferRegistro, 30, 16, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Tipomedidor(new String(bufferRegistro, 47, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Indfacturacionpr(new String(bufferRegistro, 49, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Estadomedidor(new String(bufferRegistro, 51, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Fechalectanterior(new String(bufferRegistro, 53, 10, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Nroregistros(new String(bufferRegistro, 64, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_Fechamedcambiado(new String(bufferRegistro, 66, 8, StandardCharsets.UTF_8));
            settablaMedidorEntrada_primerregistro(new String(bufferRegistro, 75, 5, StandardCharsets.UTF_8));
            settablaMedidorEntrada_cliente(new String(bufferRegistro, 81, 5, StandardCharsets.UTF_8));
            settablaMedidorEntrada_idcortado(new String(bufferRegistro, 87, 8, StandardCharsets.UTF_8));
            settablaMedidorEntrada_lecturacorte(new String(bufferRegistro, 96, 8, StandardCharsets.UTF_8));
            settablaMedidorEntrada_obsercorte(new String(bufferRegistro, 105, 2, StandardCharsets.UTF_8));
            settablaMedidorEntrada_dpto(new String(bufferRegistro, 108, 2, StandardCharsets.UTF_8));
            settablaMedidorEntrada_municipio(new String(bufferRegistro, 111, 3, StandardCharsets.UTF_8));
            settablaMedidorEntrada_convenio(new String(bufferRegistro, 115, 40, StandardCharsets.UTF_8));
            settablaMedidorEntrada_ciclo(new String(bufferRegistro, 156, 2, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CUENTA_ASEO(new String(bufferRegistro, 159, 10, StandardCharsets.UTF_8));
            settablaMedidorEntrada_DIAS_LIQUIDADOS(new String(bufferRegistro, 170, 2, StandardCharsets.UTF_8));
            settablaMedidorEntrada_periodo(new String(bufferRegistro, 173, 23, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TIPO_PRODUCTOR(new String(bufferRegistro, 197, 50, StandardCharsets.UTF_8));
            settablaMedidorEntrada_estrato(new String(bufferRegistro, 248, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CVRNAR(new String(bufferRegistro, 250, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CVRNANR(new String(bufferRegistro, 258, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VBARAUA(new String(bufferRegistro, 266, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VBARAUNA(new String(bufferRegistro, 274, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_COSTO_FIJO(new String(bufferRegistro, 282, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VOLUMEN(new String(bufferRegistro, 290, 6, StandardCharsets.UTF_8));
            settablaMedidorEntrada_DENSIDAD(new String(bufferRegistro, 297, 6, StandardCharsets.UTF_8));
            settablaMedidorEntrada_URO(new String(bufferRegistro, 304, 3, StandardCharsets.UTF_8));
            settablaMedidorEntrada_UNRO(new String(bufferRegistro, 308, 3, StandardCharsets.UTF_8));
            settablaMedidorEntrada_URD(new String(bufferRegistro, 312, 3, StandardCharsets.UTF_8));
            settablaMedidorEntrada_UNRD(new String(bufferRegistro, 316, 3, StandardCharsets.UTF_8));
            settablaMedidorEntrada_contribucion(new String(bufferRegistro, 320, 6, StandardCharsets.UTF_8));
            settablaMedidorEntrada_subsidio(new String(bufferRegistro, 327, 6, StandardCharsets.UTF_8));
            settablaMedidorEntrada_BARRIDO(new String(bufferRegistro, 334, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_RECOLECCION(new String(bufferRegistro, 336, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_PODA(new String(bufferRegistro, 338, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CORTE_CESPED(new String(bufferRegistro, 340, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_ESCOMBROS(new String(bufferRegistro, 342, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRBL(new String(bufferRegistro, 344, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRLU(new String(bufferRegistro, 354, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRRA(new String(bufferRegistro, 364, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TAFA(new String(bufferRegistro, 374, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRA(new String(bufferRegistro, 384, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRNA(new String(bufferRegistro, 394, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TAFNA(new String(bufferRegistro, 404, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TTSM(new String(bufferRegistro, 414, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRBLM1(new String(bufferRegistro, 424, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRLUM1(new String(bufferRegistro, 434, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRRAM1(new String(bufferRegistro, 444, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TAFAM1(new String(bufferRegistro, 454, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRAM1(new String(bufferRegistro, 464, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRNAM1(new String(bufferRegistro, 474, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TAFNAM1(new String(bufferRegistro, 484, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TTSMM1(new String(bufferRegistro, 494, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRBLM2(new String(bufferRegistro, 504, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRLUM2(new String(bufferRegistro, 514, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRRAM2(new String(bufferRegistro, 524, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TAFAM2(new String(bufferRegistro, 534, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRAM2(new String(bufferRegistro, 544, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TRNAM2(new String(bufferRegistro, 554, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TAFNAM2(new String(bufferRegistro, 564, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_TTSMM2(new String(bufferRegistro, 574, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_MES_1(new String(bufferRegistro, 584, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_MES_2(new String(bufferRegistro, 594, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_MES_3(new String(bufferRegistro, 604, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_MES_4(new String(bufferRegistro, 614, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_MES_5(new String(bufferRegistro, 624, 9, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VALOR_FACTURA_1(new String(bufferRegistro, 634, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VALOR_FACTURA_2(new String(bufferRegistro, 642, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VALOR_FACTURA_3(new String(bufferRegistro, 650, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VALOR_FACTURA_4(new String(bufferRegistro, 658, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_VALOR_FACTURA_5(new String(bufferRegistro, 666, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_SALDO_COVID(new String(bufferRegistro, 674, 8, StandardCharsets.UTF_8));
            settablaMedidorEntrada_DEUDA_CASTIGADA(new String(bufferRegistro, 683, 8, StandardCharsets.UTF_8));
            settablaMedidorEntrada_DIFERIDO_COVID(new String(bufferRegistro, 692, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_SALDO_NO_COVID(new String(bufferRegistro, 700, 8, StandardCharsets.UTF_8));
            settablaMedidorEntrada_RECLAMACION(new String(bufferRegistro, 709, 8, StandardCharsets.UTF_8));
            settablaMedidorEntrada_DIFERIDO_NO_COVID(new String(bufferRegistro, 718, 7, StandardCharsets.UTF_8));
            settablaMedidorEntrada_NOTA1(new String(bufferRegistro, 726, 30, StandardCharsets.UTF_8));
            settablaMedidorEntrada_NOTA2(new String(bufferRegistro, 757, 60, StandardCharsets.UTF_8));
            settablaMedidorEntrada_MENSAJE(new String(bufferRegistro, 818, 60, StandardCharsets.UTF_8));
            settablaMedidorEntrada_PPARTICIPACION(new String(bufferRegistro, 879, 6, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1CNCPTO(new String(bufferRegistro, 886, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1TPOVLOR(new String(bufferRegistro, 898, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1TPOPRCNTJE(new String(bufferRegistro, 900, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1VLOR(new String(bufferRegistro, 902, 12, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1RNGOMNMO(new String(bufferRegistro, 915, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1RNGOMXMO(new String(bufferRegistro, 927, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1TPEMNMO(new String(bufferRegistro, 939, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO1TPEMXMO(new String(bufferRegistro, 951, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2CNCPTO(new String(bufferRegistro, 963, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2TPOVLOR(new String(bufferRegistro, 975, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2TPOPRCNTJE(new String(bufferRegistro, 977, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2VLOR(new String(bufferRegistro, 979, 12, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2RNGOMNMO(new String(bufferRegistro, 992, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2RNGOMXMO(new String(bufferRegistro, 1004, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2TPEMNMO(new String(bufferRegistro, 1016, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO2TPEMXMO(new String(bufferRegistro, 1028, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3CNCPTO(new String(bufferRegistro, 1040, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3TPOVLOR(new String(bufferRegistro, 1052, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3TPOPRCNTJE(new String(bufferRegistro, 1054, 1, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3VLOR(new String(bufferRegistro, 1056, 12, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3RNGOMNMO(new String(bufferRegistro, 1069, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3RNGOMXMO(new String(bufferRegistro, 1081, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3TPEMNMO(new String(bufferRegistro, 1093, 11, StandardCharsets.UTF_8));
            settablaMedidorEntrada_CNVNIO3TPEMXMO(new String(bufferRegistro, 1105, 11, StandardCharsets.UTF_8));
            ultimo_TablaMedidorEntrada = registro;
            // fin estructura
        } catch (Exception e) {
            e.printStackTrace();
            return -2;
        }
        return 1;
    }

    // Busqueda secuencial ORIGINAL (buscarSecuencialecontad), preservada tal cual:
    // campo=0 -> busca por NUMero quitando letras T/A/U/M y comparando el sufijo
    //            de la misma longitud que el codigo buscado.
    // campo<>0 -> busca por Cuenta, tomando el sustring(1, len(codigo)+1).
    // Se mantiene la comparacion con == y con indexOf tal como estaba en el
    // original -- no se corrige aqui para no cambiar el comportamiento actual.
    public void buscarSecuencialecontad(String codigo, int campo) {
        String codigobuscar = "";
        encontro_TablaMedidorEntrada = 0;

        for (int i = 0; i < total_TablaMedidorEntrada; i++) {
            if (lectura_TablaMedidorEntrada(i + 1) == -2) {
                continue;
            }
            if (campo == 0) {
                codigobuscar = tablaMedidorEntrada_NUMero.trim().replace("T", "");
                codigobuscar = codigobuscar.trim().replace("A", "");
                codigobuscar = codigobuscar.trim().replace("U", "");
                if (codigobuscar.length() >= codigo.trim().length()) {
                    codigobuscar = codigobuscar.substring(codigobuscar.length() - codigo.trim().length(), codigo.trim().length());
                }
                codigobuscar = codigobuscar.trim().replace("M", "");
            } else {
                codigobuscar = tablaMedidorEntrada_Cuenta.substring(1, codigo.trim().length() + 1);
            }

            if (codigo.trim() == codigobuscar || codigobuscar.indexOf(codigo) >= 0) {
                encontro_TablaMedidorEntrada = 1;
                posicion_TablaMedidorEntrada(i + 1, LONGITUD_REGISTRO);
                i = total_TablaMedidorEntrada + 1;
            } else {
                encontro_TablaMedidorEntrada = 0;
            }
        }
    }

    public String getArchivo_TablaMedidorEntrada() {
        return archivo_TablaMedidorEntrada;
    }

    public void setArchivo_TablaMedidorEntrada(String archivo_TablaMedidorEntrada) {
        this.archivo_TablaMedidorEntrada = archivo_TablaMedidorEntrada;
    }

    public int getTotal_TablaMedidorEntrada() {
        return total_TablaMedidorEntrada;
    }

    public void setTotal_TablaMedidorEntrada(int total_TablaMedidorEntrada) {
        this.total_TablaMedidorEntrada = total_TablaMedidorEntrada;
    }
}