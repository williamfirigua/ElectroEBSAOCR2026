package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import static com.gselectroCaqueta.tablas.UtilPlanos.LATIN1;

/// <summary>
/// ENVIOSGPRS.SDA / CopiaRespaldoLecturas.sda - registro fijo de 580 bytes, solo se AGREGAN lineas.
/// - Agregar es atomico entre TODAS las instancias (candado de clase): la posicion se calcula con el
///   tamano real del archivo en el momento de escribir, asi dos flujos nunca escriben la misma linea.
/// - Si el archivo termina con una linea incompleta (corte de energia, app cerrada), la siguiente
///   escritura la reemplaza en lugar de dejar el resto del archivo corrido.
/// - Corrige setEnvioGPS_ValorFacturadoASEO (el aseo llegaba "null") y la lectura de NREGISTRADORES.
/// - Busqueda por indice en memoria (antes: recorrido completo por cada cuenta en ValidarNoEnviados).
/// </summary>
public class EnvioGPS {

    private static final String TAG = "ENVIOGPRS";
    static final int LONGITUD_REGISTRO = 569 + 11; // 580: incluye VALORFACTURADOASEO
    /** Candado comun a todas las instancias: el liquidador y el hilo de envio usan objetos distintos sobre el mismo archivo. */
    private static final Object CANDADO_ARCHIVO = new Object();

    public String archivo_EnvioGPS;
    public int total_EnvioGPS;
    public int encontro_EnvioGPS;
    String sep = ";";
    String EnvioGPS_ciclo;
    String EnvioGPS_mununicipio;
    String EnvioGPS_seccion;
    String EnvioGPS_departamento;
    String EnvioGPS_anno;
    String EnvioGPS_mes;
    String EnvioGPS_cuenta;
    String EnvioGPS_digitochequeo;
    String EnvioGPS_NroContador;
    String EnvioGPS_idcontador;
    String EnvioGPS_lecturatomada;
    String EnvioGPS_causadenolectura;
    String EnvioGPS_descanomalia;
    String EnvioGPS_comentario;
    String EnvioGPS_desccomentario;
    String EnvioGPS_fechayhoralectura;
    String EnvioGPS_CodLector;
    String EnvioGPS_primermedidor;
    String EnvioGPS_HoraImpresion;
    String EnvioGPS_NombreArchivo;
    String EnvioGPS_Longitud;
    String EnvioGPS_Latitud;
    String EnvioGPS_NroSatelites;
    String EnvioGPS_Distancia;
    String EnvioGPS_FechaHoraSatelite;
    String EnvioGPS_AltitudSatelite;
    String EnvioGPS_Terminal;
    String EnvioGPS_IndicadorNovedad;
    String EnvioGPS_Indcodbarras;
    String EnvioGPS_Intentos;
    String EnvioGPS_criticapda;
    String EnvioGPS_ValorFacturado;
    String EnvioGPS_ConsumoFacturado;
    String EnvioGPS_SubContribucion;
    String EnvioGPS_Consumo1;
    String EnvioGPS_Consumo2;
    String EnvioGPS_Consumo3;
    String EnvioGPS_NroConceptos;
    String EnvioGPS_IndFacturacion;
    String EnvioGPS_NroFactura;
    String EnvioGPS_FechaVence;
    String EnvioGPS_FechaCorte;
    String EnvioGPS_LecturaModificada1;
    String EnvioGPS_LecturaModificada2;
    String EnvioGPS_Digitos;
    String EnvioGPS_Procesado;
    String EnvioGPS_NumeroConceptos;
    String EnvioGPS_PrimerConcepto;
    String EnvioGPS_CritiaSIEC;
    String EnvioGPS_CodNovedad;
    String EnvioGPS_LecturaAnterior;
    String EnvioGPS_EstadoEnvio;

    //nuevos campo para generar al central
    String EnvioGPS_MARCACONTADOR;
    String EnvioGPS_ULTIMOMEDIDORLEIDO;
    String EnvioGPS_MARCALEIDO;
    String EnvioGPS_OBSERVACION1;

    String EnvioGPS_CLASESERVICIO;

    //nuevos para mayor control de los registros a enviar al siec
    String EnvioGPS_NMEDIDORES;
    String EnvioGPS_NREGISTRADORES;

    String EnvioGPS_IDREGISTRO;
    String EnvioGPS_NROCONTADORDB;
    String EnvioGPS_ValorFacturadoAseo;

    String EnvioGPS_CRNL;
    BufferedReader fin;                                         // sin uso, se conserva por compatibilidad
    private final byte[] bufferLectura = new byte[LONGITUD_REGISTRO];
    private final byte[] bufferCorto = new byte[51];            // cuenta + nro contador (lectura Tipo2)
    byte[] byteArray = bufferLectura;
    int ultimo_EnvioGPS;
    String buscar_EnvioGPS;
    String texto;
    RandomAccessFile rFile;
    Utils utils = new Utils();

    // Indice para BuscarSecuencial_EnvioGPS (se reconstruye si el archivo crecio).
    private final Map<String, Integer> indice = new HashMap<String, Integer>();
    private long largoIndexado = -1;

    public String getEnvioGPS_CICLO() {
        return EnvioGPS_ciclo;
    }

    public void setEnvioGPS_ciclo(String EnvioGPS_ciclo) {
        this.EnvioGPS_ciclo = EnvioGPS_ciclo;
    }

    public String getEnvioGPS_MUNUNICIPIO() {
        return EnvioGPS_mununicipio;
    }

    public void setEnvioGPS_mununicipio(String EnvioGPS_mununicipio) {
        this.EnvioGPS_mununicipio = EnvioGPS_mununicipio;
    }

    public String getEnvioGPS_SECCION() {
        return EnvioGPS_seccion;
    }

    public void setEnvioGPS_seccion(String EnvioGPS_seccion) {
        this.EnvioGPS_seccion = EnvioGPS_seccion;
    }

    public String getEnvioGPS_DEPARTAMENTO() {
        return EnvioGPS_departamento;
    }

