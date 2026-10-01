package com.gselectroCaqueta.tablas;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
/// <summary>
/// Descripcion breve del archivo a generar...tablaEntradaClientes
/// Estructura nueva (2026), 130 campos -- ver generadorplanosclienteentrada.txt
/// y CLIENTE.TXT. Los nombres de campo que YA EXISTIAN en la version anterior
/// de esta clase se dejaron EXACTOS (misma mayuscula/minuscula) para no romper
/// el resto del codigo que ya los referencia.
///
/// PUNTOS A VERIFICAR (no se adivinaron, quedan marcados abajo en el codigo):
///  1) Posiciones 40-43 (antes 6 campos: Nrodemedidores, primermedidor,
///     nrodeconceptos, primerconcepto, nrocobros, primercobro -- ahora son 4).
///     Se reusaron los 4 nombres mas afines, pero la correspondencia exacta
///     de conceptos no es 100% segura.
///  2) Hay DOS pares de coordenadas en el plano nuevo (posicion 16-17 y
///     posicion 47-48). El campo viejo 'cordenadax'/'cordenaday' se dejo en
///     el segundo par (47-48, coincide con el comentario 'antes 878,893' del
///     codigo original). El primer par (16-17) es un campo NUEVO, nombrado
///     'cordenadaPredioX'/'cordenadaPredioY'. Confirmar si es al reves.
/// </summary>

public class TablaEntradaClientes { //Ax ya es: E_CLIENT

    // 130 campos + separadores = 1843 bytes de datos
    // + 2 bytes de fin de linea (CR LF) = 1846 bytes por registro.
    static final int LONGITUD_REGISTRO = 1847;

    String sep = "|";
    String tablaEntradaClientes_anio;  // ano_proceso
    String tablaEntradaClientes_mes;  // mes_proceso
    String tablaEntradaClientes_Cliente;  // cliente
    String tablaEntradaClientes_Ciclo;  // ciclo
    String tablaEntradaClientes_Departamento;  // departamento
    String tablaEntradaClientes_Municipio;  // municipio
    String tablaEntradaClientes_Ruta;  // ruta
    String tablaEntradaClientes_Cuenta;  // codcuenta
    String tablaEntradaClientes_Bimestral;  // ind_fact_bimestral
    String tablaEntradaClientes_Actividad;  // Actividad
    String tablaEntradaClientes_Estadodelservicio;  // estado_cliente
    String tablaEntradaClientes_Nrofactura;  // numero_factura
    String tablaEntradaClientes_Facturadomiciliada;  // tipo_facturacion(L/E/F)
    String tablaEntradaClientes_Nombre;  // nombre
    String tablaEntradaClientes_Direccion;  // direccion
    String tablaEntradaClientes_Informacionadicional;  // infor_adicional
    String tablaEntradaClientes_cordenadaPredioX;  // coordenadaXX -- NUEVO -- ver nota coordenadas
    String tablaEntradaClientes_cordenadaPredioY;  // CoordenadaYY -- NUEVO -- ver nota coordenadas
    String tablaEntradaClientes_DireccionPostal;  // direccion_postal
    String tablaEntradaClientes_Nitocedula;  // Cedula
    String tablaEntradaClientes_Estrato;  // estrato
    String tablaEntradaClientes_Clasedeservicio;  // clase_servicio -- ancho crecio de 2 a 24 (ahora texto completo)
    String tablaEntradaClientes_claseagrupacion;  // CLASE_AGRUPADORA
    String tablaEntradaClientes_Fechaultimopago;  // fecha_ult_pago
    String tablaEntradaClientes_Valorultimopago;  // valor_ult_pago
    String tablaEntradaClientes_tipoultimopago;  // tipo_ult_pago
    String tablaEntradaClientes_Lugarultimopago;  // lugar_ult_pago
    String tablaEntradaClientes_Fechaultimafactura;  // fecha_ult_factura
    String tablaEntradaClientes_diasfacturados;  // dias_facturados
    String tablaEntradaClientes_Descalculado;  // des_calculado
    String tablaEntradaClientes_Desmaximoadmisible;  // des_maximo
    String tablaEntradaClientes_Fescalculado;  // fes_calculado
    String tablaEntradaClientes_Fesmaximoadmisible;  // fes_maximo
    String tablaEntradaClientes_mesesatra;  // meses_atraso
    String tablaEntradaClientes_cargainstalada;  // carga_instalada
    String tablaEntradaClientes_Niveldetension;  // nivel_tension
    String tablaEntradaClientes_Nodoconexion;  // nodo_conexion
    String tablaEntradaClientes_Nodocircuito;  // nodo_circuito
    String tablaEntradaClientes_nombrecircuito;  // nombre_circuito
    String tablaEntradaClientes_dato;  // Factor
    String tablaEntradaClientes_Nrodemedidores;  // totalreg -- VERIFICAR: antes eran 6 campos (Nrodemedidores..primercobro), ahora son 4
    String tablaEntradaClientes_primermedidor;  // idmedidores(literal 00000) -- VERIFICAR: ver nota arriba
    String tablaEntradaClientes_nrocobros;  // Nro_cobros_salida -- VERIFICAR: ver nota arriba
    String tablaEntradaClientes_primercobro;  // Idconceptos(literal 00000) -- VERIFICAR: ver nota arriba
    String tablaEntradaClientes_consecutivo;  // consecutivo -- ancho crecio de 5 a 12
    String tablaEntradaClientes_fechavence;  // FECHA_VENCE -- ancho crecio de 10 a 12
    String tablaEntradaClientes_fechacorte;  // FECHA_CORTE -- ancho crecio de 10 a 15
    String tablaEntradaClientes_cordenadax;  // CORDENADAX -- NUEVO NOMBRE DE CAMPO (distinto al de la posicion 16) -- ver nota
    String tablaEntradaClientes_cordenaday;  // CORDENADAY -- NUEVO NOMBRE DE CAMPO (distinto al de la posicion 17) -- ver nota
    String tablaEntradaClientes_grupo;  // GRUPONODO
    String tablaEntradaClientes_consumoanioant;  // CONSUMOANIOANTERIOR
    String tablaEntradaClientes_sector;  // sector
    String tablaEntradaClientes_consumofoes;  // ConsumoFoes
    String tablaEntradaClientes_kwfoes;  // KwFoes
    String tablaEntradaClientes_valorfoes;  // ValorFoes
    String tablaEntradaClientes_facturafoes;  // FacturaFoes
    String tablaEntradaClientes_diasvencimiento;  // DiasVencimiento
    String tablaEntradaClientes_nrofamilias;  // NroFamilias
    String tablaEntradaClientes_CDGOMNSJE;  // CDGOMNSJE
    String tablaEntradaClientes_EMPLDO;  // EMPLDO
    String tablaEntradaClientes_CSTOG;  // CSTOG
    String tablaEntradaClientes_CSTOT;  // CSTOT
    String tablaEntradaClientes_CSTOPR;  // CSTOPR
    String tablaEntradaClientes_CSTOD;  // CSTOD
    String tablaEntradaClientes_CSTOR;  // CSTOR
    String tablaEntradaClientes_CSTOCV;  // CSTOCV
    String tablaEntradaClientes_CSTOCF;  // CSTOCF
    String tablaEntradaClientes_CU;  // CU
    String tablaEntradaClientes_FNNCCIONVLOR;  // FNNCCIONVLOR
    String tablaEntradaClientes_FNNCCIONCTAS;  // FNNCCIONCTAS
    String tablaEntradaClientes_FNNCCIONCTASPNDNTES;  // FNNCCIONCTASPNDNTES
    String tablaEntradaClientes_FNNCCIONSLDO;  // FNNCCIONSLDO
    String tablaEntradaClientes_FCHAINCIOAGNDA;  // FCHAINCIOAGNDA
    String tablaEntradaClientes_FCHAFINAGNDA;  // FCHAFINAGNDA
    String tablaEntradaClientes_LDO;  // LDO
    String tablaEntradaClientes_FCHARGTRALCTURA;  // FCHARGTRALCTURA
    String tablaEntradaClientes_CDGOATRZCION;  // CDGOATRZCION
    String tablaEntradaClientes_HDOP;  // HDOP
    String tablaEntradaClientes_USUARIO_PROCESO;  // USUARIO_PROCESO
    String tablaEntradaClientes_FECHA_PROCESO;  // FECHA_PROCESO
    String tablaEntradaClientes_PROCESADO;  // PROCESADO
    String tablaEntradaClientes_PRDOCMPNSCION;  // PRDOCMPNSCION
    String tablaEntradaClientes_CUAPLCBLE;  // CUAPLCBLE
    String tablaEntradaClientes_VLORKWFOES;  // VLORKWFOES
    String tablaEntradaClientes_TPECNSMOEMPLDO;  // TPECNSMOEMPLDO
    String tablaEntradaClientes_IDCRTCA;  // IDCRTCA
    String tablaEntradaClientes_CNTCTO;  // CNTCTO
    String tablaEntradaClientes_CNSMOMSAL;  // CNSMOMSAL
    String tablaEntradaClientes_CNSMOANOANT;  // CNSMOANOANT
    String tablaEntradaClientes_TPONTFCCION;  // TPONTFCCION
    String tablaEntradaClientes_OBLGAFTO;  // OBLGAFTO
    String tablaEntradaClientes_TTBSIVA;  // TTBSIVA
    String tablaEntradaClientes_MDLDDCBRO;  // MDLDDCBRO
    String tablaEntradaClientes_FECHA_AUDITORIA;  // FECHA_AUDITORIA
    String tablaEntradaClientes_MES_1;  // MES_1
    String tablaEntradaClientes_DIU_1;  // DIU_1
    String tablaEntradaClientes_DIUG_1;  // DIUG_1
    String tablaEntradaClientes_DIUM_1;  // DIUM_1
    String tablaEntradaClientes_FIU_1;  // FIU_1
    String tablaEntradaClientes_FIUG_1;  // FIUG_1
    String tablaEntradaClientes_FIUM_1;  // FIUM_1
    String tablaEntradaClientes_MES_2;  // MES_2
    String tablaEntradaClientes_DIU_2;  // DIU_2
    String tablaEntradaClientes_DIUG_2;  // DIUG_2
    String tablaEntradaClientes_DIUM_2;  // DIUM_2
    String tablaEntradaClientes_FIU_2;  // FIU_2
    String tablaEntradaClientes_FIUG_2;  // FIUG_2
    String tablaEntradaClientes_FIUM_2;  // FIUM_2
    String tablaEntradaClientes_MES_3;  // MES_3
    String tablaEntradaClientes_DIU_3;  // DIU_3
    String tablaEntradaClientes_DIUG_3;  // DIUG_3
    String tablaEntradaClientes_DIUM_3;  // DIUM_3
    String tablaEntradaClientes_FIU_3;  // FIU_3
    String tablaEntradaClientes_FIUG_3;  // FIUG_3
    String tablaEntradaClientes_FIUM_3;  // FIUM_3
    String tablaEntradaClientes_DIU_PERIODO;  // DIU_PERIODO
    String tablaEntradaClientes_TOTAL_DIUM;  // TOTAL_DIUM
    String tablaEntradaClientes_EXCLUIDAS;  // EXCLUIDAS
    String tablaEntradaClientes_MC;  // MC
    String tablaEntradaClientes_MF;  // MF
    String tablaEntradaClientes_COSTO_D;  // COSTO_D
    String tablaEntradaClientes_THC;  // THC
    String tablaEntradaClientes_TVC;  // TVC
    String tablaEntradaClientes_CEC;  // CEC
    String tablaEntradaClientes_VCD;  // VCD
    String tablaEntradaClientes_VCF;  // VCF
    String tablaEntradaClientes_MDDOR1SBSDIOCNTRBCION;  // MDDOR1SBSDIOCNTRBCION
    String tablaEntradaClientes_MDDOR1VLORKWH;  // MDDOR1VLORKWH
    String tablaEntradaClientes_MDDOR2SBSDIOCNTRBCION;  // MDDOR2SBSDIOCNTRBCION
    String tablaEntradaClientes_MDDOR2VLORKWH;  // MDDOR2VLORKWH

