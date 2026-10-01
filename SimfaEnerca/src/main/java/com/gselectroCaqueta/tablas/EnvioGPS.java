package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/// <summary>
/// </summary>
public class EnvioGPS {

    static final int LONGITUD_REGISTRO = 569;//567 -- CUENTA de 6 a 7 (+1), digitochequeo se queda en 3, marca se queda en 12
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


    String EnvioGPS_CRNL;
    BufferedReader fin;
    byte[] byteArray;
    int ultimo_EnvioGPS;
    String buscar_EnvioGPS;
    String texto;
    //    File ruta_sd = Environment.getExternalStorageDirectory();
    //    File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    Utils utils = new Utils();
    private int _fileSize;

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

    public void setEnvioGPS_NROCONTADORDB(String EnvioGPS_NROCONTADORDBs) {
        EnvioGPS_NROCONTADORDB = EnvioGPS_NROCONTADORDBs;
    }



    public Boolean abrir_EnvioGPS(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); //C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        archivo_EnvioGPS = nombreArchivo;
        return abrir(archivo_EnvioGPS);
    }


    private Boolean abrir(String nombre_archivo) {
        try {
            _fileSize = (int) rFile.length();//Ax: fileSize

            if (_fileSize > 0) {
                byteArray = new byte[LONGITUD_REGISTRO];//Ax:  fileSize
                rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);//Ax: fileSize
                // texto = new String(byteArray);
                total_EnvioGPS = _fileSize / LONGITUD_REGISTRO;
            } else {
                byteArray = new byte[_fileSize];
                rFile.readFully(byteArray, 0, _fileSize);
                total_EnvioGPS = 0;
            }
            return true;
        } catch (Exception ex) {
            //       Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_EnvioGPS() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_EnvioGPS(int posicion) {
        posicion_EnvioGPS(posicion, LONGITUD_REGISTRO);
        rellenar_EnvioGPS();
        String texto = EnvioGPS_ciclo + sep + EnvioGPS_mununicipio + sep + EnvioGPS_seccion + sep + EnvioGPS_departamento + sep + EnvioGPS_anno + sep +
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

                //nuevos para mayor control de los registros a enviar al siec
                + sep + EnvioGPS_NMEDIDORES
                + sep + EnvioGPS_NREGISTRADORES
                + sep + EnvioGPS_IDREGISTRO
                + sep + EnvioGPS_NROCONTADORDB


                + sep + EnvioGPS_CRNL;
        try {
            Log.e("error",LONGITUD_REGISTRO+" envio gps "+texto.length());
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"); //Ax: EJ: O108010.009
                utils.EscribirLinea(desc,"CADENA DAÑADA ENVIOGPRS:"+"\r\n" + texto);
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamaño texto: " + texto.length());
                return false;
            }
        } catch (IOException ioe) {
            Log.e("error","no envio gps "+ioe.getMessage());
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_EnvioGPS() {
        try {
            EnvioGPS_ciclo = String.format("%-3s", EnvioGPS_ciclo);
            EnvioGPS_mununicipio = String.format("%-3s", EnvioGPS_mununicipio);
            EnvioGPS_seccion = String.format("%-3s", EnvioGPS_seccion);
            EnvioGPS_departamento = String.format("%-2s", EnvioGPS_departamento);
            EnvioGPS_anno = String.format("%-4s", EnvioGPS_anno);
            EnvioGPS_mes = String.format("%-2s", EnvioGPS_mes);
            EnvioGPS_cuenta = String.format("%-7s", EnvioGPS_cuenta);//era 6
            EnvioGPS_digitochequeo = String.format("%-3s", EnvioGPS_digitochequeo);
            EnvioGPS_NroContador = String.format("%-16s", EnvioGPS_NroContador);
            if (EnvioGPS_idcontador.trim().length()>3)
                EnvioGPS_idcontador=EnvioGPS_idcontador.substring(0,3);

            EnvioGPS_idcontador = String.format("%-3s", EnvioGPS_idcontador);
            EnvioGPS_lecturatomada = String.format("%-10s", EnvioGPS_lecturatomada);
            EnvioGPS_causadenolectura = String.format("%-2s", EnvioGPS_causadenolectura);
            EnvioGPS_descanomalia = String.format("%-30s", EnvioGPS_descanomalia);
            EnvioGPS_comentario = String.format("%-2s", EnvioGPS_comentario);
            EnvioGPS_desccomentario = String.format("%-30s", EnvioGPS_desccomentario);
            EnvioGPS_fechayhoralectura = String.format("%-19s", EnvioGPS_fechayhoralectura);
            EnvioGPS_CodLector = String.format("%-10s", EnvioGPS_CodLector);
            EnvioGPS_primermedidor = String.format("%-5s", EnvioGPS_primermedidor);
            EnvioGPS_HoraImpresion = String.format("%-8s", EnvioGPS_HoraImpresion);
            EnvioGPS_NombreArchivo = String.format("%-12s", EnvioGPS_NombreArchivo);
            EnvioGPS_Longitud = String.format("%-20s", EnvioGPS_Longitud);
            EnvioGPS_Latitud = String.format("%-20s", EnvioGPS_Latitud);
            EnvioGPS_NroSatelites = String.format("%-3s", EnvioGPS_NroSatelites);
            EnvioGPS_Distancia = String.format("%-15s", EnvioGPS_Distancia);
            EnvioGPS_FechaHoraSatelite = String.format("%-30s", EnvioGPS_FechaHoraSatelite);
            EnvioGPS_AltitudSatelite = String.format("%-20s", EnvioGPS_AltitudSatelite);
            EnvioGPS_Terminal = String.format("%-15s", EnvioGPS_Terminal);
            EnvioGPS_IndicadorNovedad = String.format("%-1s", EnvioGPS_IndicadorNovedad);
            EnvioGPS_Indcodbarras = String.format("%-1s", EnvioGPS_Indcodbarras);
            EnvioGPS_Intentos = String.format("%-1s", EnvioGPS_Intentos);
            EnvioGPS_criticapda = String.format("%-1s", EnvioGPS_criticapda);
            EnvioGPS_ValorFacturado = String.format("%-10s", EnvioGPS_ValorFacturado);
            EnvioGPS_ConsumoFacturado = String.format("%-10s", EnvioGPS_ConsumoFacturado);
            EnvioGPS_SubContribucion = String.format("%-10s", EnvioGPS_SubContribucion);
            EnvioGPS_Consumo1 = String.format("%-10s", EnvioGPS_Consumo1);
            EnvioGPS_Consumo2 = String.format("%-10s", EnvioGPS_Consumo2);
            EnvioGPS_Consumo3 = String.format("%-10s", EnvioGPS_Consumo3);
            EnvioGPS_NroConceptos = String.format("%-2s", EnvioGPS_NroConceptos);
            EnvioGPS_IndFacturacion = String.format("%-1s", EnvioGPS_IndFacturacion);
            EnvioGPS_NroFactura = String.format("%-15s", EnvioGPS_NroFactura);
            EnvioGPS_FechaVence = String.format("%-10s", EnvioGPS_FechaVence);
            EnvioGPS_FechaCorte = String.format("%-10s", EnvioGPS_FechaCorte);
            EnvioGPS_LecturaModificada1 = String.format("%-10s", EnvioGPS_LecturaModificada1);
            EnvioGPS_LecturaModificada2 = String.format("%-10s", EnvioGPS_LecturaModificada2);
            EnvioGPS_Digitos = String.format("%-1s", EnvioGPS_Digitos);
            EnvioGPS_Procesado = String.format("%-1s", EnvioGPS_Procesado);
            EnvioGPS_NumeroConceptos = String.format("%-2s", EnvioGPS_NumeroConceptos);
            EnvioGPS_PrimerConcepto = String.format("%-6s", EnvioGPS_PrimerConcepto);
            EnvioGPS_CritiaSIEC = String.format("%-2s", EnvioGPS_CritiaSIEC);
            EnvioGPS_CodNovedad = String.format("%-4s", EnvioGPS_CodNovedad);
            EnvioGPS_LecturaAnterior = String.format("%-10s", EnvioGPS_LecturaAnterior);
            EnvioGPS_EstadoEnvio = String.format("%-1s", EnvioGPS_EstadoEnvio);

            EnvioGPS_MARCACONTADOR =  String.format("%-12s", EnvioGPS_MARCACONTADOR);
            EnvioGPS_ULTIMOMEDIDORLEIDO =  String.format("%-16s", EnvioGPS_ULTIMOMEDIDORLEIDO);
            EnvioGPS_MARCALEIDO =   String.format("%-12s", EnvioGPS_MARCALEIDO);
            EnvioGPS_OBSERVACION1 = String.format("%-3s", EnvioGPS_OBSERVACION1);
            EnvioGPS_CLASESERVICIO = String.format("%-3s", EnvioGPS_CLASESERVICIO);

            //nuevos para mayor control de los registros a enviar al siec
            EnvioGPS_NMEDIDORES = String.format("%-1s", EnvioGPS_NMEDIDORES);
            EnvioGPS_NREGISTRADORES = String.format("%-2s", EnvioGPS_NREGISTRADORES);
            EnvioGPS_IDREGISTRO = String.format("%-10s", EnvioGPS_IDREGISTRO);
            EnvioGPS_NROCONTADORDB = String.format("%-1s", EnvioGPS_NROCONTADORDB);

            EnvioGPS_CRNL = "\r\n";


        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo EnvioGPS.dat..");
            e.printStackTrace();
        }
        return;
    }

    public void posicion_EnvioGPS(int registro, int tamaño) {
        try {
            rFile.seek((registro - 1) * tamaño);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Escribir los registros actual en el archivo en posicion
    public void lectura_EnvioGPS(int registro) {
        //String[] campos;
        encontro_EnvioGPS = 0;
        posicion_EnvioGPS(registro, LONGITUD_REGISTRO);
        try {
            //Ax: int fileSize = (int) rFile.length();
            byteArray = new byte[LONGITUD_REGISTRO];//Ax: _fileSize
            rFile.read(byteArray);//Ax: rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            setEnvioGPS_ciclo(texto.substring(0, 3));
            setEnvioGPS_mununicipio(texto.substring(4, 7));
            setEnvioGPS_seccion(texto.substring(8, 11));
            setEnvioGPS_departamento(texto.substring(12, 14));
            setEnvioGPS_anno(texto.substring(15, 19));
            setEnvioGPS_mes(texto.substring(20, 22));
            setEnvioGPS_cuenta(texto.substring(23, 30));//7 (era 6)
            setEnvioGPS_digitochequeo(texto.substring(31, 34));
            setEnvioGPS_NroContador(texto.substring(35, 51));
            setEnvioGPS_idcontador(texto.substring(52, 55));
            setEnvioGPS_lecturatomada(texto.substring(56, 66));
            setEnvioGPS_causadenolectura(texto.substring(67, 69));
            setEnvioGPS_descanomalia(texto.substring(70, 100));
            setEnvioGPS_comentario(texto.substring(101, 103));
            setEnvioGPS_desccomentario(texto.substring(104, 134));
            setEnvioGPS_fechayhoralectura(texto.substring(135, 154));
            setEnvioGPS_CodLector(texto.substring(155, 165));
            setEnvioGPS_primermedidor(texto.substring(166, 171));
            setEnvioGPS_HoraImpresion(texto.substring(172, 180));
            setEnvioGPS_NombreArchivo(texto.substring(181, 193));
            setEnvioGPS_Longitud(texto.substring(194, 214));
            setEnvioGPS_Latitud(texto.substring(215, 235));
            setEnvioGPS_NroSatelites(texto.substring(236, 239));
            setEnvioGPS_Distancia(texto.substring(240, 255));
            setEnvioGPS_FechaHoraSatelite(texto.substring(256, 286));
            setEnvioGPS_AltitudSatelite(texto.substring(287, 307));
            setEnvioGPS_Terminal(texto.substring(308, 323));
            setEnvioGPS_IndicadorNovedad(texto.substring(324, 325));
            setEnvioGPS_Indcodbarras(texto.substring(326, 327));
            setEnvioGPS_Intentos(texto.substring(328, 329));
            setEnvioGPS_criticapda(texto.substring(330, 331));
            setEnvioGPS_ValorFacturado(texto.substring(332, 342));
            setEnvioGPS_ConsumoFacturado(texto.substring(343, 353));
            setEnvioGPS_SubContribucion(texto.substring(354, 364));
            setEnvioGPS_Consumo1(texto.substring(365, 375));
            setEnvioGPS_Consumo2(texto.substring(376, 386));
            setEnvioGPS_Consumo3(texto.substring(387, 397));
            setEnvioGPS_NroConceptos(texto.substring(398, 400));
            setEnvioGPS_IndFacturacion(texto.substring(401, 402));
            setEnvioGPS_NroFactura(texto.substring(403, 418));
            setEnvioGPS_FechaVence(texto.substring(419, 429));
            setEnvioGPS_FechaCorte(texto.substring(430, 440));
            setEnvioGPS_LecturaModificada1(texto.substring(441, 451));
            setEnvioGPS_LecturaModificada2(texto.substring(452, 462));
            setEnvioGPS_Digitos(texto.substring(463, 464));
            setEnvioGPS_Procesado(texto.substring(465, 466));
            setEnvioGPS_NumeroConceptos(texto.substring(467, 469));
            setEnvioGPS_PrimerConcepto(texto.substring(470, 476));
            setEnvioGPS_CritiaSIEC(texto.substring(476+1, 478+1));
            setEnvioGPS_CodNovedad(texto.substring(479+1, 483+1));
            setEnvioGPS_LecturaAnterior(texto.substring(484+1, 494+1));
            setEnvioGPS_EstadoEnvio(texto.substring(495+1, 496+1));
            setEnvioGPS_MARCACONTADOR(texto.substring(497+1, 509+1));
            setEnvioGPS_ULTIMOMEDIDORLEIDO(texto.substring(510+1, 526+1));
            setEnvioGPS_MARCALEIDO(texto.substring(527+1, 539+1));
            setEnvioGPS_OBSERVACION1(texto.substring(540+1, 543+1));
            setEnvioGPS_CLASESERVICIO(texto.substring(544+1, 547+1));
            setEnvioGPS_NMEDIDORES(texto.substring(548+1, 549+1));
            setEnvioGPS_CLASESERVICIO(texto.substring(550+1, 552+1));
            setEnvioGPS_IDREGISTRO(texto.substring(553+1, 563+1));
            setEnvioGPS_NROCONTADORDB(texto.substring(564+1, 565+1));
            setEnvioGPS_CRNL(texto.substring(566+1, 568+1));

            ultimo_EnvioGPS = registro;
            //fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Escribir los registros actual en el archivo en posicion
    public void lectura_EnvioGPS_Tipo2(int registro) //Ax: es para no escribir toodos los registros que no se necesitan en el modulo consultanoenviados
    {
        encontro_EnvioGPS = 0;
        posicion_EnvioGPS(registro, LONGITUD_REGISTRO);
        try {
            byteArray = new byte[LONGITUD_REGISTRO];
            rFile.read(byteArray);
            texto = new String(byteArray);
            setEnvioGPS_cuenta(texto.substring(23, 30));//7 (era 6)
            setEnvioGPS_NroContador(texto.substring(35, 51));
            ultimo_EnvioGPS = registro;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //por el momento no es requerido la critica es la misma
    public void BuscarSecuencial_EnvioGPS(String codigo, String contador, String idcontador) {
        encontro_EnvioGPS = 0;
        //buffer_EnvioGPS.Initialize();
        for (int i = 0; i < (int) (total_EnvioGPS); i++) {
            lectura_EnvioGPS(i + 1);

            String idcont = String.format("%1$3s", idcontador.trim()).replace(" ", "0");
            String idcont2 = String.format("%1$3s", EnvioGPS_idcontador.trim()).replace(" ", "0");
            String CuentaReal = EnvioGPS_cuenta.trim() + EnvioGPS_digitochequeo.trim();
            if (Double.parseDouble(codigo.trim()) == Double.parseDouble(CuentaReal) && contador.trim().toUpperCase().equals(EnvioGPS_NroContador.trim().toUpperCase()) && idcont.equals(idcont2)) {
                encontro_EnvioGPS = i + 1;
                posicion_EnvioGPS(i + 1, LONGITUD_REGISTRO);
                i = (total_EnvioGPS) + 10;
            } else {
                encontro_EnvioGPS = 0;
            }
        }
        return;
    }

}//end