    public void setEnvioGPS_departamento(String EnvioGPS_departamento) {
        this.EnvioGPS_departamento = EnvioGPS_departamento;
    }

    public String getEnvioGPS_ANNO() {
        return EnvioGPS_anno;
    }

    public void setEnvioGPS_anno(String EnvioGPS_anno) {
        this.EnvioGPS_anno = EnvioGPS_anno;
    }

    public String getEnvioGPS_MES() {
        return EnvioGPS_mes;
    }

    public void setEnvioGPS_mes(String EnvioGPS_mes) {
        this.EnvioGPS_mes = EnvioGPS_mes;
    }

    public String getEnvioGPS_CUENTA() {
        return EnvioGPS_cuenta;
    }

    public void setEnvioGPS_cuenta(String EnvioGPS_cuenta) {
        this.EnvioGPS_cuenta = EnvioGPS_cuenta;
    }

    public String getEnvioGPS_DIGITOCHEQUEO() {
        return EnvioGPS_digitochequeo;
    }

    public void setEnvioGPS_digitochequeo(String EnvioGPS_digitochequeo) {
        this.EnvioGPS_digitochequeo = EnvioGPS_digitochequeo;
    }

    public String getEnvioGPS_NROCONTADOR() {
        return EnvioGPS_NroContador;
    }

    public void setEnvioGPS_NroContador(String EnvioGPS_NroContador) {
        this.EnvioGPS_NroContador = EnvioGPS_NroContador;
    }

    public String getEnvioGPS_IDCONTADOR() {
        return EnvioGPS_idcontador;
    }

    public void setEnvioGPS_idcontador(String EnvioGPS_idcontador) {
        this.EnvioGPS_idcontador = EnvioGPS_idcontador;
    }

    public String getEnvioGPS_LECTURATOMADA() {
        return EnvioGPS_lecturatomada;
    }

    public void setEnvioGPS_lecturatomada(String EnvioGPS_lecturatomada) {
        this.EnvioGPS_lecturatomada = EnvioGPS_lecturatomada;
    }

    public String getEnvioGPS_CAUSADENOLECTURA() {
        return EnvioGPS_causadenolectura;
    }

    public void setEnvioGPS_causadenolectura(String EnvioGPS_causadenolectura) {
        this.EnvioGPS_causadenolectura = EnvioGPS_causadenolectura;
    }

    public String getEnvioGPS_DESCANOMALIA() {
        return EnvioGPS_descanomalia;
    }

    public void setEnvioGPS_descanomalia(String EnvioGPS_descanomalia) {
        this.EnvioGPS_descanomalia = EnvioGPS_descanomalia;
    }

    public String getEnvioGPS_COMENTARIO() {
        return EnvioGPS_comentario;
    }

    public void setEnvioGPS_comentario(String EnvioGPS_comentario) {
        this.EnvioGPS_comentario = EnvioGPS_comentario;
    }

    public String getEnvioGPS_DESCCOMENTARIO() {
        return EnvioGPS_desccomentario;
    }

    public void setEnvioGPS_desccomentario(String EnvioGPS_desccomentario) {
        this.EnvioGPS_desccomentario = EnvioGPS_desccomentario;
    }

    public String getEnvioGPS_FECHAYHORALECTURA() {
        return EnvioGPS_fechayhoralectura;
    }

    public void setEnvioGPS_fechayhoralectura(String EnvioGPS_fechayhoralectura) {
        this.EnvioGPS_fechayhoralectura = EnvioGPS_fechayhoralectura;
    }

    public String getEnvioGPS_CODLECTOR() {
        return EnvioGPS_CodLector;
    }

    public void setEnvioGPS_CodLector(String EnvioGPS_CodLector) {
        this.EnvioGPS_CodLector = EnvioGPS_CodLector;
    }

    public String getEnvioGPS_PRIMERMEDIDOR() {
        return EnvioGPS_primermedidor;
    }

    public void setEnvioGPS_primermedidor(String EnvioGPS_primermedidor) {
        this.EnvioGPS_primermedidor = EnvioGPS_primermedidor;
    }

    public String getEnvioGPS_HORAIMPRESION() {
        return EnvioGPS_HoraImpresion;
    }

    public void setEnvioGPS_HoraImpresion(String EnvioGPS_HoraImpresion) {
        this.EnvioGPS_HoraImpresion = EnvioGPS_HoraImpresion;
    }

    public String getEnvioGPS_NOMBREARCHIVO() {
        return EnvioGPS_NombreArchivo;
    }

    public void setEnvioGPS_NombreArchivo(String EnvioGPS_NombreArchivo) {
        this.EnvioGPS_NombreArchivo = EnvioGPS_NombreArchivo;
    }

    public String getEnvioGPS_LONGITUD() {
        return EnvioGPS_Longitud;
    }

    public void setEnvioGPS_Longitud(String EnvioGPS_Longitud) {
        this.EnvioGPS_Longitud = EnvioGPS_Longitud;
    }

    public String getEnvioGPS_LATITUD() {
        return EnvioGPS_Latitud;
    }

    public void setEnvioGPS_Latitud(String EnvioGPS_Latitud) {
        this.EnvioGPS_Latitud = EnvioGPS_Latitud;
    }

    public String getEnvioGPS_NROSATELITES() {
        return EnvioGPS_NroSatelites;
    }

    public void setEnvioGPS_NroSatelites(String EnvioGPS_NroSatelites) {
        this.EnvioGPS_NroSatelites = EnvioGPS_NroSatelites;
    }

    public String getEnvioGPS_DISTANCIA() {
        return EnvioGPS_Distancia;
    }

    public void setEnvioGPS_Distancia(String EnvioGPS_Distancia) {
        this.EnvioGPS_Distancia = EnvioGPS_Distancia;
    }

    public String getEnvioGPS_FECHAHORASATELITE() {
        return EnvioGPS_FechaHoraSatelite;
    }