    // Buffer reusable: se reserva UNA sola vez y se sobreescribe en cada
    // lectura, en vez de crear un byte[] nuevo por cada registro leido.
    private final byte[] bufferRegistro = new byte[LONGITUD_REGISTRO];
    int total_TablaEntradaClientes;
    int ultimo_TablaEntradaClientes;
    int encontro_TablaEntradaClientes;
    RandomAccessFile rFile;
    private String archivo_TablaEntradaClientes;

    public String gettablaEntradaClientes_anio() {
        return tablaEntradaClientes_anio;
    }

    public void settablaEntradaClientes_anio(String tablaEntradaClientes_anio) {
        this.tablaEntradaClientes_anio = tablaEntradaClientes_anio;
    }

    public String gettablaEntradaClientes_mes() {
        return tablaEntradaClientes_mes;
    }

    public void settablaEntradaClientes_mes(String tablaEntradaClientes_mes) {
        this.tablaEntradaClientes_mes = tablaEntradaClientes_mes;
    }

    public String gettablaEntradaClientes_Cliente() {
        return tablaEntradaClientes_Cliente;
    }

    public void settablaEntradaClientes_Cliente(String tablaEntradaClientes_Cliente) {
        this.tablaEntradaClientes_Cliente = tablaEntradaClientes_Cliente;
    }

    public String gettablaEntradaClientes_Ciclo() {
        return tablaEntradaClientes_Ciclo;
    }

    public void settablaEntradaClientes_Ciclo(String tablaEntradaClientes_Ciclo) {
        this.tablaEntradaClientes_Ciclo = tablaEntradaClientes_Ciclo;
    }

    public String gettablaEntradaClientes_Departamento() {
        return tablaEntradaClientes_Departamento;
    }

    public void settablaEntradaClientes_Departamento(String tablaEntradaClientes_Departamento) {
        this.tablaEntradaClientes_Departamento = tablaEntradaClientes_Departamento;
    }

    public String gettablaEntradaClientes_Municipio() {
        return tablaEntradaClientes_Municipio;
    }

    public void settablaEntradaClientes_Municipio(String tablaEntradaClientes_Municipio) {
        this.tablaEntradaClientes_Municipio = tablaEntradaClientes_Municipio;
    }

    public String gettablaEntradaClientes_Ruta() {
        return tablaEntradaClientes_Ruta;
    }

    public void settablaEntradaClientes_Ruta(String tablaEntradaClientes_Ruta) {
        this.tablaEntradaClientes_Ruta = tablaEntradaClientes_Ruta;
    }

    public String gettablaEntradaClientes_Cuenta() {
        return tablaEntradaClientes_Cuenta;
    }

    public void settablaEntradaClientes_Cuenta(String tablaEntradaClientes_Cuenta) {
        this.tablaEntradaClientes_Cuenta = tablaEntradaClientes_Cuenta;
    }

    public String gettablaEntradaClientes_Bimestral() {
        return tablaEntradaClientes_Bimestral;
    }

    public void settablaEntradaClientes_Bimestral(String tablaEntradaClientes_Bimestral) {
        this.tablaEntradaClientes_Bimestral = tablaEntradaClientes_Bimestral;
    }

    public String gettablaEntradaClientes_Actividad() {
        return tablaEntradaClientes_Actividad;
    }

    public void settablaEntradaClientes_Actividad(String tablaEntradaClientes_Actividad) {
        this.tablaEntradaClientes_Actividad = tablaEntradaClientes_Actividad;
    }

    public String gettablaEntradaClientes_Estadodelservicio() {
        return tablaEntradaClientes_Estadodelservicio;
    }

    public void settablaEntradaClientes_Estadodelservicio(String tablaEntradaClientes_Estadodelservicio) {
        this.tablaEntradaClientes_Estadodelservicio = tablaEntradaClientes_Estadodelservicio;
    }

    public String gettablaEntradaClientes_Nrofactura() {
        return tablaEntradaClientes_Nrofactura;
    }

    public void settablaEntradaClientes_Nrofactura(String tablaEntradaClientes_Nrofactura) {
        this.tablaEntradaClientes_Nrofactura = tablaEntradaClientes_Nrofactura;
    }

    public String gettablaEntradaClientes_Facturadomiciliada() {
        return tablaEntradaClientes_Facturadomiciliada;
    }

    public void settablaEntradaClientes_Facturadomiciliada(String tablaEntradaClientes_Facturadomiciliada) {
        this.tablaEntradaClientes_Facturadomiciliada = tablaEntradaClientes_Facturadomiciliada;
    }

    public String gettablaEntradaClientes_Nombre() {
        return tablaEntradaClientes_Nombre;
    }

    public void settablaEntradaClientes_Nombre(String tablaEntradaClientes_Nombre) {
        this.tablaEntradaClientes_Nombre = tablaEntradaClientes_Nombre;
    }

    public String gettablaEntradaClientes_Direccion() {
        return tablaEntradaClientes_Direccion;
    }

    public void settablaEntradaClientes_Direccion(String tablaEntradaClientes_Direccion) {
        this.tablaEntradaClientes_Direccion = tablaEntradaClientes_Direccion;
    }

    public String gettablaEntradaClientes_Informacionadicional() {
        return tablaEntradaClientes_Informacionadicional;
    }

    public void settablaEntradaClientes_Informacionadicional(String tablaEntradaClientes_Informacionadicional) {
        this.tablaEntradaClientes_Informacionadicional = tablaEntradaClientes_Informacionadicional;
    }

