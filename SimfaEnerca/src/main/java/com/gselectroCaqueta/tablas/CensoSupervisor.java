package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CensoSupervisor {
    static final int LONGITUD_REGISTRO = 522;
    public String textoTemporal;//Ax es para guardar la linea completa en el metodo II
    public String archivo_CensoSupervisor;
    public int total_CensoSupervisor;
    String sep = ";";
    String CensoSupervisor_ciclo;
    String CensoSupervisor_anno;
    String CensoSupervisor_mes;
    String CensoSupervisor_cuenta;
    String CensoSupervisor_nombreusuario;
    String CensoSupervisor_direccion;
    String CensoSupervisor_fecha;
    String CensoSupervisor_hora;
    String CensoSupervisor_telefono;
    String CensoSupervisor_barrio;
    String CensoSupervisor_lector;
    String CensoSupervisor_contador;
    String CensoSupervisor_lectura;
    String CensoSupervisor_entregasi;
    String CensoSupervisor_dificilsi;
    String CensoSupervisor_facturasi;
    String CensoSupervisor_etiquetasi;
    String CensoSupervisor_dotacion;
    String CensoSupervisor_codlectorcomercial;
    String CensoSupervisor_enviado;
    String CensoSupervisor_observacion;
    String CensoSupervisor_longitud;
    String CensoSupervisor_latitud;
    String CensoSupervisor_CRNL;
    BufferedReader fin;
    byte[] byteArray;
    int ultimo_CensoSupervisor;
    int encontro_CensoSupervisor;
    String buscar_CensoSupervisor;
    String texto;
    RandomAccessFile rFile;

    public String getCensoSupervisor_CICLO() {
        return CensoSupervisor_ciclo;
    }

    public void setCensoSupervisor_ciclo(String CensoSupervisor_ciclo) {
        this.CensoSupervisor_ciclo = CensoSupervisor_ciclo;
    }

    public String getCensoSupervisor_ANNO() {
        return CensoSupervisor_anno;
    }

    public void setCensoSupervisor_anno(String CensoSupervisor_anno) {
        this.CensoSupervisor_anno = CensoSupervisor_anno;
    }

    public String getCensoSupervisor_MES() {
        return CensoSupervisor_mes;
    }

    public void setCensoSupervisor_mes(String CensoSupervisor_mes) {
        this.CensoSupervisor_mes = CensoSupervisor_mes;
    }

    public String getCensoSupervisor_CUENTA() {
        return CensoSupervisor_cuenta;
    }

    public void setCensoSupervisor_cuenta(String CensoSupervisor_cuenta) {
        this.CensoSupervisor_cuenta = CensoSupervisor_cuenta;
    }

    public String getCensoSupervisor_NOMBREUSUARIO() {
        return CensoSupervisor_nombreusuario;
    }

    public void setCensoSupervisor_nombreusuario(String CensoSupervisor_nombreusuario) {
        this.CensoSupervisor_nombreusuario = CensoSupervisor_nombreusuario;
    }

    public String getCensoSupervisor_DIRECCION() {
        return CensoSupervisor_direccion;
    }

    public void setCensoSupervisor_direccion(String CensoSupervisor_direccion) {
        this.CensoSupervisor_direccion = CensoSupervisor_direccion;
    }

    public String getCensoSupervisor_FECHA() {
        return CensoSupervisor_fecha;
    }

    public void setCensoSupervisor_fecha(String CensoSupervisor_fecha) {
        this.CensoSupervisor_fecha = CensoSupervisor_fecha;
    }

    public String getCensoSupervisor_HORA() {
        return CensoSupervisor_hora;
    }

    public void setCensoSupervisor_hora(String CensoSupervisor_hora) {
        this.CensoSupervisor_hora = CensoSupervisor_hora;
    }

    public String getCensoSupervisor_TELEFONO() {
        return CensoSupervisor_telefono;
    }

    public void setCensoSupervisor_telefono(String CensoSupervisor_telefono) {
        this.CensoSupervisor_telefono = CensoSupervisor_telefono;
    }

    public String getCensoSupervisor_BARRIO() {
        return CensoSupervisor_barrio;
    }

    public void setCensoSupervisor_barrio(String CensoSupervisor_barrio) {
        this.CensoSupervisor_barrio = CensoSupervisor_barrio;
    }

    public String getCensoSupervisor_LECTOR() {
        return CensoSupervisor_lector;
    }

    public void setCensoSupervisor_lector(String CensoSupervisor_lector) {
        this.CensoSupervisor_lector = CensoSupervisor_lector;
    }

    public String getCensoSupervisor_CONTADOR() {
        return CensoSupervisor_contador;
    }

    public void setCensoSupervisor_contador(String CensoSupervisor_contador) {
        this.CensoSupervisor_contador = CensoSupervisor_contador;
    }

    public String getCensoSupervisor_LECTURA() {
        return CensoSupervisor_lectura;
    }

    public void setCensoSupervisor_lectura(String CensoSupervisor_lectura) {
        this.CensoSupervisor_lectura = CensoSupervisor_lectura;
    }

    public String getCensoSupervisor_ENTREGASI() {
        return CensoSupervisor_entregasi;
    }

    public void setCensoSupervisor_entregasi(String CensoSupervisor_entregasi) {
        this.CensoSupervisor_entregasi = CensoSupervisor_entregasi;
    }

    public String getCensoSupervisor_DIFICILSI() {
        return CensoSupervisor_dificilsi;
    }

    public void setCensoSupervisor_dificilsi(String CensoSupervisor_dificilsi) {
        this.CensoSupervisor_dificilsi = CensoSupervisor_dificilsi;
    }

    public String getCensoSupervisor_FACTURASI() {
        return CensoSupervisor_facturasi;
    }

    public void setCensoSupervisor_facturasi(String CensoSupervisor_facturasi) {
        this.CensoSupervisor_facturasi = CensoSupervisor_facturasi;
    }

    public String getCensoSupervisor_ETIQUETASI() {
        return CensoSupervisor_etiquetasi;
    }

    public void setCensoSupervisor_etiquetasi(String CensoSupervisor_etiquetasi) {
        this.CensoSupervisor_etiquetasi = CensoSupervisor_etiquetasi;
    }

    public String getCensoSupervisor_DOTACION() {
        return CensoSupervisor_dotacion;
    }

    public void setCensoSupervisor_dotacion(String CensoSupervisor_dotacion) {
        this.CensoSupervisor_dotacion = CensoSupervisor_dotacion;
    }

    public String getCensoSupervisor_CODLECTORCOMERCIAL() {
        return CensoSupervisor_codlectorcomercial;
    }

    public void setCensoSupervisor_codlectorcomercial(String CensoSupervisor_codlectorcomercial) {
        this.CensoSupervisor_codlectorcomercial = CensoSupervisor_codlectorcomercial;
    }

    public String getCensoSupervisor_ENVIADO() {
        return CensoSupervisor_enviado;
    }

    public void setCensoSupervisor_enviado(String CensoSupervisor_enviado) {
        this.CensoSupervisor_enviado = CensoSupervisor_enviado;
    }

    public String getCensoSupervisor_OBSERVACION() {
        return CensoSupervisor_observacion;
    }

    public void setCensoSupervisor_observacion(String CensoSupervisor_observacion) {
        this.CensoSupervisor_observacion = CensoSupervisor_observacion;
    }

    public String getCensoSupervisor_LONGITUD() {
        return CensoSupervisor_longitud;
    }

    public void setCensoSupervisor_longitud(String CensoSupervisor_longitud) {
        this.CensoSupervisor_longitud = CensoSupervisor_longitud;
    }

    public String getCensoSupervisor_LATITUD() {
        return CensoSupervisor_latitud;
    }

    public void setCensoSupervisor_latitud(String CensoSupervisor_latitud) {
        this.CensoSupervisor_latitud = CensoSupervisor_latitud;
    }

    public String getCensoSupervisor_CRNL() {
        return CensoSupervisor_CRNL;
    }

    public void setCensoSupervisor_CRNL(String CensoSupervisor_CRNL) {
        this.CensoSupervisor_CRNL = CensoSupervisor_CRNL;
    }

    public Boolean abrir_CensoSupervisor(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); //C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        archivo_CensoSupervisor = nombreArchivo;
        return abrir(archivo_CensoSupervisor);
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_CensoSupervisor = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            //       Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_CensoSupervisor() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_CensoSupervisor(int posicion) {
        posicion_CensoSupervisor(posicion, LONGITUD_REGISTRO);
        rellenar_CensoSupervisor();
        String texto = CensoSupervisor_ciclo + sep + CensoSupervisor_anno + sep + CensoSupervisor_mes + sep + CensoSupervisor_cuenta + sep + CensoSupervisor_nombreusuario + sep +
                CensoSupervisor_direccion + sep + CensoSupervisor_fecha + sep + CensoSupervisor_hora + sep + CensoSupervisor_telefono + sep + CensoSupervisor_barrio + sep +
                CensoSupervisor_lector + sep + CensoSupervisor_contador + sep + CensoSupervisor_lectura + sep + CensoSupervisor_entregasi + sep + CensoSupervisor_dificilsi + sep +
                CensoSupervisor_facturasi + sep + CensoSupervisor_etiquetasi + sep + CensoSupervisor_dotacion + sep + CensoSupervisor_codlectorcomercial + sep + CensoSupervisor_enviado + sep +
                CensoSupervisor_observacion + sep + CensoSupervisor_longitud + sep + CensoSupervisor_latitud + sep + CensoSupervisor_CRNL;

        textoTemporal = texto; //Ax: para escribir copia
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                //System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamaño texto: " + texto.length());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println("Se presento problema al escribir en el archivo Longitud Errada: "+ LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_CensoSupervisor() {
        try {
            CensoSupervisor_ciclo = String.format("%-4s", CensoSupervisor_ciclo);
            CensoSupervisor_anno = String.format("%-4s", CensoSupervisor_anno);
            CensoSupervisor_mes = String.format("%-2s", CensoSupervisor_mes);
            CensoSupervisor_cuenta = String.format("%-9s", CensoSupervisor_cuenta);
            CensoSupervisor_nombreusuario = String.format("%-48s", CensoSupervisor_nombreusuario);
            CensoSupervisor_direccion = String.format("%-64s", CensoSupervisor_direccion);
            CensoSupervisor_fecha = String.format("%-10s", CensoSupervisor_fecha);
            CensoSupervisor_hora = String.format("%-8s", CensoSupervisor_hora);
            CensoSupervisor_telefono = String.format("%-20s", CensoSupervisor_telefono);
            CensoSupervisor_barrio = String.format("%-30s", CensoSupervisor_barrio);
            CensoSupervisor_lector = String.format("%-10s", CensoSupervisor_lector);
            CensoSupervisor_contador = String.format("%-16s", CensoSupervisor_contador);
            CensoSupervisor_lectura = String.format("%-10s", CensoSupervisor_lectura);
            CensoSupervisor_entregasi = String.format("%-2s", CensoSupervisor_entregasi);
            CensoSupervisor_dificilsi = String.format("%-2s", CensoSupervisor_dificilsi);
            CensoSupervisor_facturasi = String.format("%-2s", CensoSupervisor_facturasi);
            CensoSupervisor_etiquetasi = String.format("%-2s", CensoSupervisor_etiquetasi);
            CensoSupervisor_dotacion = String.format("%-2s", CensoSupervisor_dotacion);
            CensoSupervisor_codlectorcomercial = String.format("%-10s", CensoSupervisor_codlectorcomercial);
            CensoSupervisor_enviado = String.format("%-2s", CensoSupervisor_enviado);
            CensoSupervisor_observacion = String.format("%-200s", CensoSupervisor_observacion);
            CensoSupervisor_longitud = String.format("%-20s", CensoSupervisor_longitud);
            CensoSupervisor_latitud = String.format("%-20s", CensoSupervisor_latitud);
            CensoSupervisor_CRNL = "\r\n";//= String.format("%-2s", CensoSupervisor_CRNL);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo CensoSupervisor.dat..");
            e.printStackTrace();
        }
        return;
    }

    public void posicion_CensoSupervisor(int registro, int tamaño) {
        try {
            int x = (registro - 1) * tamaño;
            rFile.seek(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Escribir los registros atual en el archivo en posicion
    public void lectura_CensoSupervisor(int registro) {
        //String[] campos;
        encontro_CensoSupervisor = 0;
        posicion_CensoSupervisor(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            setCensoSupervisor_ciclo(texto.substring(0, 4));
            setCensoSupervisor_anno(texto.substring(5, 9));
            setCensoSupervisor_mes(texto.substring(10, 12));
            setCensoSupervisor_cuenta(texto.substring(13, 22));
            setCensoSupervisor_nombreusuario(texto.substring(23, 71));
            setCensoSupervisor_direccion(texto.substring(72, 136));
            setCensoSupervisor_fecha(texto.substring(137, 147));
            setCensoSupervisor_hora(texto.substring(148, 156));
            setCensoSupervisor_telefono(texto.substring(157, 177));
            setCensoSupervisor_barrio(texto.substring(178, 208));
            setCensoSupervisor_lector(texto.substring(209, 219));
            setCensoSupervisor_contador(texto.substring(220, 236));
            setCensoSupervisor_lectura(texto.substring(237, 247));
            setCensoSupervisor_entregasi(texto.substring(248, 250));
            setCensoSupervisor_dificilsi(texto.substring(251, 253));
            setCensoSupervisor_facturasi(texto.substring(254, 256));
            setCensoSupervisor_etiquetasi(texto.substring(257, 259));
            setCensoSupervisor_dotacion(texto.substring(260, 262));
            setCensoSupervisor_codlectorcomercial(texto.substring(263, 273));
            setCensoSupervisor_enviado(texto.substring(274, 276));
            setCensoSupervisor_observacion(texto.substring(277, 477));
            setCensoSupervisor_longitud(texto.substring(478, 498));
            setCensoSupervisor_latitud(texto.substring(499, 519));
            setCensoSupervisor_CRNL(texto.substring(520, 522));
            ultimo_CensoSupervisor = registro;
            //fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Escribir los registros atual en el archivo en posicion
    public void lectura_CensoSupervisorII(int registro) //Ax: se usa en Menu de liquidacion, esto deberia cambiarse, es inncesario
    {
        encontro_CensoSupervisor = 0;
        posicion_CensoSupervisor(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            textoTemporal = texto;
            setCensoSupervisor_enviado(texto.substring(274, 276));
            ultimo_CensoSupervisor = registro;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencial_CensoSupervisor(String codigo) {
        encontro_CensoSupervisor = 0;
        for (int i = 0; i < (int) (total_CensoSupervisor); i++) {
            lectura_CensoSupervisor(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(CensoSupervisor_cuenta.trim())) {
                encontro_CensoSupervisor = i + 1;
                posicion_CensoSupervisor(i + 1, LONGITUD_REGISTRO);
                i = total_CensoSupervisor + 10;
            } else {
                encontro_CensoSupervisor = 0;
            }
        }
        return;
    }
}