    public void setEnvioGPS_FechaHoraSatelite(String EnvioGPS_FechaHoraSatelite) {
        this.EnvioGPS_FechaHoraSatelite = EnvioGPS_FechaHoraSatelite;
    }

    public String getEnvioGPS_ALTITUDSATELITE() {
        return EnvioGPS_AltitudSatelite;
    }

    public void setEnvioGPS_AltitudSatelite(String EnvioGPS_AltitudSatelite) {
        this.EnvioGPS_AltitudSatelite = EnvioGPS_AltitudSatelite;
    }

    public String getEnvioGPS_TERMINAL() {
        return EnvioGPS_Terminal;
    }

    public void setEnvioGPS_Terminal(String EnvioGPS_Terminal) {
        this.EnvioGPS_Terminal = EnvioGPS_Terminal;
    }

    public String getEnvioGPS_INDICADORNOVEDAD() {
        return EnvioGPS_IndicadorNovedad;
    }

    public void setEnvioGPS_IndicadorNovedad(String EnvioGPS_IndicadorNovedad) {
        this.EnvioGPS_IndicadorNovedad = EnvioGPS_IndicadorNovedad;
    }

    public String getEnvioGPS_INDCODBARRAS() {
        return EnvioGPS_Indcodbarras;
    }

    public void setEnvioGPS_Indcodbarras(String EnvioGPS_Indcodbarras) {
        this.EnvioGPS_Indcodbarras = EnvioGPS_Indcodbarras;
    }

    public String getEnvioGPS_INTENTOS() {
        return EnvioGPS_Intentos;
    }

    public void setEnvioGPS_Intentos(String EnvioGPS_Intentos) {
        this.EnvioGPS_Intentos = EnvioGPS_Intentos;
    }

    public String getEnvioGPS_CRITICAPDA() {
        return EnvioGPS_criticapda;
    }

    public void setEnvioGPS_criticapda(String EnvioGPS_criticapda) {
        this.EnvioGPS_criticapda = EnvioGPS_criticapda;
    }

    public String getEnvioGPS_VALORFACTURADO() {
        return EnvioGPS_ValorFacturado;
    }

    public void setEnvioGPS_ValorFacturado(String EnvioGPS_ValorFacturado) {
        this.EnvioGPS_ValorFacturado = EnvioGPS_ValorFacturado;
    }

    public String getEnvioGPS_CONSUMOFACTURADO() {
        return EnvioGPS_ConsumoFacturado;
    }

    public void setEnvioGPS_ConsumoFacturado(String EnvioGPS_ConsumoFacturado) {
        this.EnvioGPS_ConsumoFacturado = EnvioGPS_ConsumoFacturado;
    }

    public String getEnvioGPS_SUBCONTRIBUCION() {
        return EnvioGPS_SubContribucion;
    }

    public void setEnvioGPS_SubContribucion(String EnvioGPS_SubContribucion) {
        this.EnvioGPS_SubContribucion = EnvioGPS_SubContribucion;
    }

    public String getEnvioGPS_CONSUMO1() {
        return EnvioGPS_Consumo1;
    }

    public void setEnvioGPS_Consumo1(String EnvioGPS_Consumo1) {
        this.EnvioGPS_Consumo1 = EnvioGPS_Consumo1;
    }

    public String getEnvioGPS_CONSUMO2() {
        return EnvioGPS_Consumo2;
    }

    public void setEnvioGPS_Consumo2(String EnvioGPS_Consumo2) {
        this.EnvioGPS_Consumo2 = EnvioGPS_Consumo2;
    }

    public String getEnvioGPS_CONSUMO3() {
        return EnvioGPS_Consumo3;
    }

    public void setEnvioGPS_Consumo3(String EnvioGPS_Consumo3) {
        this.EnvioGPS_Consumo3 = EnvioGPS_Consumo3;
    }

    public String getEnvioGPS_NROCONCEPTOS() {
        return EnvioGPS_NroConceptos;
    }

    public void setEnvioGPS_NroConceptos(String EnvioGPS_NroConceptos) {
        this.EnvioGPS_NroConceptos = EnvioGPS_NroConceptos;
    }

    public String getEnvioGPS_INDFACTURACION() {
        return EnvioGPS_IndFacturacion;
    }

    public void setEnvioGPS_IndFacturacion(String EnvioGPS_IndFacturacion) {
        this.EnvioGPS_IndFacturacion = EnvioGPS_IndFacturacion;
    }

    public String getEnvioGPS_NROFACTURA() {
        return EnvioGPS_NroFactura;
    }

    public void setEnvioGPS_NroFactura(String EnvioGPS_NroFactura) {
        this.EnvioGPS_NroFactura = EnvioGPS_NroFactura;
    }

    public String getEnvioGPS_FECHAVENCE() {
        return EnvioGPS_FechaVence;
    }

    public void setEnvioGPS_FechaVence(String EnvioGPS_FechaVence) {
        this.EnvioGPS_FechaVence = EnvioGPS_FechaVence;
    }

    public String getEnvioGPS_FECHACORTE() {
        return EnvioGPS_FechaCorte;
    }

    public void setEnvioGPS_FechaCorte(String EnvioGPS_FechaCorte) {
        this.EnvioGPS_FechaCorte = EnvioGPS_FechaCorte;
    }

    public String getEnvioGPS_LECTURAMODIFICADA1() {
        return EnvioGPS_LecturaModificada1;
    }

    public void setEnvioGPS_LecturaModificada1(String EnvioGPS_LecturaModificada1) {
        this.EnvioGPS_LecturaModificada1 = EnvioGPS_LecturaModificada1;
    }

    public String getEnvioGPS_LECTURAMODIFICADA2() {
        return EnvioGPS_LecturaModificada2;
    }

    public void setEnvioGPS_LecturaModificada2(String EnvioGPS_LecturaModificada2) {
        this.EnvioGPS_LecturaModificada2 = EnvioGPS_LecturaModificada2;
    }

    public String getEnvioGPS_DIGITOS() {
        return EnvioGPS_Digitos;
    }