    public String gettablaEntradaClientes_cordenadaPredioX() {
        return tablaEntradaClientes_cordenadaPredioX;
    }

    public void settablaEntradaClientes_cordenadaPredioX(String tablaEntradaClientes_cordenadaPredioX) {
        this.tablaEntradaClientes_cordenadaPredioX = tablaEntradaClientes_cordenadaPredioX;
    }

    public String gettablaEntradaClientes_cordenadaPredioY() {
        return tablaEntradaClientes_cordenadaPredioY;
    }

    public void settablaEntradaClientes_cordenadaPredioY(String tablaEntradaClientes_cordenadaPredioY) {
        this.tablaEntradaClientes_cordenadaPredioY = tablaEntradaClientes_cordenadaPredioY;
    }

    public String gettablaEntradaClientes_DireccionPostal() {
        return tablaEntradaClientes_DireccionPostal;
    }

    public void settablaEntradaClientes_DireccionPostal(String tablaEntradaClientes_DireccionPostal) {
        this.tablaEntradaClientes_DireccionPostal = tablaEntradaClientes_DireccionPostal;
    }

    public String gettablaEntradaClientes_Nitocedula() {
        return tablaEntradaClientes_Nitocedula;
    }

    public void settablaEntradaClientes_Nitocedula(String tablaEntradaClientes_Nitocedula) {
        this.tablaEntradaClientes_Nitocedula = tablaEntradaClientes_Nitocedula;
    }

    public String gettablaEntradaClientes_Estrato() {
        return tablaEntradaClientes_Estrato;
    }

    public void settablaEntradaClientes_Estrato(String tablaEntradaClientes_Estrato) {
        this.tablaEntradaClientes_Estrato = tablaEntradaClientes_Estrato;
    }

    public String gettablaEntradaClientes_Clasedeservicio() {
        return tablaEntradaClientes_Clasedeservicio;
    }

    public void settablaEntradaClientes_Clasedeservicio(String tablaEntradaClientes_Clasedeservicio) {
        this.tablaEntradaClientes_Clasedeservicio = tablaEntradaClientes_Clasedeservicio;
    }

    public String gettablaEntradaClientes_claseagrupacion() {
        return tablaEntradaClientes_claseagrupacion;
    }

    public void settablaEntradaClientes_claseagrupacion(String tablaEntradaClientes_claseagrupacion) {
        this.tablaEntradaClientes_claseagrupacion = tablaEntradaClientes_claseagrupacion;
    }

    public String gettablaEntradaClientes_Fechaultimopago() {
        return tablaEntradaClientes_Fechaultimopago;
    }

    public void settablaEntradaClientes_Fechaultimopago(String tablaEntradaClientes_Fechaultimopago) {
        this.tablaEntradaClientes_Fechaultimopago = tablaEntradaClientes_Fechaultimopago;
    }

    public String gettablaEntradaClientes_Valorultimopago() {
        return tablaEntradaClientes_Valorultimopago;
    }

    public void settablaEntradaClientes_Valorultimopago(String tablaEntradaClientes_Valorultimopago) {
        this.tablaEntradaClientes_Valorultimopago = tablaEntradaClientes_Valorultimopago;
    }

    public String gettablaEntradaClientes_tipoultimopago() {
        return tablaEntradaClientes_tipoultimopago;
    }

    public void settablaEntradaClientes_tipoultimopago(String tablaEntradaClientes_tipoultimopago) {
        this.tablaEntradaClientes_tipoultimopago = tablaEntradaClientes_tipoultimopago;
    }

    public String gettablaEntradaClientes_Lugarultimopago() {
        return tablaEntradaClientes_Lugarultimopago;
    }

    public void settablaEntradaClientes_Lugarultimopago(String tablaEntradaClientes_Lugarultimopago) {
        this.tablaEntradaClientes_Lugarultimopago = tablaEntradaClientes_Lugarultimopago;
    }

    public String gettablaEntradaClientes_Fechaultimafactura() {
        return tablaEntradaClientes_Fechaultimafactura;
    }

    public void settablaEntradaClientes_Fechaultimafactura(String tablaEntradaClientes_Fechaultimafactura) {
        this.tablaEntradaClientes_Fechaultimafactura = tablaEntradaClientes_Fechaultimafactura;
    }

    public String gettablaEntradaClientes_diasfacturados() {
        return tablaEntradaClientes_diasfacturados;
    }

    public void settablaEntradaClientes_diasfacturados(String tablaEntradaClientes_diasfacturados) {
        this.tablaEntradaClientes_diasfacturados = tablaEntradaClientes_diasfacturados;
    }

    public String gettablaEntradaClientes_Descalculado() {
        return tablaEntradaClientes_Descalculado;
    }

    public void settablaEntradaClientes_Descalculado(String tablaEntradaClientes_Descalculado) {
        this.tablaEntradaClientes_Descalculado = tablaEntradaClientes_Descalculado;
    }

    public String gettablaEntradaClientes_Desmaximoadmisible() {
        return tablaEntradaClientes_Desmaximoadmisible;
    }

    public void settablaEntradaClientes_Desmaximoadmisible(String tablaEntradaClientes_Desmaximoadmisible) {
        this.tablaEntradaClientes_Desmaximoadmisible = tablaEntradaClientes_Desmaximoadmisible;
    }

    public String gettablaEntradaClientes_Fescalculado() {
        return tablaEntradaClientes_Fescalculado;
    }

    public void settablaEntradaClientes_Fescalculado(String tablaEntradaClientes_Fescalculado) {
        this.tablaEntradaClientes_Fescalculado = tablaEntradaClientes_Fescalculado;
    }

    public String gettablaEntradaClientes_Fesmaximoadmisible() {
        return tablaEntradaClientes_Fesmaximoadmisible;
    }

    public void settablaEntradaClientes_Fesmaximoadmisible(String tablaEntradaClientes_Fesmaximoadmisible) {
        this.tablaEntradaClientes_Fesmaximoadmisible = tablaEntradaClientes_Fesmaximoadmisible;
    }

    public String gettablaEntradaClientes_mesesatra() {
        return tablaEntradaClientes_mesesatra;
    }

    public void settablaEntradaClientes_mesesatra(String tablaEntradaClientes_mesesatra) {
        this.tablaEntradaClientes_mesesatra = tablaEntradaClientes_mesesatra;
    }

    public String gettablaEntradaClientes_cargainstalada() {
        return tablaEntradaClientes_cargainstalada;
    }

    public void settablaEntradaClientes_cargainstalada(String tablaEntradaClientes_cargainstalada) {
        this.tablaEntradaClientes_cargainstalada = tablaEntradaClientes_cargainstalada;
    }

    public String gettablaEntradaClientes_Niveldetension() {
        return tablaEntradaClientes_Niveldetension;
    }

    public void settablaEntradaClientes_Niveldetension(String tablaEntradaClientes_Niveldetension) {
        this.tablaEntradaClientes_Niveldetension = tablaEntradaClientes_Niveldetension;
    }

    public String gettablaEntradaClientes_Nodoconexion() {
        return tablaEntradaClientes_Nodoconexion;
    }

    public void settablaEntradaClientes_Nodoconexion(String tablaEntradaClientes_Nodoconexion) {
        this.tablaEntradaClientes_Nodoconexion = tablaEntradaClientes_Nodoconexion;
    }

    public String gettablaEntradaClientes_Nodocircuito() {
        return tablaEntradaClientes_Nodocircuito;
    }

    public void settablaEntradaClientes_Nodocircuito(String tablaEntradaClientes_Nodocircuito) {
        this.tablaEntradaClientes_Nodocircuito = tablaEntradaClientes_Nodocircuito;
    }

    public String gettablaEntradaClientes_nombrecircuito() {
        return tablaEntradaClientes_nombrecircuito;
    }

    public void settablaEntradaClientes_nombrecircuito(String tablaEntradaClientes_nombrecircuito) {
        this.tablaEntradaClientes_nombrecircuito = tablaEntradaClientes_nombrecircuito;
    }

    public String gettablaEntradaClientes_dato() {
        return tablaEntradaClientes_dato;
    }

    public void settablaEntradaClientes_dato(String tablaEntradaClientes_dato) {
        this.tablaEntradaClientes_dato = tablaEntradaClientes_dato;
    }

    public String gettablaEntradaClientes_Nrodemedidores() {
        return tablaEntradaClientes_Nrodemedidores;
    }

    public void settablaEntradaClientes_Nrodemedidores(String tablaEntradaClientes_Nrodemedidores) {
        this.tablaEntradaClientes_Nrodemedidores = tablaEntradaClientes_Nrodemedidores;
    }

    public String gettablaEntradaClientes_primermedidor() {
        return tablaEntradaClientes_primermedidor;
    }

    public void settablaEntradaClientes_primermedidor(String tablaEntradaClientes_primermedidor) {
        this.tablaEntradaClientes_primermedidor = tablaEntradaClientes_primermedidor;
    }