    public void setEnvioGPS_Digitos(String EnvioGPS_Digitos) {
        this.EnvioGPS_Digitos = EnvioGPS_Digitos;
    }

    public String getEnvioGPS_PROCESADO() {
        return EnvioGPS_Procesado;
    }

    public void setEnvioGPS_Procesado(String EnvioGPS_Procesado) {
        this.EnvioGPS_Procesado = EnvioGPS_Procesado;
    }

    public String getEnvioGPS_NUMEROCONCEPTOS() {
        return EnvioGPS_NumeroConceptos;
    }

    public void setEnvioGPS_NumeroConceptos(String EnvioGPS_NumeroConceptos) {
        this.EnvioGPS_NumeroConceptos = EnvioGPS_NumeroConceptos;
    }

    public String getEnvioGPS_PRIMERCONCEPTO() {
        return EnvioGPS_PrimerConcepto;
    }

    public void setEnvioGPS_PrimerConcepto(String EnvioGPS_PrimerConcepto) {
        this.EnvioGPS_PrimerConcepto = EnvioGPS_PrimerConcepto;
    }

    public String getEnvioGPS_CRITIASIEC() {
        return EnvioGPS_CritiaSIEC;
    }

    public void setEnvioGPS_CritiaSIEC(String EnvioGPS_CritiaSIEC) {
        this.EnvioGPS_CritiaSIEC = EnvioGPS_CritiaSIEC;
    }

    public String getEnvioGPS_CODNOVEDAD() {
        return EnvioGPS_CodNovedad;
    }

    public void setEnvioGPS_CodNovedad(String EnvioGPS_CodNovedad) {
        this.EnvioGPS_CodNovedad = EnvioGPS_CodNovedad;
    }

    public String getEnvioGPS_LECTURAANTERIOR() {
        return EnvioGPS_LecturaAnterior;
    }

    public void setEnvioGPS_LecturaAnterior(String EnvioGPS_LecturaAnterior) {
        this.EnvioGPS_LecturaAnterior = EnvioGPS_LecturaAnterior;
    }

    public String getEnvioGPS_ESTADOENVIO() {
        return EnvioGPS_EstadoEnvio;
    }

    public void setEnvioGPS_EstadoEnvio(String EnvioGPS_EstadoEnvio) {
        this.EnvioGPS_EstadoEnvio = EnvioGPS_EstadoEnvio;
    }

    public String getEnvioGPS_CRNL() {
        return EnvioGPS_CRNL;
    }

    public void setEnvioGPS_CRNL(String EnvioGPS_CRNL) {
        this.EnvioGPS_CRNL = EnvioGPS_CRNL;
    }

    public String getEnvioGPS_MARCACONTADOR() {
        return EnvioGPS_MARCACONTADOR;
    }

    public void setEnvioGPS_MARCACONTADOR(String envioGPS_MARCACONTADOR) {
        EnvioGPS_MARCACONTADOR = envioGPS_MARCACONTADOR;
    }

    public String getEnvioGPS_ULTIMOMEDIDORLEIDO() {
        return EnvioGPS_ULTIMOMEDIDORLEIDO;
    }

    public void setEnvioGPS_ULTIMOMEDIDORLEIDO(String envioGPS_ULTIMOMEDIDORLEIDO) {
        EnvioGPS_ULTIMOMEDIDORLEIDO = envioGPS_ULTIMOMEDIDORLEIDO;
    }

    public String getEnvioGPS_MARCALEIDO() {
        return EnvioGPS_MARCALEIDO;
    }

    public void setEnvioGPS_MARCALEIDO(String envioGPS_MARCALEIDO) {
        EnvioGPS_MARCALEIDO = envioGPS_MARCALEIDO;
    }

    public String getEnvioGPS_OBSERVACION1() {
        return EnvioGPS_OBSERVACION1;
    }

    public void setEnvioGPS_OBSERVACION1(String envioGPS_OBSERVACION1) {
        EnvioGPS_OBSERVACION1 = envioGPS_OBSERVACION1;
    }

    public String getEnvioGPS_CLASESERVICIO() {
        return EnvioGPS_CLASESERVICIO;
    }

    public void setEnvioGPS_CLASESERVICIO(String envioGPS_CLASESERVICIO) {
        EnvioGPS_CLASESERVICIO = envioGPS_CLASESERVICIO;
    }

    public String getEnvioGPS_NMEDIDORES() {
        return EnvioGPS_NMEDIDORES;
    }

    public void setEnvioGPS_NMEDIDORES(String envioGPS_NMEDIDORES) {
        EnvioGPS_NMEDIDORES = envioGPS_NMEDIDORES;
    }

    public String getEnvioGPS_NREGISTRADORES() {
        return EnvioGPS_NREGISTRADORES;
    }

    public void setEnvioGPS_NREGISTRADORES(String envioGPS_NREGISTRADORES) {
        EnvioGPS_NREGISTRADORES = envioGPS_NREGISTRADORES;
    }

    public String getEnvioGPS_IDREGISTRO() {
        return EnvioGPS_IDREGISTRO;
    }

    public void setEnvioGPS_IDREGISTRO(String envioGPS_IDREGISTRO) {
        EnvioGPS_IDREGISTRO = envioGPS_IDREGISTRO;
    }

    public String getEnvioGPS_NROCONTADORDB() {
        return EnvioGPS_NROCONTADORDB;
    }

    public void setEnvioGPS_NROCONTADORDB(String EnvioGPS_NROCONTADORDBs) {
        EnvioGPS_NROCONTADORDB = EnvioGPS_NROCONTADORDBs;
    }

    public String getEnvioGPS_VALORFACTURADOASEO() {
        return EnvioGPS_ValorFacturadoAseo;
    }

    // CORREGIDO: antes asignaba el campo a si mismo (parametro "...Asseo") y el valor quedaba null,
    // por eso el plano llegaba al servidor con "null" en el total de aseo.
    public void setEnvioGPS_ValorFacturadoASEO(String valorFacturadoAseo) {
        this.EnvioGPS_ValorFacturadoAseo = valorFacturadoAseo;
    }



    // ------------------------------------------------------------------ apertura / cierre

    public Boolean abrir_EnvioGPS(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.length() == 0) return false;
        synchronized (CANDADO_ARCHIVO) {
            cerrarSilencioso();
            try {
                rFile = new RandomAccessFile(nombreArchivo, "rw");
                archivo_EnvioGPS = nombreArchivo;
                total_EnvioGPS = (int) (rFile.length() / LONGITUD_REGISTRO);
                largoIndexado = -1;
                return true;
            } catch (FileNotFoundException e) {
                Log.e(TAG, "al abrir " + nombreArchivo + ": " + e.getMessage());
                return false;
            } catch (IOException e) {
                Log.e(TAG, "al abrir1 " + e.getMessage());
                return false;
            }
        }
    }

    public void Cerrar_EnvioGPS() {
        synchronized (CANDADO_ARCHIVO) {
            cerrarSilencioso();
        }
    }

    private void cerrarSilencioso() {
        if (rFile == null) return;
        try {
            rFile.close();
        } catch (IOException e) {
            Log.e(TAG, "al cerrar " + e.getMessage());
        }
        rFile = null;
    }

    public void posicion_EnvioGPS(int registro, int tamano) {
        synchronized (CANDADO_ARCHIVO) {
            try {
                rFile.seek((long) (registro - 1) * tamano);
            } catch (IOException e) {
                Log.e(TAG, "seek " + registro + ": " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ escritura

    /**
     * Uso actual de la app: escribir_EnvioGPS(total_EnvioGPS + 1) = AGREGAR.
     * Una posicion mayor que el total que conoce este objeto se toma como AGREGAR al final real del archivo
     * (recalculado aqui, dentro del candado). Una posicion ya existente reescribe esa linea solo si es de la
     * misma cuenta y medidor.
     */
    public boolean escribir_EnvioGPS(int posicion) {
        synchronized (CANDADO_ARCHIVO) {
            if (rFile == null) {
                UtilPlanos.evento(TAG, "[BLOQUEADO ENVIO] archivo cerrado, cuenta " + cuentaActual());
                return false;
            }
            try {
                int lineasCompletas = (int) (rFile.length() / LONGITUD_REGISTRO);
                // total_EnvioGPS + 1 (o mas) es la forma en que la app pide AGREGAR. Ese total puede estar
                // desactualizado si otro flujo agrego entretanto: se agrega al final REAL, nunca encima.
                if (posicion < 1 || posicion > total_EnvioGPS || posicion > lineasCompletas) {
                    return escribirEn(lineasCompletas + 1);        // agregar (pisa una linea final incompleta si la hay)
                }
                if (!esLaMismaLinea(posicion)) return false;
                return escribirEn(posicion);
            } catch (IOException e) {
                Log.e(TAG, "escritura fallida: " + e.getMessage());
                return false;
            }
        }
    }

    /** Forma explicita de agregar una linea al final. */
    public boolean agregar_EnvioGPS() {
        return escribir_EnvioGPS(Integer.MAX_VALUE);
    }

    private boolean esLaMismaLinea(int posicion) throws IOException {
        rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO);
        rFile.readFully(bufferCorto);
        rellenar_EnvioGPS();
        String enDisco = new String(bufferCorto, 23, 28, LATIN1);   // cuenta(7);digito(3);nrocontador(16)
        String enMemoria = EnvioGPS_cuenta + sep + EnvioGPS_digitochequeo + sep + EnvioGPS_NroContador;
        if (!enDisco.equals(enMemoria)) {
            UtilPlanos.evento(TAG, "[BLOQUEADO ENVIO] linea " + posicion + " es de " + enDisco.trim()
                    + " y se intento escribir " + enMemoria.trim());
            return false;
        }
        return true;
    }

    private boolean escribirEn(int posicion) throws IOException {
        rellenar_EnvioGPS();
        String linea = EnvioGPS_ciclo + sep + EnvioGPS_mununicipio + sep + EnvioGPS_seccion + sep + EnvioGPS_departamento + sep + EnvioGPS_anno + sep +
                EnvioGPS_mes + sep + EnvioGPS_cuenta + sep + EnvioGPS_digitochequeo + sep + EnvioGPS_NroContador + sep + EnvioGPS_idcontador + sep +
                EnvioGPS_lecturatomada + sep + EnvioGPS_causadenolectura + sep + EnvioGPS_descanomalia + sep + EnvioGPS_comentario + sep + EnvioGPS_desccomentario + sep +
                EnvioGPS_fechayhoralectura + sep + EnvioGPS_CodLector + sep + EnvioGPS_primermedidor + sep + EnvioGPS_HoraImpresion + sep + EnvioGPS_NombreArchivo + sep +
                EnvioGPS_Longitud + sep + EnvioGPS_Latitud + sep + EnvioGPS_NroSatelites + sep + EnvioGPS_Distancia + sep + EnvioGPS_FechaHoraSatelite + sep +
                EnvioGPS_AltitudSatelite + sep + EnvioGPS_Terminal + sep + EnvioGPS_IndicadorNovedad + sep + EnvioGPS_Indcodbarras + sep + EnvioGPS_Intentos + sep +
                EnvioGPS_criticapda + sep + EnvioGPS_ValorFacturado + sep + EnvioGPS_ConsumoFacturado + sep + EnvioGPS_SubContribucion + sep + EnvioGPS_Consumo1 + sep +
                EnvioGPS_Consumo2 + sep + EnvioGPS_Consumo3 + sep + EnvioGPS_NroConceptos + sep + EnvioGPS_IndFacturacion + sep + EnvioGPS_NroFactura + sep +
                EnvioGPS_FechaVence + sep + EnvioGPS_FechaCorte + sep + EnvioGPS_LecturaModificada1 + sep + EnvioGPS_LecturaModificada2 + sep + EnvioGPS_Digitos + sep +
                EnvioGPS_Procesado + sep + EnvioGPS_NumeroConceptos + sep + EnvioGPS_PrimerConcepto + sep + EnvioGPS_CritiaSIEC + sep + EnvioGPS_CodNovedad + sep +
                EnvioGPS_LecturaAnterior + sep + EnvioGPS_EstadoEnvio
                + sep + EnvioGPS_MARCACONTADOR
                + sep + EnvioGPS_ULTIMOMEDIDORLEIDO
                + sep + EnvioGPS_MARCALEIDO
                + sep + EnvioGPS_OBSERVACION1
                + sep + EnvioGPS_CLASESERVICIO
                + sep + EnvioGPS_NMEDIDORES
                + sep + EnvioGPS_NREGISTRADORES
                + sep + EnvioGPS_IDREGISTRO
                + sep + EnvioGPS_NROCONTADORDB
                + sep + EnvioGPS_ValorFacturadoAseo
                + sep + EnvioGPS_CRNL;
        if (linea.length() != LONGITUD_REGISTRO) {
            UtilPlanos.evento(TAG, "CADENA DAÑADA ENVIOGPRS (longitud " + linea.length() + "):\r\n" + linea);
            return false;
        }
        rFile.seek((long) (posicion - 1) * LONGITUD_REGISTRO);
        rFile.write(linea.getBytes(LATIN1));
        total_EnvioGPS = Math.max(total_EnvioGPS, posicion);
        return true;
    }

    private String cuentaActual() {
        return (EnvioGPS_cuenta == null ? "" : EnvioGPS_cuenta.trim()) + (EnvioGPS_digitochequeo == null ? "" : EnvioGPS_digitochequeo.trim());
    }

    /** Campo nulo o vacio -> espacios; el aseo nulo o "null" -> "0" (el servidor tambien lo acepta como 0). */
    private static String fmt(String formato, String valor) {
        return String.format(formato, valor == null ? "" : valor);
    }

    public void rellenar_EnvioGPS() {
        EnvioGPS_ciclo = fmt("%-3s", EnvioGPS_ciclo);
        EnvioGPS_mununicipio = fmt("%-3s", EnvioGPS_mununicipio);
        EnvioGPS_seccion = fmt("%-3s", EnvioGPS_seccion);
        EnvioGPS_departamento = fmt("%-2s", EnvioGPS_departamento);
        EnvioGPS_anno = fmt("%-4s", EnvioGPS_anno);
        EnvioGPS_mes = fmt("%-2s", EnvioGPS_mes);
        EnvioGPS_cuenta = fmt("%-7s", EnvioGPS_cuenta);
        EnvioGPS_digitochequeo = fmt("%-3s", EnvioGPS_digitochequeo);
        EnvioGPS_NroContador = fmt("%-16s", EnvioGPS_NroContador);
        if (EnvioGPS_idcontador != null && EnvioGPS_idcontador.trim().length() > 3)
            EnvioGPS_idcontador = EnvioGPS_idcontador.substring(0, 3);
        EnvioGPS_idcontador = fmt("%-3s", EnvioGPS_idcontador);
        EnvioGPS_lecturatomada = fmt("%-10s", EnvioGPS_lecturatomada);
        EnvioGPS_causadenolectura = fmt("%-2s", EnvioGPS_causadenolectura);
        EnvioGPS_descanomalia = fmt("%-30s", EnvioGPS_descanomalia);
        EnvioGPS_comentario = fmt("%-2s", EnvioGPS_comentario);
        EnvioGPS_desccomentario = fmt("%-30s", EnvioGPS_desccomentario);
        EnvioGPS_fechayhoralectura = fmt("%-19s", EnvioGPS_fechayhoralectura);
        EnvioGPS_CodLector = fmt("%-10s", EnvioGPS_CodLector);
        EnvioGPS_primermedidor = fmt("%-5s", EnvioGPS_primermedidor);
        EnvioGPS_HoraImpresion = fmt("%-8s", EnvioGPS_HoraImpresion);
        EnvioGPS_NombreArchivo = fmt("%-12s", EnvioGPS_NombreArchivo);
        EnvioGPS_Longitud = fmt("%-20s", EnvioGPS_Longitud);
        EnvioGPS_Latitud = fmt("%-20s", EnvioGPS_Latitud);
        EnvioGPS_NroSatelites = fmt("%-3s", EnvioGPS_NroSatelites);
        EnvioGPS_Distancia = fmt("%-15s", EnvioGPS_Distancia);
        EnvioGPS_FechaHoraSatelite = fmt("%-30s", EnvioGPS_FechaHoraSatelite);
        EnvioGPS_AltitudSatelite = fmt("%-20s", EnvioGPS_AltitudSatelite);
        EnvioGPS_Terminal = fmt("%-15s", EnvioGPS_Terminal);
        EnvioGPS_IndicadorNovedad = fmt("%-1s", EnvioGPS_IndicadorNovedad);
        EnvioGPS_Indcodbarras = fmt("%-1s", EnvioGPS_Indcodbarras);
        EnvioGPS_Intentos = fmt("%-1s", EnvioGPS_Intentos);
        EnvioGPS_criticapda = fmt("%-1s", EnvioGPS_criticapda);
        EnvioGPS_ValorFacturado = fmt("%-10s", EnvioGPS_ValorFacturado);
        EnvioGPS_ConsumoFacturado = fmt("%-10s", EnvioGPS_ConsumoFacturado);
        EnvioGPS_SubContribucion = fmt("%-10s", EnvioGPS_SubContribucion);
        EnvioGPS_Consumo1 = fmt("%-10s", EnvioGPS_Consumo1);
        EnvioGPS_Consumo2 = fmt("%-10s", EnvioGPS_Consumo2);
        EnvioGPS_Consumo3 = fmt("%-10s", EnvioGPS_Consumo3);
        EnvioGPS_NroConceptos = fmt("%-2s", EnvioGPS_NroConceptos);
        EnvioGPS_IndFacturacion = fmt("%-1s", EnvioGPS_IndFacturacion);
        EnvioGPS_NroFactura = fmt("%-15s", EnvioGPS_NroFactura);
        EnvioGPS_FechaVence = fmt("%-10s", EnvioGPS_FechaVence);
        EnvioGPS_FechaCorte = fmt("%-10s", EnvioGPS_FechaCorte);
        EnvioGPS_LecturaModificada1 = fmt("%-10s", EnvioGPS_LecturaModificada1);
        EnvioGPS_LecturaModificada2 = fmt("%-10s", EnvioGPS_LecturaModificada2);
        EnvioGPS_Digitos = fmt("%-1s", EnvioGPS_Digitos);
        EnvioGPS_Procesado = fmt("%-1s", EnvioGPS_Procesado);
        EnvioGPS_NumeroConceptos = fmt("%-2s", EnvioGPS_NumeroConceptos);
        EnvioGPS_PrimerConcepto = fmt("%-6s", EnvioGPS_PrimerConcepto);
        EnvioGPS_CritiaSIEC = fmt("%-2s", EnvioGPS_CritiaSIEC);
        EnvioGPS_CodNovedad = fmt("%-4s", EnvioGPS_CodNovedad);
        EnvioGPS_LecturaAnterior = fmt("%-10s", EnvioGPS_LecturaAnterior);
        EnvioGPS_EstadoEnvio = fmt("%-1s", EnvioGPS_EstadoEnvio);
        EnvioGPS_MARCACONTADOR = fmt("%-12s", EnvioGPS_MARCACONTADOR);
        EnvioGPS_ULTIMOMEDIDORLEIDO = fmt("%-16s", EnvioGPS_ULTIMOMEDIDORLEIDO);
        EnvioGPS_MARCALEIDO = fmt("%-12s", EnvioGPS_MARCALEIDO);
        EnvioGPS_OBSERVACION1 = fmt("%-3s", EnvioGPS_OBSERVACION1);
        EnvioGPS_CLASESERVICIO = fmt("%-3s", EnvioGPS_CLASESERVICIO);
        EnvioGPS_NMEDIDORES = fmt("%-1s", EnvioGPS_NMEDIDORES);
        EnvioGPS_NREGISTRADORES = fmt("%-2s", EnvioGPS_NREGISTRADORES);
        EnvioGPS_IDREGISTRO = fmt("%-10s", EnvioGPS_IDREGISTRO);
        EnvioGPS_NROCONTADORDB = fmt("%-1s", EnvioGPS_NROCONTADORDB);
        String aseo = EnvioGPS_ValorFacturadoAseo == null ? "" : EnvioGPS_ValorFacturadoAseo.trim();
        if (aseo.length() == 0 || aseo.equalsIgnoreCase("null")) aseo = "0";
        EnvioGPS_ValorFacturadoAseo = fmt("%-10s", aseo);
        EnvioGPS_CRNL = "\r\n";
    }

    // ------------------------------------------------------------------ lectura

    public void lectura_EnvioGPS(int registro) {
        synchronized (CANDADO_ARCHIVO) {
            encontro_EnvioGPS = 0;
            if (!leer(registro, bufferLectura)) return;
            byte[] b = bufferLectura;
            texto = null;
            EnvioGPS_ciclo = new String(b, 0, 3, LATIN1);
            EnvioGPS_mununicipio = new String(b, 4, 3, LATIN1);
            EnvioGPS_seccion = new String(b, 8, 3, LATIN1);
            EnvioGPS_departamento = new String(b, 12, 2, LATIN1);
            EnvioGPS_anno = new String(b, 15, 4, LATIN1);
            EnvioGPS_mes = new String(b, 20, 2, LATIN1);
            EnvioGPS_cuenta = new String(b, 23, 7, LATIN1);
            EnvioGPS_digitochequeo = new String(b, 31, 3, LATIN1);
            EnvioGPS_NroContador = new String(b, 35, 16, LATIN1);
            EnvioGPS_idcontador = new String(b, 52, 3, LATIN1);
            EnvioGPS_lecturatomada = new String(b, 56, 10, LATIN1);
            EnvioGPS_causadenolectura = new String(b, 67, 2, LATIN1);
            EnvioGPS_descanomalia = new String(b, 70, 30, LATIN1);
            EnvioGPS_comentario = new String(b, 101, 2, LATIN1);
            EnvioGPS_desccomentario = new String(b, 104, 30, LATIN1);
            EnvioGPS_fechayhoralectura = new String(b, 135, 19, LATIN1);
            EnvioGPS_CodLector = new String(b, 155, 10, LATIN1);
            EnvioGPS_primermedidor = new String(b, 166, 5, LATIN1);
            EnvioGPS_HoraImpresion = new String(b, 172, 8, LATIN1);
            EnvioGPS_NombreArchivo = new String(b, 181, 12, LATIN1);
            EnvioGPS_Longitud = new String(b, 194, 20, LATIN1);
            EnvioGPS_Latitud = new String(b, 215, 20, LATIN1);
            EnvioGPS_NroSatelites = new String(b, 236, 3, LATIN1);
            EnvioGPS_Distancia = new String(b, 240, 15, LATIN1);
            EnvioGPS_FechaHoraSatelite = new String(b, 256, 30, LATIN1);
            EnvioGPS_AltitudSatelite = new String(b, 287, 20, LATIN1);
            EnvioGPS_Terminal = new String(b, 308, 15, LATIN1);
            EnvioGPS_IndicadorNovedad = new String(b, 324, 1, LATIN1);
            EnvioGPS_Indcodbarras = new String(b, 326, 1, LATIN1);
            EnvioGPS_Intentos = new String(b, 328, 1, LATIN1);
            EnvioGPS_criticapda = new String(b, 330, 1, LATIN1);
            EnvioGPS_ValorFacturado = new String(b, 332, 10, LATIN1);
            EnvioGPS_ConsumoFacturado = new String(b, 343, 10, LATIN1);
            EnvioGPS_SubContribucion = new String(b, 354, 10, LATIN1);
            EnvioGPS_Consumo1 = new String(b, 365, 10, LATIN1);
            EnvioGPS_Consumo2 = new String(b, 376, 10, LATIN1);
            EnvioGPS_Consumo3 = new String(b, 387, 10, LATIN1);
            EnvioGPS_NroConceptos = new String(b, 398, 2, LATIN1);
            EnvioGPS_IndFacturacion = new String(b, 401, 1, LATIN1);
            EnvioGPS_NroFactura = new String(b, 403, 15, LATIN1);
            EnvioGPS_FechaVence = new String(b, 419, 10, LATIN1);
            EnvioGPS_FechaCorte = new String(b, 430, 10, LATIN1);
            EnvioGPS_LecturaModificada1 = new String(b, 441, 10, LATIN1);
            EnvioGPS_LecturaModificada2 = new String(b, 452, 10, LATIN1);
            EnvioGPS_Digitos = new String(b, 463, 1, LATIN1);
            EnvioGPS_Procesado = new String(b, 465, 1, LATIN1);
            EnvioGPS_NumeroConceptos = new String(b, 467, 2, LATIN1);
            EnvioGPS_PrimerConcepto = new String(b, 470, 6, LATIN1);
            EnvioGPS_CritiaSIEC = new String(b, 477, 2, LATIN1);
            EnvioGPS_CodNovedad = new String(b, 480, 4, LATIN1);
            EnvioGPS_LecturaAnterior = new String(b, 485, 10, LATIN1);
            EnvioGPS_EstadoEnvio = new String(b, 496, 1, LATIN1);
            EnvioGPS_MARCACONTADOR = new String(b, 498, 12, LATIN1);
            EnvioGPS_ULTIMOMEDIDORLEIDO = new String(b, 511, 16, LATIN1);
            EnvioGPS_MARCALEIDO = new String(b, 528, 12, LATIN1);
            EnvioGPS_OBSERVACION1 = new String(b, 541, 3, LATIN1);
            EnvioGPS_CLASESERVICIO = new String(b, 545, 3, LATIN1);
            EnvioGPS_NMEDIDORES = new String(b, 549, 1, LATIN1);
            EnvioGPS_NREGISTRADORES = new String(b, 551, 2, LATIN1);   // CORREGIDO: antes se guardaba en CLASESERVICIO
            EnvioGPS_IDREGISTRO = new String(b, 554, 10, LATIN1);
            EnvioGPS_NROCONTADORDB = new String(b, 565, 1, LATIN1);
            EnvioGPS_ValorFacturadoAseo = new String(b, 567, 10, LATIN1);
            EnvioGPS_CRNL = new String(b, 578, 2, LATIN1);
            ultimo_EnvioGPS = registro;
        }
    }

    /** Para ConsultaNoEnviados: solo cuenta y numero de contador (lee 51 bytes, no 580). */
    public void lectura_EnvioGPS_Tipo2(int registro) {
        synchronized (CANDADO_ARCHIVO) {
            encontro_EnvioGPS = 0;
            if (!leer(registro, bufferCorto)) return;
            EnvioGPS_cuenta = new String(bufferCorto, 23, 7, LATIN1);
            EnvioGPS_NroContador = new String(bufferCorto, 35, 16, LATIN1);
            ultimo_EnvioGPS = registro;
        }
    }

    private boolean leer(int registro, byte[] destino) {
        if (rFile == null || registro < 1 || registro > total_EnvioGPS) {
            Log.e(TAG, "lectura fuera de rango o archivo cerrado: " + registro + " de " + total_EnvioGPS);
            return false;
        }
        try {
            rFile.seek((long) (registro - 1) * LONGITUD_REGISTRO);
            rFile.readFully(destino);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "lectura fallida en linea " + registro + ": " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------ busqueda (indice)

    private static String llave(String cuentaCompleta, String contador, String idcontador) {
        String id = String.format("%1$3s", idcontador == null ? "" : idcontador.trim()).replace(" ", "0");
        return UtilPlanos.normalizar(cuentaCompleta) + "|" + (contador == null ? "" : contador.trim().toUpperCase()) + "|" + id;
    }

    /** Arma el indice leyendo solo los primeros 55 bytes de cada linea, en bloques. */
    private void construirIndice() throws IOException {
        indice.clear();
        long largo = rFile.length();
        int lineas = (int) (largo / LONGITUD_REGISTRO);
        final int porBloque = 64;
        byte[] bloque = new byte[porBloque * LONGITUD_REGISTRO];
        int linea = 1;
        while (linea <= lineas) {
            int n = Math.min(porBloque, lineas - linea + 1);
            rFile.seek((long) (linea - 1) * LONGITUD_REGISTRO);
            rFile.readFully(bloque, 0, n * LONGITUD_REGISTRO);
            for (int r = 0; r < n; r++) {
                int base = r * LONGITUD_REGISTRO;
                String cuenta = new String(bloque, base + 23, 7, LATIN1).trim() + new String(bloque, base + 31, 3, LATIN1).trim();
                String k = llave(cuenta, new String(bloque, base + 35, 16, LATIN1), new String(bloque, base + 52, 3, LATIN1));
                if (!indice.containsKey(k)) indice.put(k, linea + r);
            }
            linea += n;
        }
        largoIndexado = largo;
        total_EnvioGPS = lineas;
    }

    //por el momento no es requerido la critica es la misma
    public void BuscarSecuencial_EnvioGPS(String codigo, String contador, String idcontador) {
        synchronized (CANDADO_ARCHIVO) {
            encontro_EnvioGPS = 0;
            if (rFile == null) return;
            try {
                if (rFile.length() != largoIndexado) construirIndice();
            } catch (IOException e) {
                Log.e(TAG, "no se pudo indexar: " + e.getMessage());
                return;
            }
            Integer linea = indice.get(llave(codigo, contador, idcontador));
            if (linea != null) {
                lectura_EnvioGPS(linea);
                encontro_EnvioGPS = linea;
            }
        }
    }

}//end