    public String gettablaEntradaClientes_nrocobros() {
        return tablaEntradaClientes_nrocobros;
    }

    public void settablaEntradaClientes_nrocobros(String tablaEntradaClientes_nrocobros) {
        this.tablaEntradaClientes_nrocobros = tablaEntradaClientes_nrocobros;
    }

    public String gettablaEntradaClientes_primercobro() {
        return tablaEntradaClientes_primercobro;
    }

    public void settablaEntradaClientes_primercobro(String tablaEntradaClientes_primercobro) {
        this.tablaEntradaClientes_primercobro = tablaEntradaClientes_primercobro;
    }

    public String gettablaEntradaClientes_consecutivo() {
        return tablaEntradaClientes_consecutivo;
    }

    public void settablaEntradaClientes_consecutivo(String tablaEntradaClientes_consecutivo) {
        this.tablaEntradaClientes_consecutivo = tablaEntradaClientes_consecutivo;
    }

    public String gettablaEntradaClientes_fechavence() {
        return tablaEntradaClientes_fechavence;
    }

    public void settablaEntradaClientes_fechavence(String tablaEntradaClientes_fechavence) {
        this.tablaEntradaClientes_fechavence = tablaEntradaClientes_fechavence;
    }

    public String gettablaEntradaClientes_fechacorte() {
        return tablaEntradaClientes_fechacorte;
    }

    public void settablaEntradaClientes_fechacorte(String tablaEntradaClientes_fechacorte) {
        this.tablaEntradaClientes_fechacorte = tablaEntradaClientes_fechacorte;
    }

    public String gettablaEntradaClientes_cordenadax() {
        return tablaEntradaClientes_cordenadax;
    }

    public void settablaEntradaClientes_cordenadax(String tablaEntradaClientes_cordenadax) {
        this.tablaEntradaClientes_cordenadax = tablaEntradaClientes_cordenadax;
    }

    public String gettablaEntradaClientes_cordenaday() {
        return tablaEntradaClientes_cordenaday;
    }

    public void settablaEntradaClientes_cordenaday(String tablaEntradaClientes_cordenaday) {
        this.tablaEntradaClientes_cordenaday = tablaEntradaClientes_cordenaday;
    }

    public String gettablaEntradaClientes_grupo() {
        return tablaEntradaClientes_grupo;
    }

    public void settablaEntradaClientes_grupo(String tablaEntradaClientes_grupo) {
        this.tablaEntradaClientes_grupo = tablaEntradaClientes_grupo;
    }

    public String gettablaEntradaClientes_consumoanioant() {
        return tablaEntradaClientes_consumoanioant;
    }

    public void settablaEntradaClientes_consumoanioant(String tablaEntradaClientes_consumoanioant) {
        this.tablaEntradaClientes_consumoanioant = tablaEntradaClientes_consumoanioant;
    }

    public String gettablaEntradaClientes_sector() {
        return tablaEntradaClientes_sector;
    }

    public void settablaEntradaClientes_sector(String tablaEntradaClientes_sector) {
        this.tablaEntradaClientes_sector = tablaEntradaClientes_sector;
    }

    public String gettablaEntradaClientes_consumofoes() {
        return tablaEntradaClientes_consumofoes;
    }

    public void settablaEntradaClientes_consumofoes(String tablaEntradaClientes_consumofoes) {
        this.tablaEntradaClientes_consumofoes = tablaEntradaClientes_consumofoes;
    }

    public String gettablaEntradaClientes_kwfoes() {
        return tablaEntradaClientes_kwfoes;
    }

    public void settablaEntradaClientes_kwfoes(String tablaEntradaClientes_kwfoes) {
        this.tablaEntradaClientes_kwfoes = tablaEntradaClientes_kwfoes;
    }

    public String gettablaEntradaClientes_valorfoes() {
        return tablaEntradaClientes_valorfoes;
    }

    public void settablaEntradaClientes_valorfoes(String tablaEntradaClientes_valorfoes) {
        this.tablaEntradaClientes_valorfoes = tablaEntradaClientes_valorfoes;
    }

    public String gettablaEntradaClientes_facturafoes() {
        return tablaEntradaClientes_facturafoes;
    }

    public void settablaEntradaClientes_facturafoes(String tablaEntradaClientes_facturafoes) {
        this.tablaEntradaClientes_facturafoes = tablaEntradaClientes_facturafoes;
    }

    public String gettablaEntradaClientes_diasvencimiento() {
        return tablaEntradaClientes_diasvencimiento;
    }

    public void settablaEntradaClientes_diasvencimiento(String tablaEntradaClientes_diasvencimiento) {
        this.tablaEntradaClientes_diasvencimiento = tablaEntradaClientes_diasvencimiento;
    }

    public String gettablaEntradaClientes_nrofamilias() {
        return tablaEntradaClientes_nrofamilias;
    }

    public void settablaEntradaClientes_nrofamilias(String tablaEntradaClientes_nrofamilias) {
        this.tablaEntradaClientes_nrofamilias = tablaEntradaClientes_nrofamilias;
    }

    public String gettablaEntradaClientes_CDGOMNSJE() {
        return tablaEntradaClientes_CDGOMNSJE;
    }

    public void settablaEntradaClientes_CDGOMNSJE(String tablaEntradaClientes_CDGOMNSJE) {
        this.tablaEntradaClientes_CDGOMNSJE = tablaEntradaClientes_CDGOMNSJE;
    }

    public String gettablaEntradaClientes_EMPLDO() {
        return tablaEntradaClientes_EMPLDO;
    }

    public void settablaEntradaClientes_EMPLDO(String tablaEntradaClientes_EMPLDO) {
        this.tablaEntradaClientes_EMPLDO = tablaEntradaClientes_EMPLDO;
    }

    public String gettablaEntradaClientes_CSTOG() {
        return tablaEntradaClientes_CSTOG;
    }

    public void settablaEntradaClientes_CSTOG(String tablaEntradaClientes_CSTOG) {
        this.tablaEntradaClientes_CSTOG = tablaEntradaClientes_CSTOG;
    }

    public String gettablaEntradaClientes_CSTOT() {
        return tablaEntradaClientes_CSTOT;
    }

    public void settablaEntradaClientes_CSTOT(String tablaEntradaClientes_CSTOT) {
        this.tablaEntradaClientes_CSTOT = tablaEntradaClientes_CSTOT;
    }

    public String gettablaEntradaClientes_CSTOPR() {
        return tablaEntradaClientes_CSTOPR;
    }

    public void settablaEntradaClientes_CSTOPR(String tablaEntradaClientes_CSTOPR) {
        this.tablaEntradaClientes_CSTOPR = tablaEntradaClientes_CSTOPR;
    }

    public String gettablaEntradaClientes_CSTOD() {
        return tablaEntradaClientes_CSTOD;
    }

    public void settablaEntradaClientes_CSTOD(String tablaEntradaClientes_CSTOD) {
        this.tablaEntradaClientes_CSTOD = tablaEntradaClientes_CSTOD;
    }

    public String gettablaEntradaClientes_CSTOR() {
        return tablaEntradaClientes_CSTOR;
    }

    public void settablaEntradaClientes_CSTOR(String tablaEntradaClientes_CSTOR) {
        this.tablaEntradaClientes_CSTOR = tablaEntradaClientes_CSTOR;
    }

    public String gettablaEntradaClientes_CSTOCV() {
        return tablaEntradaClientes_CSTOCV;
    }

    public void settablaEntradaClientes_CSTOCV(String tablaEntradaClientes_CSTOCV) {
        this.tablaEntradaClientes_CSTOCV = tablaEntradaClientes_CSTOCV;
    }

    public String gettablaEntradaClientes_CSTOCF() {
        return tablaEntradaClientes_CSTOCF;
    }

    public void settablaEntradaClientes_CSTOCF(String tablaEntradaClientes_CSTOCF) {
        this.tablaEntradaClientes_CSTOCF = tablaEntradaClientes_CSTOCF;
    }

    public String gettablaEntradaClientes_CU() {
        return tablaEntradaClientes_CU;
    }

    public void settablaEntradaClientes_CU(String tablaEntradaClientes_CU) {
        this.tablaEntradaClientes_CU = tablaEntradaClientes_CU;
    }

    public String gettablaEntradaClientes_FNNCCIONVLOR() {
        return tablaEntradaClientes_FNNCCIONVLOR;
    }

    public void settablaEntradaClientes_FNNCCIONVLOR(String tablaEntradaClientes_FNNCCIONVLOR) {
        this.tablaEntradaClientes_FNNCCIONVLOR = tablaEntradaClientes_FNNCCIONVLOR;
    }

    public String gettablaEntradaClientes_FNNCCIONCTAS() {
        return tablaEntradaClientes_FNNCCIONCTAS;
    }

    public void settablaEntradaClientes_FNNCCIONCTAS(String tablaEntradaClientes_FNNCCIONCTAS) {
        this.tablaEntradaClientes_FNNCCIONCTAS = tablaEntradaClientes_FNNCCIONCTAS;
    }

    public String gettablaEntradaClientes_FNNCCIONCTASPNDNTES() {
        return tablaEntradaClientes_FNNCCIONCTASPNDNTES;
    }

    public void settablaEntradaClientes_FNNCCIONCTASPNDNTES(String tablaEntradaClientes_FNNCCIONCTASPNDNTES) {
        this.tablaEntradaClientes_FNNCCIONCTASPNDNTES = tablaEntradaClientes_FNNCCIONCTASPNDNTES;
    }

    public String gettablaEntradaClientes_FNNCCIONSLDO() {
        return tablaEntradaClientes_FNNCCIONSLDO;
    }

    public void settablaEntradaClientes_FNNCCIONSLDO(String tablaEntradaClientes_FNNCCIONSLDO) {
        this.tablaEntradaClientes_FNNCCIONSLDO = tablaEntradaClientes_FNNCCIONSLDO;
    }

    public String gettablaEntradaClientes_FCHAINCIOAGNDA() {
        return tablaEntradaClientes_FCHAINCIOAGNDA;
    }

    public void settablaEntradaClientes_FCHAINCIOAGNDA(String tablaEntradaClientes_FCHAINCIOAGNDA) {
        this.tablaEntradaClientes_FCHAINCIOAGNDA = tablaEntradaClientes_FCHAINCIOAGNDA;
    }

    public String gettablaEntradaClientes_FCHAFINAGNDA() {
        return tablaEntradaClientes_FCHAFINAGNDA;
    }

    public void settablaEntradaClientes_FCHAFINAGNDA(String tablaEntradaClientes_FCHAFINAGNDA) {
        this.tablaEntradaClientes_FCHAFINAGNDA = tablaEntradaClientes_FCHAFINAGNDA;
    }

    public String gettablaEntradaClientes_LDO() {
        return tablaEntradaClientes_LDO;
    }

    public void settablaEntradaClientes_LDO(String tablaEntradaClientes_LDO) {
        this.tablaEntradaClientes_LDO = tablaEntradaClientes_LDO;
    }

    public String gettablaEntradaClientes_FCHARGTRALCTURA() {
        return tablaEntradaClientes_FCHARGTRALCTURA;
    }

    public void settablaEntradaClientes_FCHARGTRALCTURA(String tablaEntradaClientes_FCHARGTRALCTURA) {
        this.tablaEntradaClientes_FCHARGTRALCTURA = tablaEntradaClientes_FCHARGTRALCTURA;
    }

    public String gettablaEntradaClientes_CDGOATRZCION() {
        return tablaEntradaClientes_CDGOATRZCION;
    }

    public void settablaEntradaClientes_CDGOATRZCION(String tablaEntradaClientes_CDGOATRZCION) {
        this.tablaEntradaClientes_CDGOATRZCION = tablaEntradaClientes_CDGOATRZCION;
    }

    public String gettablaEntradaClientes_HDOP() {
        return tablaEntradaClientes_HDOP;
    }

    public void settablaEntradaClientes_HDOP(String tablaEntradaClientes_HDOP) {
        this.tablaEntradaClientes_HDOP = tablaEntradaClientes_HDOP;
    }

    public String gettablaEntradaClientes_USUARIO_PROCESO() {
        return tablaEntradaClientes_USUARIO_PROCESO;
    }

    public void settablaEntradaClientes_USUARIO_PROCESO(String tablaEntradaClientes_USUARIO_PROCESO) {
        this.tablaEntradaClientes_USUARIO_PROCESO = tablaEntradaClientes_USUARIO_PROCESO;
    }

    public String gettablaEntradaClientes_FECHA_PROCESO() {
        return tablaEntradaClientes_FECHA_PROCESO;
    }

    public void settablaEntradaClientes_FECHA_PROCESO(String tablaEntradaClientes_FECHA_PROCESO) {
        this.tablaEntradaClientes_FECHA_PROCESO = tablaEntradaClientes_FECHA_PROCESO;
    }

    public String gettablaEntradaClientes_PROCESADO() {
        return tablaEntradaClientes_PROCESADO;
    }

    public void settablaEntradaClientes_PROCESADO(String tablaEntradaClientes_PROCESADO) {
        this.tablaEntradaClientes_PROCESADO = tablaEntradaClientes_PROCESADO;
    }

    public String gettablaEntradaClientes_PRDOCMPNSCION() {
        return tablaEntradaClientes_PRDOCMPNSCION;
    }

    public void settablaEntradaClientes_PRDOCMPNSCION(String tablaEntradaClientes_PRDOCMPNSCION) {
        this.tablaEntradaClientes_PRDOCMPNSCION = tablaEntradaClientes_PRDOCMPNSCION;
    }

    public String gettablaEntradaClientes_CUAPLCBLE() {
        return tablaEntradaClientes_CUAPLCBLE;
    }

    public void settablaEntradaClientes_CUAPLCBLE(String tablaEntradaClientes_CUAPLCBLE) {
        this.tablaEntradaClientes_CUAPLCBLE = tablaEntradaClientes_CUAPLCBLE;
    }

    public String gettablaEntradaClientes_VLORKWFOES() {
        return tablaEntradaClientes_VLORKWFOES;
    }

    public void settablaEntradaClientes_VLORKWFOES(String tablaEntradaClientes_VLORKWFOES) {
        this.tablaEntradaClientes_VLORKWFOES = tablaEntradaClientes_VLORKWFOES;
    }

    public String gettablaEntradaClientes_TPECNSMOEMPLDO() {
        return tablaEntradaClientes_TPECNSMOEMPLDO;
    }

    public void settablaEntradaClientes_TPECNSMOEMPLDO(String tablaEntradaClientes_TPECNSMOEMPLDO) {
        this.tablaEntradaClientes_TPECNSMOEMPLDO = tablaEntradaClientes_TPECNSMOEMPLDO;
    }

    public String gettablaEntradaClientes_IDCRTCA() {
        return tablaEntradaClientes_IDCRTCA;
    }

    public void settablaEntradaClientes_IDCRTCA(String tablaEntradaClientes_IDCRTCA) {
        this.tablaEntradaClientes_IDCRTCA = tablaEntradaClientes_IDCRTCA;
    }

    public String gettablaEntradaClientes_CNTCTO() {
        return tablaEntradaClientes_CNTCTO;
    }

    public void settablaEntradaClientes_CNTCTO(String tablaEntradaClientes_CNTCTO) {
        this.tablaEntradaClientes_CNTCTO = tablaEntradaClientes_CNTCTO;
    }

    public String gettablaEntradaClientes_CNSMOMSAL() {
        return tablaEntradaClientes_CNSMOMSAL;
    }

    public void settablaEntradaClientes_CNSMOMSAL(String tablaEntradaClientes_CNSMOMSAL) {
        this.tablaEntradaClientes_CNSMOMSAL = tablaEntradaClientes_CNSMOMSAL;
    }

    public String gettablaEntradaClientes_CNSMOANOANT() {
        return tablaEntradaClientes_CNSMOANOANT;
    }

    public void settablaEntradaClientes_CNSMOANOANT(String tablaEntradaClientes_CNSMOANOANT) {
        this.tablaEntradaClientes_CNSMOANOANT = tablaEntradaClientes_CNSMOANOANT;
    }

    public String gettablaEntradaClientes_TPONTFCCION() {
        return tablaEntradaClientes_TPONTFCCION;
    }

    public void settablaEntradaClientes_TPONTFCCION(String tablaEntradaClientes_TPONTFCCION) {
        this.tablaEntradaClientes_TPONTFCCION = tablaEntradaClientes_TPONTFCCION;
    }

    public String gettablaEntradaClientes_OBLGAFTO() {
        return tablaEntradaClientes_OBLGAFTO;
    }

    public void settablaEntradaClientes_OBLGAFTO(String tablaEntradaClientes_OBLGAFTO) {
        this.tablaEntradaClientes_OBLGAFTO = tablaEntradaClientes_OBLGAFTO;
    }

    public String gettablaEntradaClientes_TTBSIVA() {
        return tablaEntradaClientes_TTBSIVA;
    }

    public void settablaEntradaClientes_TTBSIVA(String tablaEntradaClientes_TTBSIVA) {
        this.tablaEntradaClientes_TTBSIVA = tablaEntradaClientes_TTBSIVA;
    }

    public String gettablaEntradaClientes_MDLDDCBRO() {
        return tablaEntradaClientes_MDLDDCBRO;
    }

    public void settablaEntradaClientes_MDLDDCBRO(String tablaEntradaClientes_MDLDDCBRO) {
        this.tablaEntradaClientes_MDLDDCBRO = tablaEntradaClientes_MDLDDCBRO;
    }

    public String gettablaEntradaClientes_FECHA_AUDITORIA() {
        return tablaEntradaClientes_FECHA_AUDITORIA;
    }

    public void settablaEntradaClientes_FECHA_AUDITORIA(String tablaEntradaClientes_FECHA_AUDITORIA) {
        this.tablaEntradaClientes_FECHA_AUDITORIA = tablaEntradaClientes_FECHA_AUDITORIA;
    }

    public String gettablaEntradaClientes_MES_1() {
        return tablaEntradaClientes_MES_1;
    }

    public void settablaEntradaClientes_MES_1(String tablaEntradaClientes_MES_1) {
        this.tablaEntradaClientes_MES_1 = tablaEntradaClientes_MES_1;
    }

    public String gettablaEntradaClientes_DIU_1() {
        return tablaEntradaClientes_DIU_1;
    }

    public void settablaEntradaClientes_DIU_1(String tablaEntradaClientes_DIU_1) {
        this.tablaEntradaClientes_DIU_1 = tablaEntradaClientes_DIU_1;
    }

    public String gettablaEntradaClientes_DIUG_1() {
        return tablaEntradaClientes_DIUG_1;
    }

    public void settablaEntradaClientes_DIUG_1(String tablaEntradaClientes_DIUG_1) {
        this.tablaEntradaClientes_DIUG_1 = tablaEntradaClientes_DIUG_1;
    }

    public String gettablaEntradaClientes_DIUM_1() {
        return tablaEntradaClientes_DIUM_1;
    }

    public void settablaEntradaClientes_DIUM_1(String tablaEntradaClientes_DIUM_1) {
        this.tablaEntradaClientes_DIUM_1 = tablaEntradaClientes_DIUM_1;
    }

    public String gettablaEntradaClientes_FIU_1() {
        return tablaEntradaClientes_FIU_1;
    }

    public void settablaEntradaClientes_FIU_1(String tablaEntradaClientes_FIU_1) {
        this.tablaEntradaClientes_FIU_1 = tablaEntradaClientes_FIU_1;
    }

    public String gettablaEntradaClientes_FIUG_1() {
        return tablaEntradaClientes_FIUG_1;
    }

    public void settablaEntradaClientes_FIUG_1(String tablaEntradaClientes_FIUG_1) {
        this.tablaEntradaClientes_FIUG_1 = tablaEntradaClientes_FIUG_1;
    }

    public String gettablaEntradaClientes_FIUM_1() {
        return tablaEntradaClientes_FIUM_1;
    }

    public void settablaEntradaClientes_FIUM_1(String tablaEntradaClientes_FIUM_1) {
        this.tablaEntradaClientes_FIUM_1 = tablaEntradaClientes_FIUM_1;
    }

    public String gettablaEntradaClientes_MES_2() {
        return tablaEntradaClientes_MES_2;
    }

    public void settablaEntradaClientes_MES_2(String tablaEntradaClientes_MES_2) {
        this.tablaEntradaClientes_MES_2 = tablaEntradaClientes_MES_2;
    }

    public String gettablaEntradaClientes_DIU_2() {
        return tablaEntradaClientes_DIU_2;
    }

    public void settablaEntradaClientes_DIU_2(String tablaEntradaClientes_DIU_2) {
        this.tablaEntradaClientes_DIU_2 = tablaEntradaClientes_DIU_2;
    }

    public String gettablaEntradaClientes_DIUG_2() {
        return tablaEntradaClientes_DIUG_2;
    }

    public void settablaEntradaClientes_DIUG_2(String tablaEntradaClientes_DIUG_2) {
        this.tablaEntradaClientes_DIUG_2 = tablaEntradaClientes_DIUG_2;
    }

    public String gettablaEntradaClientes_DIUM_2() {
        return tablaEntradaClientes_DIUM_2;
    }

    public void settablaEntradaClientes_DIUM_2(String tablaEntradaClientes_DIUM_2) {
        this.tablaEntradaClientes_DIUM_2 = tablaEntradaClientes_DIUM_2;
    }

    public String gettablaEntradaClientes_FIU_2() {
        return tablaEntradaClientes_FIU_2;
    }

    public void settablaEntradaClientes_FIU_2(String tablaEntradaClientes_FIU_2) {
        this.tablaEntradaClientes_FIU_2 = tablaEntradaClientes_FIU_2;
    }

    public String gettablaEntradaClientes_FIUG_2() {
        return tablaEntradaClientes_FIUG_2;
    }

    public void settablaEntradaClientes_FIUG_2(String tablaEntradaClientes_FIUG_2) {
        this.tablaEntradaClientes_FIUG_2 = tablaEntradaClientes_FIUG_2;
    }

    public String gettablaEntradaClientes_FIUM_2() {
        return tablaEntradaClientes_FIUM_2;
    }

    public void settablaEntradaClientes_FIUM_2(String tablaEntradaClientes_FIUM_2) {
        this.tablaEntradaClientes_FIUM_2 = tablaEntradaClientes_FIUM_2;
    }

    public String gettablaEntradaClientes_MES_3() {
        return tablaEntradaClientes_MES_3;
    }

    public void settablaEntradaClientes_MES_3(String tablaEntradaClientes_MES_3) {
        this.tablaEntradaClientes_MES_3 = tablaEntradaClientes_MES_3;
    }

    public String gettablaEntradaClientes_DIU_3() {
        return tablaEntradaClientes_DIU_3;
    }

    public void settablaEntradaClientes_DIU_3(String tablaEntradaClientes_DIU_3) {
        this.tablaEntradaClientes_DIU_3 = tablaEntradaClientes_DIU_3;
    }

    public String gettablaEntradaClientes_DIUG_3() {
        return tablaEntradaClientes_DIUG_3;
    }

    public void settablaEntradaClientes_DIUG_3(String tablaEntradaClientes_DIUG_3) {
        this.tablaEntradaClientes_DIUG_3 = tablaEntradaClientes_DIUG_3;
    }

    public String gettablaEntradaClientes_DIUM_3() {
        return tablaEntradaClientes_DIUM_3;
    }

    public void settablaEntradaClientes_DIUM_3(String tablaEntradaClientes_DIUM_3) {
        this.tablaEntradaClientes_DIUM_3 = tablaEntradaClientes_DIUM_3;
    }

    public String gettablaEntradaClientes_FIU_3() {
        return tablaEntradaClientes_FIU_3;
    }

    public void settablaEntradaClientes_FIU_3(String tablaEntradaClientes_FIU_3) {
        this.tablaEntradaClientes_FIU_3 = tablaEntradaClientes_FIU_3;
    }

    public String gettablaEntradaClientes_FIUG_3() {
        return tablaEntradaClientes_FIUG_3;
    }

    public void settablaEntradaClientes_FIUG_3(String tablaEntradaClientes_FIUG_3) {
        this.tablaEntradaClientes_FIUG_3 = tablaEntradaClientes_FIUG_3;
    }

    public String gettablaEntradaClientes_FIUM_3() {
        return tablaEntradaClientes_FIUM_3;
    }

    public void settablaEntradaClientes_FIUM_3(String tablaEntradaClientes_FIUM_3) {
        this.tablaEntradaClientes_FIUM_3 = tablaEntradaClientes_FIUM_3;
    }

    public String gettablaEntradaClientes_DIU_PERIODO() {
        return tablaEntradaClientes_DIU_PERIODO;
    }

    public void settablaEntradaClientes_DIU_PERIODO(String tablaEntradaClientes_DIU_PERIODO) {
        this.tablaEntradaClientes_DIU_PERIODO = tablaEntradaClientes_DIU_PERIODO;
    }

    public String gettablaEntradaClientes_TOTAL_DIUM() {
        return tablaEntradaClientes_TOTAL_DIUM;
    }

    public void settablaEntradaClientes_TOTAL_DIUM(String tablaEntradaClientes_TOTAL_DIUM) {
        this.tablaEntradaClientes_TOTAL_DIUM = tablaEntradaClientes_TOTAL_DIUM;
    }

    public String gettablaEntradaClientes_EXCLUIDAS() {
        return tablaEntradaClientes_EXCLUIDAS;
    }

    public void settablaEntradaClientes_EXCLUIDAS(String tablaEntradaClientes_EXCLUIDAS) {
        this.tablaEntradaClientes_EXCLUIDAS = tablaEntradaClientes_EXCLUIDAS;
    }

    public String gettablaEntradaClientes_MC() {
        return tablaEntradaClientes_MC;
    }

    public void settablaEntradaClientes_MC(String tablaEntradaClientes_MC) {
        this.tablaEntradaClientes_MC = tablaEntradaClientes_MC;
    }

    public String gettablaEntradaClientes_MF() {
        return tablaEntradaClientes_MF;
    }

    public void settablaEntradaClientes_MF(String tablaEntradaClientes_MF) {
        this.tablaEntradaClientes_MF = tablaEntradaClientes_MF;
    }

    public String gettablaEntradaClientes_COSTO_D() {
        return tablaEntradaClientes_COSTO_D;
    }

    public void settablaEntradaClientes_COSTO_D(String tablaEntradaClientes_COSTO_D) {
        this.tablaEntradaClientes_COSTO_D = tablaEntradaClientes_COSTO_D;
    }

    public String gettablaEntradaClientes_THC() {
        return tablaEntradaClientes_THC;
    }

    public void settablaEntradaClientes_THC(String tablaEntradaClientes_THC) {
        this.tablaEntradaClientes_THC = tablaEntradaClientes_THC;
    }

    public String gettablaEntradaClientes_TVC() {
        return tablaEntradaClientes_TVC;
    }

    public void settablaEntradaClientes_TVC(String tablaEntradaClientes_TVC) {
        this.tablaEntradaClientes_TVC = tablaEntradaClientes_TVC;
    }

    public String gettablaEntradaClientes_CEC() {
        return tablaEntradaClientes_CEC;
    }

    public void settablaEntradaClientes_CEC(String tablaEntradaClientes_CEC) {
        this.tablaEntradaClientes_CEC = tablaEntradaClientes_CEC;
    }

    public String gettablaEntradaClientes_VCD() {
        return tablaEntradaClientes_VCD;
    }

    public void settablaEntradaClientes_VCD(String tablaEntradaClientes_VCD) {
        this.tablaEntradaClientes_VCD = tablaEntradaClientes_VCD;
    }

    public String gettablaEntradaClientes_VCF() {
        return tablaEntradaClientes_VCF;
    }

    public void settablaEntradaClientes_VCF(String tablaEntradaClientes_VCF) {
        this.tablaEntradaClientes_VCF = tablaEntradaClientes_VCF;
    }

    public String gettablaEntradaClientes_MDDOR1SBSDIOCNTRBCION() {
        return tablaEntradaClientes_MDDOR1SBSDIOCNTRBCION;
    }

    public void settablaEntradaClientes_MDDOR1SBSDIOCNTRBCION(String tablaEntradaClientes_MDDOR1SBSDIOCNTRBCION) {
        this.tablaEntradaClientes_MDDOR1SBSDIOCNTRBCION = tablaEntradaClientes_MDDOR1SBSDIOCNTRBCION;
    }

    public String gettablaEntradaClientes_MDDOR1VLORKWH() {
        return tablaEntradaClientes_MDDOR1VLORKWH;
    }

    public void settablaEntradaClientes_MDDOR1VLORKWH(String tablaEntradaClientes_MDDOR1VLORKWH) {
        this.tablaEntradaClientes_MDDOR1VLORKWH = tablaEntradaClientes_MDDOR1VLORKWH;
    }

    public String gettablaEntradaClientes_MDDOR2SBSDIOCNTRBCION() {
        return tablaEntradaClientes_MDDOR2SBSDIOCNTRBCION;
    }

    public void settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION(String tablaEntradaClientes_MDDOR2SBSDIOCNTRBCION) {
        this.tablaEntradaClientes_MDDOR2SBSDIOCNTRBCION = tablaEntradaClientes_MDDOR2SBSDIOCNTRBCION;
    }

    public String gettablaEntradaClientes_MDDOR2VLORKWH() {
        return tablaEntradaClientes_MDDOR2VLORKWH;
    }

    public void settablaEntradaClientes_MDDOR2VLORKWH(String tablaEntradaClientes_MDDOR2VLORKWH) {
        this.tablaEntradaClientes_MDDOR2VLORKWH = tablaEntradaClientes_MDDOR2VLORKWH;
    }

    public Boolean abrir_TablaEntradaClientes(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        setArchivo_TablaEntradaClientes(nombreArchivo);
        return abrir();
    }

    private Boolean abrir() {
        try {
            int fileSize = (int) rFile.length();
            total_TablaEntradaClientes = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaEntradaClientes() {
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void posicion_TablaEntradaClientes(int registro, int tamano) {
        try {
            rFile.seek((long) (registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // IMPORTANTE: requiere haber llamado abrir_TablaEntradaClientes() antes.
    public int lectura_TablaEntradaClientes(int registro) {
        Log.e("INFO", "lectura_TablaEntradaClientes: " + archivo_TablaEntradaClientes + " |data cliente " + registro);
        encontro_TablaEntradaClientes = 0;
        posicion_TablaEntradaClientes(registro, LONGITUD_REGISTRO);
        try {
            rFile.readFully(bufferRegistro);
            settablaEntradaClientes_anio(new String(bufferRegistro, 0, 4, StandardCharsets.UTF_8));
            settablaEntradaClientes_mes(new String(bufferRegistro, 5, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_Cliente(new String(bufferRegistro, 8, 5, StandardCharsets.UTF_8));
            settablaEntradaClientes_Ciclo(new String(bufferRegistro, 14, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_Departamento(new String(bufferRegistro, 18, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_Municipio(new String(bufferRegistro, 21, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_Ruta(new String(bufferRegistro, 25, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_Cuenta(new String(bufferRegistro, 42, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_Bimestral(new String(bufferRegistro, 53, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_Actividad(new String(bufferRegistro, 55, 4, StandardCharsets.UTF_8));
            settablaEntradaClientes_Estadodelservicio(new String(bufferRegistro, 60, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_Nrofactura(new String(bufferRegistro, 62, 15, StandardCharsets.UTF_8));
            settablaEntradaClientes_Facturadomiciliada(new String(bufferRegistro, 78, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_Nombre(new String(bufferRegistro, 80, 48, StandardCharsets.UTF_8));
            settablaEntradaClientes_Direccion(new String(bufferRegistro, 129, 60, StandardCharsets.UTF_8));
            settablaEntradaClientes_Informacionadicional(new String(bufferRegistro, 190, 250, StandardCharsets.UTF_8));
            settablaEntradaClientes_cordenadaPredioX(new String(bufferRegistro, 441, 20, StandardCharsets.UTF_8));
            settablaEntradaClientes_cordenadaPredioY(new String(bufferRegistro, 462, 20, StandardCharsets.UTF_8));
            settablaEntradaClientes_DireccionPostal(new String(bufferRegistro, 483, 40, StandardCharsets.UTF_8));
            settablaEntradaClientes_Nitocedula(new String(bufferRegistro, 524, 15, StandardCharsets.UTF_8));
            settablaEntradaClientes_Estrato(new String(bufferRegistro, 540, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_Clasedeservicio(new String(bufferRegistro, 542, 24, StandardCharsets.UTF_8));
            settablaEntradaClientes_claseagrupacion(new String(bufferRegistro, 567, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_Fechaultimopago(new String(bufferRegistro, 571, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_Valorultimopago(new String(bufferRegistro, 582, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_tipoultimopago(new String(bufferRegistro, 595, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_Lugarultimopago(new String(bufferRegistro, 598, 40, StandardCharsets.UTF_8));
            settablaEntradaClientes_Fechaultimafactura(new String(bufferRegistro, 639, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_diasfacturados(new String(bufferRegistro, 650, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_Descalculado(new String(bufferRegistro, 654, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_Desmaximoadmisible(new String(bufferRegistro, 667, 8, StandardCharsets.UTF_8));
            settablaEntradaClientes_Fescalculado(new String(bufferRegistro, 676, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_Fesmaximoadmisible(new String(bufferRegistro, 689, 8, StandardCharsets.UTF_8));
            settablaEntradaClientes_mesesatra(new String(bufferRegistro, 698, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_cargainstalada(new String(bufferRegistro, 702, 9, StandardCharsets.UTF_8));
            settablaEntradaClientes_Niveldetension(new String(bufferRegistro, 712, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_Nodoconexion(new String(bufferRegistro, 714, 11, StandardCharsets.UTF_8));
            settablaEntradaClientes_Nodocircuito(new String(bufferRegistro, 726, 11, StandardCharsets.UTF_8));
            settablaEntradaClientes_nombrecircuito(new String(bufferRegistro, 738, 45, StandardCharsets.UTF_8));
            settablaEntradaClientes_dato(new String(bufferRegistro, 784, 5, StandardCharsets.UTF_8));
            settablaEntradaClientes_Nrodemedidores(new String(bufferRegistro, 790, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_primermedidor(new String(bufferRegistro, 792, 5, StandardCharsets.UTF_8));
            settablaEntradaClientes_nrocobros(new String(bufferRegistro, 798, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_primercobro(new String(bufferRegistro, 801, 6, StandardCharsets.UTF_8));//nuevo para poder incluir conceptos sumados de 6 digitos
            settablaEntradaClientes_consecutivo(new String(bufferRegistro, 808, 5, StandardCharsets.UTF_8));
            settablaEntradaClientes_fechavence(new String(bufferRegistro, 814, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_fechacorte(new String(bufferRegistro, 827, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_cordenadax(new String(bufferRegistro, 840, 15, StandardCharsets.UTF_8));
            settablaEntradaClientes_cordenaday(new String(bufferRegistro, 856, 15, StandardCharsets.UTF_8));
            settablaEntradaClientes_grupo(new String(bufferRegistro, 872, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_consumoanioant(new String(bufferRegistro, 883, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_sector(new String(bufferRegistro, 894, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_consumofoes(new String(bufferRegistro, 898, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_kwfoes(new String(bufferRegistro, 909, 11, StandardCharsets.UTF_8));
            settablaEntradaClientes_valorfoes(new String(bufferRegistro, 921, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_facturafoes(new String(bufferRegistro, 932, 15, StandardCharsets.UTF_8));
            settablaEntradaClientes_diasvencimiento(new String(bufferRegistro, 948, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_nrofamilias(new String(bufferRegistro, 951, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_CDGOMNSJE(new String(bufferRegistro, 955, 8, StandardCharsets.UTF_8));
            settablaEntradaClientes_EMPLDO(new String(bufferRegistro, 964, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_CSTOG(new String(bufferRegistro, 966, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_CSTOT(new String(bufferRegistro, 981, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_CSTOPR(new String(bufferRegistro, 996, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_CSTOD(new String(bufferRegistro, 1011, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_CSTOR(new String(bufferRegistro, 1026, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_CSTOCV(new String(bufferRegistro, 1041, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_CSTOCF(new String(bufferRegistro, 1056, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_CU(new String(bufferRegistro, 1071, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_FNNCCIONVLOR(new String(bufferRegistro, 1086, 11, StandardCharsets.UTF_8));
            settablaEntradaClientes_FNNCCIONCTAS(new String(bufferRegistro, 1098, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_FNNCCIONCTASPNDNTES(new String(bufferRegistro, 1102, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_FNNCCIONSLDO(new String(bufferRegistro, 1106, 11, StandardCharsets.UTF_8));
            settablaEntradaClientes_FCHAINCIOAGNDA(new String(bufferRegistro, 1118, 25, StandardCharsets.UTF_8));
            settablaEntradaClientes_FCHAFINAGNDA(new String(bufferRegistro, 1144, 25, StandardCharsets.UTF_8));
            settablaEntradaClientes_LDO(new String(bufferRegistro, 1170, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_FCHARGTRALCTURA(new String(bufferRegistro, 1173, 25, StandardCharsets.UTF_8));
            settablaEntradaClientes_CDGOATRZCION(new String(bufferRegistro, 1199, 8, StandardCharsets.UTF_8));
            settablaEntradaClientes_HDOP(new String(bufferRegistro, 1208, 4, StandardCharsets.UTF_8));
            settablaEntradaClientes_USUARIO_PROCESO(new String(bufferRegistro, 1213, 30, StandardCharsets.UTF_8));
            settablaEntradaClientes_FECHA_PROCESO(new String(bufferRegistro, 1244, 25, StandardCharsets.UTF_8));
            settablaEntradaClientes_PROCESADO(new String(bufferRegistro, 1270, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_PRDOCMPNSCION(new String(bufferRegistro, 1272, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_CUAPLCBLE(new String(bufferRegistro, 1285, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_VLORKWFOES(new String(bufferRegistro, 1300, 14, StandardCharsets.UTF_8));
            settablaEntradaClientes_TPECNSMOEMPLDO(new String(bufferRegistro, 1315, 11, StandardCharsets.UTF_8));
            settablaEntradaClientes_IDCRTCA(new String(bufferRegistro, 1327, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_CNTCTO(new String(bufferRegistro, 1331, 13, StandardCharsets.UTF_8));
            settablaEntradaClientes_CNSMOMSAL(new String(bufferRegistro, 1345, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_CNSMOANOANT(new String(bufferRegistro, 1356, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_TPONTFCCION(new String(bufferRegistro, 1367, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_OBLGAFTO(new String(bufferRegistro, 1370, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_TTBSIVA(new String(bufferRegistro, 1372, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_MDLDDCBRO(new String(bufferRegistro, 1385, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_FECHA_AUDITORIA(new String(bufferRegistro, 1388, 25, StandardCharsets.UTF_8));
            settablaEntradaClientes_MES_1(new String(bufferRegistro, 1414, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIU_1(new String(bufferRegistro, 1418, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIUG_1(new String(bufferRegistro, 1431, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIUM_1(new String(bufferRegistro, 1444, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIU_1(new String(bufferRegistro, 1457, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIUG_1(new String(bufferRegistro, 1470, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIUM_1(new String(bufferRegistro, 1483, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_MES_2(new String(bufferRegistro, 1496, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIU_2(new String(bufferRegistro, 1500, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIUG_2(new String(bufferRegistro, 1513, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIUM_2(new String(bufferRegistro, 1526, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIU_2(new String(bufferRegistro, 1539, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIUG_2(new String(bufferRegistro, 1552, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIUM_2(new String(bufferRegistro, 1565, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_MES_3(new String(bufferRegistro, 1578, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIU_3(new String(bufferRegistro, 1582, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIUG_3(new String(bufferRegistro, 1595, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIUM_3(new String(bufferRegistro, 1608, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIU_3(new String(bufferRegistro, 1621, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIUG_3(new String(bufferRegistro, 1634, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_FIUM_3(new String(bufferRegistro, 1647, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_DIU_PERIODO(new String(bufferRegistro, 1660, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_TOTAL_DIUM(new String(bufferRegistro, 1673, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_EXCLUIDAS(new String(bufferRegistro, 1686, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_MC(new String(bufferRegistro, 1699, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_MF(new String(bufferRegistro, 1701, 1, StandardCharsets.UTF_8));
            settablaEntradaClientes_COSTO_D(new String(bufferRegistro, 1703, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_THC(new String(bufferRegistro, 1720, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_TVC(new String(bufferRegistro, 1737, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_CEC(new String(bufferRegistro, 1754, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_VCD(new String(bufferRegistro, 1771, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_VCF(new String(bufferRegistro, 1788, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_MDDOR1SBSDIOCNTRBCION(new String(bufferRegistro, 1805, 6, StandardCharsets.UTF_8));
            settablaEntradaClientes_MDDOR1VLORKWH(new String(bufferRegistro, 1812, 12, StandardCharsets.UTF_8));
            settablaEntradaClientes_MDDOR2SBSDIOCNTRBCION(new String(bufferRegistro, 1825, 6, StandardCharsets.UTF_8));
            settablaEntradaClientes_MDDOR2VLORKWH(new String(bufferRegistro, 1832, 12, StandardCharsets.UTF_8));
            ultimo_TablaEntradaClientes = registro;
            // fin estructura
        } catch (Exception e) {
            Log.e("error", "lectura cli " + e.getMessage());
            e.printStackTrace();
            return -2;
        }
        return 1;
    }
    public void lectura_TablaEntradaClientesII(int registro) {

        encontro_TablaEntradaClientes = 0;
        posicion_TablaEntradaClientes(registro, LONGITUD_REGISTRO);
        try {
            rFile.readFully(bufferRegistro);


            settablaEntradaClientes_anio(new String(bufferRegistro, 0, 4, StandardCharsets.UTF_8));
            settablaEntradaClientes_mes(new String(bufferRegistro, 5, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_Cliente(new String(bufferRegistro, 8, 5, StandardCharsets.UTF_8));
            settablaEntradaClientes_Ciclo(new String(bufferRegistro, 14, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_Departamento(new String(bufferRegistro, 18, 2, StandardCharsets.UTF_8));
            settablaEntradaClientes_Municipio(new String(bufferRegistro, 21, 3, StandardCharsets.UTF_8));
            settablaEntradaClientes_Ruta(new String(bufferRegistro, 25, 16, StandardCharsets.UTF_8));
            settablaEntradaClientes_Cuenta(new String(bufferRegistro, 42, 10, StandardCharsets.UTF_8));
            settablaEntradaClientes_Nombre(new String(bufferRegistro, 80, 48, StandardCharsets.UTF_8));
            settablaEntradaClientes_Direccion(new String(bufferRegistro, 129, 60, StandardCharsets.UTF_8));



            ultimo_TablaEntradaClientes = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getArchivo_TablaEntradaClientes() {
        return archivo_TablaEntradaClientes;
    }

    public void setArchivo_TablaEntradaClientes(String archivo_TablaEntradaClientes) {
        this.archivo_TablaEntradaClientes = archivo_TablaEntradaClientes;
    }

    public int getTotal_TablaEntradaClientes() {
        return total_TablaEntradaClientes;
    }

    public void setTotal_TablaEntradaClientes(int total_TablaEntradaClientes) {
        this.total_TablaEntradaClientes = total_TablaEntradaClientes;
    }
}