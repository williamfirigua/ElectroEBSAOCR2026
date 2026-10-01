package com.gselectroCaqueta.tablas;

import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gsutil.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaClienteSalida
/// </summary>

public class TablaClienteSalida {
    String sep = ";";
    String tablaClienteSalida_ANIO;
    String tablaClienteSalida_MES;
    String tablaClienteSalida_CUENTA;
    String tablaClienteSalida_ACTIVIDAD;
    String tablaClienteSalida_INDFACTURACION;
    String tablaClienteSalida_NROIMPRESIONES;
    String tablaClienteSalida_IMPRESORA;
    String tablaClienteSalida_LECTOR;
    String tablaClienteSalida_TERMINAL;
    String tablaClienteSalida_VALORFACTURADO;
    String tablaClienteSalida_CONTRIBUCIONENERGIA;
    private String tablaClienteSalida_CONSUMOFACTURADO;
    String tablaClienteSalida_HORAIMPRESION;
    String tablaClienteSalida_FECHAIMPRESION;
    String tablaClienteSalida_Nromedidores;
    String tablaClienteSalida_primermedidor;
    String tablaClienteSalida_consumo1;
    String tablaClienteSalida_consumo2;
    String tablaClienteSalida_consumo3;
    String tablaClienteSalida_valor1;
    String tablaClienteSalida_valor2;
    String tablaClienteSalida_valor3;
    String tablaClienteSalida_fechavence;
    String tablaClienteSalida_fechacorte;
    String tablaClienteSalida_nombrefoto;
    String tablaClienteSalida_distanciacalculada;
    String tablaClienteSalida_FIN;

    public String gettablaClienteSalida_ANIO() {
        return tablaClienteSalida_ANIO;
    }

    public void settablaClienteSalida_ANIO(String tablaClienteSalida_ANIO) {
        this.tablaClienteSalida_ANIO = tablaClienteSalida_ANIO;
    }

    public String gettablaClienteSalida_MES() {
        return tablaClienteSalida_MES;
    }

    public void settablaClienteSalida_MES(String tablaClienteSalida_MES) {
        this.tablaClienteSalida_MES = tablaClienteSalida_MES;
    }

    public String gettablaClienteSalida_CUENTA() {
        return tablaClienteSalida_CUENTA;
    }

    public void settablaClienteSalida_CUENTA(String tablaClienteSalida_CUENTA) {
        this.tablaClienteSalida_CUENTA = tablaClienteSalida_CUENTA;
    }

    public String gettablaClienteSalida_ACTIVIDAD() {
        return tablaClienteSalida_ACTIVIDAD;
    }

    public void settablaClienteSalida_ACTIVIDAD(String tablaClienteSalida_ACTIVIDAD) {
        this.tablaClienteSalida_ACTIVIDAD = tablaClienteSalida_ACTIVIDAD;
    }

    public String gettablaClienteSalida_INDFACTURACION() {
        return tablaClienteSalida_INDFACTURACION;
    }

    public void settablaClienteSalida_INDFACTURACION(String tablaClienteSalida_INDFACTURACION) {
        this.tablaClienteSalida_INDFACTURACION = tablaClienteSalida_INDFACTURACION;
    }

    public String gettablaClienteSalida_NROIMPRESIONES() {
        return tablaClienteSalida_NROIMPRESIONES;
    }

    public void settablaClienteSalida_NROIMPRESIONES(String tablaClienteSalida_NROIMPRESIONES) {
        this.tablaClienteSalida_NROIMPRESIONES = tablaClienteSalida_NROIMPRESIONES;
    }

    public String gettablaClienteSalida_IMPRESORA() {
        return tablaClienteSalida_IMPRESORA;
    }

    public void settablaClienteSalida_IMPRESORA(String tablaClienteSalida_IMPRESORA) {
        this.tablaClienteSalida_IMPRESORA = tablaClienteSalida_IMPRESORA;
    }

    public String gettablaClienteSalida_LECTOR() {
        return tablaClienteSalida_LECTOR;
    }

    public void settablaClienteSalida_LECTOR(String tablaClienteSalida_LECTOR) {
        this.tablaClienteSalida_LECTOR = tablaClienteSalida_LECTOR;
    }

    public String gettablaClienteSalida_TERMINAL() {
        return tablaClienteSalida_TERMINAL;
    }

    public void settablaClienteSalida_TERMINAL(String tablaClienteSalida_TERMINAL) {
        this.tablaClienteSalida_TERMINAL = tablaClienteSalida_TERMINAL;
    }

    public String gettablaClienteSalida_VALORFACTURADO() {
        return tablaClienteSalida_VALORFACTURADO;
    }

    public void settablaClienteSalida_VALORFACTURADO(String tablaClienteSalida_VALORFACTURADO) {
        this.tablaClienteSalida_VALORFACTURADO = tablaClienteSalida_VALORFACTURADO;
    }

    public String gettablaClienteSalida_CONTRIBUCIONENERGIA() {
        return tablaClienteSalida_CONTRIBUCIONENERGIA;
    }

    public void settablaClienteSalida_CONTRIBUCIONENERGIA(String tablaClienteSalida_CONTRIBUCIONENERGIA) {
        this.tablaClienteSalida_CONTRIBUCIONENERGIA = tablaClienteSalida_CONTRIBUCIONENERGIA;
    }

    public String gettablaClienteSalida_CONSUMOFACTURADO() {
        return getTablaClienteSalida_CONSUMOFACTURADO();
    }

    public void settablaClienteSalida_CONSUMOFACTURADO(String tablaClienteSalida_CONSUMOFACTURADO) {
        this.setTablaClienteSalida_CONSUMOFACTURADO(tablaClienteSalida_CONSUMOFACTURADO);
    }

    public String gettablaClienteSalida_HORAIMPRESION() {
        return tablaClienteSalida_HORAIMPRESION;
    }

    public void settablaClienteSalida_HORAIMPRESION(String tablaClienteSalida_HORAIMPRESION) {
        this.tablaClienteSalida_HORAIMPRESION = tablaClienteSalida_HORAIMPRESION;
    }

    public String gettablaClienteSalida_FECHAIMPRESION() {
        return tablaClienteSalida_FECHAIMPRESION;
    }

    public void settablaClienteSalida_FECHAIMPRESION(String tablaClienteSalida_FECHAIMPRESION) {
        this.tablaClienteSalida_FECHAIMPRESION = tablaClienteSalida_FECHAIMPRESION;
    }

    public String gettablaClienteSalida_NROMEDIDORES() {
        return tablaClienteSalida_Nromedidores;
    }

    public void settablaClienteSalida_Nromedidores(String tablaClienteSalida_Nromedidores) {
        this.tablaClienteSalida_Nromedidores = tablaClienteSalida_Nromedidores;
    }

    public String gettablaClienteSalida_PRIMERMEDIDOR() {
        return tablaClienteSalida_primermedidor;
    }

    public void settablaClienteSalida_primermedidor(String tablaClienteSalida_primermedidor) {
        this.tablaClienteSalida_primermedidor = tablaClienteSalida_primermedidor;
    }

    public String gettablaClienteSalida_CONSUMO1() {
        return tablaClienteSalida_consumo1;
    }

    public void settablaClienteSalida_consumo1(String tablaClienteSalida_consumo1) {
        this.tablaClienteSalida_consumo1 = tablaClienteSalida_consumo1;
    }

    public String gettablaClienteSalida_CONSUMO2() {
        return tablaClienteSalida_consumo2;
    }

    public void settablaClienteSalida_consumo2(String tablaClienteSalida_consumo2) {
        this.tablaClienteSalida_consumo2 = tablaClienteSalida_consumo2;
    }

    public String gettablaClienteSalida_CONSUMO3() {
        return tablaClienteSalida_consumo3;
    }

    public void settablaClienteSalida_consumo3(String tablaClienteSalida_consumo3) {
        this.tablaClienteSalida_consumo3 = tablaClienteSalida_consumo3;
    }

    public String gettablaClienteSalida_VALOR1() {
        return tablaClienteSalida_valor1;
    }

    public void settablaClienteSalida_valor1(String tablaClienteSalida_valor1) {
        this.tablaClienteSalida_valor1 = tablaClienteSalida_valor1;
    }

    public String gettablaClienteSalida_VALOR2() {
        return tablaClienteSalida_valor2;
    }

    public void settablaClienteSalida_valor2(String tablaClienteSalida_valor2) {
        this.tablaClienteSalida_valor2 = tablaClienteSalida_valor2;
    }

    public String gettablaClienteSalida_VALOR3() {
        return tablaClienteSalida_valor3;
    }

    public void settablaClienteSalida_valor3(String tablaClienteSalida_valor3) {
        this.tablaClienteSalida_valor3 = tablaClienteSalida_valor3;
    }

    public String gettablaClienteSalida_FECHAVENCE() {
        return tablaClienteSalida_fechavence;
    }

    public void settablaClienteSalida_fechavence(String tablaClienteSalida_fechavence) {
        this.tablaClienteSalida_fechavence = tablaClienteSalida_fechavence;
    }

    public String gettablaClienteSalida_FECHACORTE() {
        return tablaClienteSalida_fechacorte;
    }

    public void settablaClienteSalida_fechacorte(String tablaClienteSalida_fechacorte) {
        this.tablaClienteSalida_fechacorte = tablaClienteSalida_fechacorte;
    }

    public String gettablaClienteSalida_NOMBREFOTO() {
        return tablaClienteSalida_nombrefoto;
    }

    public void settablaClienteSalida_nombrefoto(String tablaClienteSalida_nombrefoto) {
        this.tablaClienteSalida_nombrefoto = tablaClienteSalida_nombrefoto;
    }

    public String gettablaClienteSalida_DISTANCIACALCULADA() {
        return tablaClienteSalida_distanciacalculada;
    }

    public void settablaClienteSalida_distanciacalculada(String tablaClienteSalida_distanciacalculada) {
        this.tablaClienteSalida_distanciacalculada = tablaClienteSalida_distanciacalculada;
    }

    public String gettablaClienteSalida_FIN() {
        return tablaClienteSalida_FIN;
    }

    public void settablaClienteSalida_FIN(String tablaClienteSalida_FIN) {
        this.tablaClienteSalida_FIN = tablaClienteSalida_FIN;
    }

    public String getArchivo_TablaClienteSalida() {
        return archivo_TablaClienteSalida;
    }

    public void setArchivo_TablaClienteSalida(String archivo_TablaClienteSalida) {
        this.archivo_TablaClienteSalida = archivo_TablaClienteSalida;
    }

    private int _fileSize;

    BufferedReader fin;
    byte[] byteArray;
    String archivo_TablaClienteSalida;
    static final int LONGITUD_REGISTRO = 247;
    private int total_TablaClienteSalida;
    int ultimo_TablaClienteSalida;
    int encontro_TablaClienteSalida;
    String buscar_TablaClienteSalida;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new
    // File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    Utils utils = new Utils();

    public Boolean abrir_TablaClienteSalida(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas																// Java/CensoSalida.txt

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        archivo_TablaClienteSalida = nombreArchivo;
        return abrir(archivo_TablaClienteSalida);
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            _fileSize = (int) rFile.length();//Ax: fileSize

            if(_fileSize>0) {
                byteArray = new byte[LONGITUD_REGISTRO];//Ax:  fileSize
                rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);//Ax: fileSize
                // texto = new String(byteArray);
                setTotal_TablaClienteSalida( _fileSize / LONGITUD_REGISTRO);
            }else{
                byteArray = new byte[_fileSize];
                rFile.readFully(byteArray, 0, _fileSize);
                setTotal_TablaClienteSalida(0);
            }
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaClienteSalida() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaClienteSalida(int posicion) {
        posicion_TablaClienteSalida(posicion, LONGITUD_REGISTRO);
        rellenar_TablaClienteSalida();
        String texto = tablaClienteSalida_ANIO + sep + tablaClienteSalida_MES + sep + tablaClienteSalida_CUENTA + sep + tablaClienteSalida_ACTIVIDAD + sep + tablaClienteSalida_INDFACTURACION + sep
                + tablaClienteSalida_NROIMPRESIONES + sep + tablaClienteSalida_IMPRESORA + sep + tablaClienteSalida_LECTOR + sep + tablaClienteSalida_TERMINAL + sep
                + tablaClienteSalida_VALORFACTURADO + sep + tablaClienteSalida_CONTRIBUCIONENERGIA + sep + tablaClienteSalida_CONSUMOFACTURADO + sep + tablaClienteSalida_HORAIMPRESION + sep
                + tablaClienteSalida_FECHAIMPRESION + sep + tablaClienteSalida_Nromedidores + sep + tablaClienteSalida_primermedidor + sep + tablaClienteSalida_consumo1 + sep
                + tablaClienteSalida_consumo2 + sep + tablaClienteSalida_consumo3 + sep + tablaClienteSalida_valor1 + sep + tablaClienteSalida_valor2 + sep + tablaClienteSalida_valor3 + sep
                + tablaClienteSalida_fechavence + sep + tablaClienteSalida_fechacorte + sep + tablaClienteSalida_nombrefoto + sep + tablaClienteSalida_distanciacalculada + sep
                + tablaClienteSalida_FIN;
      //  Log.e("error","cliente salida "+texto);
//Log.e("error",texto.length()+" cliente salida "+LONGITUD_REGISTRO);
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO))
            {
                rFile.writeBytes(texto);
                //System.out.println("Los Datos fueron grabados correctamente");
                return true;
            }
            else
            {
                int cantidadtex=texto.length();
                File desc = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"); //Ax: EJ: O108010.009
                utils.EscribirLinea(desc,"CADENA DAÑADA CLIENTE:"+"\r\n" + texto);
                //rFile.writeBytes(texto);
                //System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamano texto: " + texto.length() + texto);
                return false;
            }
        } catch (IOException ioe)
        {
            //System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO+" Llegaron "+ (""+texto.length()).trim());
            //return false;
        }
        return false;
    }

    public void rellenar_TablaClienteSalida() {
        try {
            tablaClienteSalida_ANIO = String.format("%-4s", tablaClienteSalida_ANIO);
            tablaClienteSalida_MES = String.format("%-2s", tablaClienteSalida_MES);
            tablaClienteSalida_CUENTA = String.format("%-9s", tablaClienteSalida_CUENTA);
            tablaClienteSalida_ACTIVIDAD = String.format("%-4s", tablaClienteSalida_ACTIVIDAD);
            tablaClienteSalida_INDFACTURACION = String.format("%-1s", tablaClienteSalida_INDFACTURACION);
            tablaClienteSalida_NROIMPRESIONES = String.format("%-1s", tablaClienteSalida_NROIMPRESIONES);
            tablaClienteSalida_IMPRESORA = String.format("%-15s", tablaClienteSalida_IMPRESORA);
            tablaClienteSalida_LECTOR = String.format("%-10s", tablaClienteSalida_LECTOR);
            tablaClienteSalida_TERMINAL = String.format("%-15s", tablaClienteSalida_TERMINAL);
            tablaClienteSalida_VALORFACTURADO = String.format("%-10s", tablaClienteSalida_VALORFACTURADO);
            tablaClienteSalida_CONTRIBUCIONENERGIA = String.format("%-10s", tablaClienteSalida_CONTRIBUCIONENERGIA);
            setTablaClienteSalida_CONSUMOFACTURADO(String.format("%-10s", getTablaClienteSalida_CONSUMOFACTURADO()));
            tablaClienteSalida_HORAIMPRESION = String.format("%-6s", tablaClienteSalida_HORAIMPRESION);
            tablaClienteSalida_FECHAIMPRESION = String.format("%-8s", tablaClienteSalida_FECHAIMPRESION);
            tablaClienteSalida_Nromedidores = String.format("%-1s", tablaClienteSalida_Nromedidores);
            tablaClienteSalida_primermedidor = String.format("%-5s", tablaClienteSalida_primermedidor);
            tablaClienteSalida_consumo1 = String.format("%-10s", tablaClienteSalida_consumo1);
            tablaClienteSalida_consumo2 = String.format("%-10s", tablaClienteSalida_consumo2);
            tablaClienteSalida_consumo3 = String.format("%-10s", tablaClienteSalida_consumo3);
            tablaClienteSalida_valor1 = String.format("%-10s", tablaClienteSalida_valor1);
            tablaClienteSalida_valor2 = String.format("%-10s", tablaClienteSalida_valor2);
            tablaClienteSalida_valor3 = String.format("%-10s", tablaClienteSalida_valor3);
            tablaClienteSalida_fechavence = String.format("%-10s", tablaClienteSalida_fechavence);
            tablaClienteSalida_fechacorte = String.format("%-10s", tablaClienteSalida_fechacorte);
            tablaClienteSalida_nombrefoto = String.format("%-18s", tablaClienteSalida_nombrefoto);
            tablaClienteSalida_distanciacalculada = String.format("%-10s", tablaClienteSalida_distanciacalculada);
            tablaClienteSalida_FIN = String.format("%-2s", tablaClienteSalida_FIN);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaClienteSalida.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaClienteSalida(int registro, int tamano) {
        try {
            if (registro == 0) registro++;
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaClienteSalida(int registro) {
        //String[] campos;
        encontro_TablaClienteSalida = 0;
        posicion_TablaClienteSalida(registro, LONGITUD_REGISTRO);
        try {
            //Ax: int fileSize = (int) rFile.length();
            byteArray = new byte[LONGITUD_REGISTRO];//Ax: _fileSize
            rFile.read(byteArray);//Ax: rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaClienteSalida_ANIO(texto.substring(0, 4));
            settablaClienteSalida_MES(texto.substring(5, 7));
            settablaClienteSalida_CUENTA(texto.substring(8, 17));
            settablaClienteSalida_ACTIVIDAD(texto.substring(18, 22));
            settablaClienteSalida_INDFACTURACION(texto.substring(23, 24));
            settablaClienteSalida_NROIMPRESIONES(texto.substring(25, 26));
            settablaClienteSalida_IMPRESORA(texto.substring(27, 42));
            settablaClienteSalida_LECTOR(texto.substring(43, 53));
            settablaClienteSalida_TERMINAL(texto.substring(54, 69));
            settablaClienteSalida_VALORFACTURADO(texto.substring(70, 80));
            settablaClienteSalida_CONTRIBUCIONENERGIA(texto.substring(81, 91));
            settablaClienteSalida_CONSUMOFACTURADO(texto.substring(92, 102));
            settablaClienteSalida_HORAIMPRESION(texto.substring(103, 109));
            settablaClienteSalida_FECHAIMPRESION(texto.substring(110, 118));
            settablaClienteSalida_Nromedidores(texto.substring(119, 120));
            settablaClienteSalida_primermedidor(texto.substring(121, 126));
            settablaClienteSalida_consumo1(texto.substring(127, 137));
            settablaClienteSalida_consumo2(texto.substring(138, 148));
            settablaClienteSalida_consumo3(texto.substring(149, 159));
            settablaClienteSalida_valor1(texto.substring(160, 170));
            settablaClienteSalida_valor2(texto.substring(171, 181));
            settablaClienteSalida_valor3(texto.substring(182, 192));
            settablaClienteSalida_fechavence(texto.substring(193, 203));
            settablaClienteSalida_fechacorte(texto.substring(204, 214));
            settablaClienteSalida_nombrefoto(texto.substring(215, 233));
            settablaClienteSalida_distanciacalculada(texto.substring(234, 244));
            settablaClienteSalida_FIN(texto.substring(245, 247));
            ultimo_TablaClienteSalida = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-------------------------------------------Ax : Bloque creado para Acelerar resumen....
    public void inicializaBloque() {
        try {
            File filepaths = new File(getArchivo_TablaClienteSalida());
            rdr = new LineNumberReader(new FileReader(filepaths));
        } catch (Exception d) {
            d.printStackTrace();
        }
    }

    public LineNumberReader rdr;
    public void lectura_TablaClienteSalidaII(int x) {
        encontro_TablaClienteSalida = 0;
        posicion_TablaClienteSalida(x, LONGITUD_REGISTRO);
        try {
            byteArray = new byte[LONGITUD_REGISTRO];//Ax: _fileSize
            rFile.read(byteArray);//Ax: rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaClienteSalida_CUENTA(texto.substring(8, 17));
            settablaClienteSalida_INDFACTURACION(texto.substring(23, 24));//--
            ultimo_TablaClienteSalida = x;
        } catch (Exception d) {
            d.printStackTrace();
        }
    }

    /*public void lectura_TablaClienteSalidaII(int x) {
        try {
            encontro_TablaClienteSalida = 0;

            for (String line = null; (line = rdr.readLine()) != null; ) {
                if (rdr.getLineNumber() >= x) {
                    //sb1=line;
                    settablaClienteSalida_CUENTA(line.substring(8, 17));
                    settablaClienteSalida_INDFACTURACION(line.substring(23, 24));//--
                    ultimo_TablaClienteSalida = x;
                    return;
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
        }
    }*/

    public void terminaBloque() {
        try {
            rdr.close();
        } catch (Exception d) {
            d.printStackTrace();
        }
    }
    //---------------------------------------------------------------------------------------

    public void buscarSecuencial_TablaClienteSalida(String codigo) {
        encontro_TablaClienteSalida = 0;
        for (int i = 0; i < (int) (getTotal_TablaClienteSalida()); i++) {
            lectura_TablaClienteSalida(i + 1);
            if (Integer.parseInt(codigo.trim()) == Integer.parseInt(tablaClienteSalida_LECTOR.trim())) {
                encontro_TablaClienteSalida = i + 1;
                posicion_TablaClienteSalida(i + 1, LONGITUD_REGISTRO);
                i = getTotal_TablaClienteSalida() + 10;
            } else {
                encontro_TablaClienteSalida = 0;
            }
        }
        return;
    }

    public String getTablaClienteSalida_CONSUMOFACTURADO() {
        return tablaClienteSalida_CONSUMOFACTURADO;
    }

    public void setTablaClienteSalida_CONSUMOFACTURADO(String tablaClienteSalida_CONSUMOFACTURADO) {
        this.tablaClienteSalida_CONSUMOFACTURADO = tablaClienteSalida_CONSUMOFACTURADO;
    }

    public int getTotal_TablaClienteSalida() {
        return total_TablaClienteSalida;
    }

    public void setTotal_TablaClienteSalida(int total_TablaClienteSalida) {
        this.total_TablaClienteSalida = total_TablaClienteSalida;
    }

    // public void buscarbinario_TablaClienteSalida(String codigo)
    // {
    // int salir=0;
    // int i = 0;
    // int t = total_TablaClienteSalida;
    // int b = 0;
    // lectura_TablaClienteSalida(1);
    // String uno = codigo.trim();
    // String otro = .trim();
    // if (otro.equals(uno))
    // {
    // posicion_TablaClienteSalida(i+1,LONGITUD_REGISTRO);
    // encontro_TablaClienteSalida=1;
    // }
    // else
    // {
    // lectura_TablaClienteSalida(total_TablaClienteSalida);
    // otro = .trim().equals("")?"0":.trim();
    // if ((Integer.parseInt(uno)) > (Integer.parseInt(otro)))
    // {
    // encontro_TablaClienteSalida=0;
    // }
    // else
    // {
    // while(salir==0)
    // {
    // i = (b + t)/2;
    // lectura_TablaClienteSalida(i+1);
    // otro = .trim();
    // if (otro.equals(uno))
    // {
    // encontro_TablaClienteSalida=i;
    // posicion_TablaClienteSalida(i+1,LONGITUD_REGISTRO);
    // salir=1;
    // }
    // else
    // {
    // if (b == i)
    // {
    // lectura_TablaClienteSalida(i+2);
    // otro = .trim();
    // if (uno == otro)
    // {
    // encontro_TablaClienteSalida=i;
    // posicion_TablaClienteSalida(i+1,LONGITUD_REGISTRO);
    // }
    // else
    // {
    // encontro_TablaClienteSalida = 0;
    // }
    // salir=1;
    // }
    // else
    // {
    // if (Integer.parseInt(uno) > Integer.parseInt(otro))
    // {
    // b = i;
    // }
    // else
    // {
    // if (i-b==1)
    // {
    // b=i;
    // }
    // t = i;
    // }
    // }
    // }
    // }
    // }
    // }
    // }
    //
    // public void buscarbinarioChar_TablaClienteSalida(String codigo)
    // {
    // int salir=0;
    // int i = 0;
    // int t = total_TablaClienteSalida;
    // int b = 0;
    // lectura_TablaClienteSalida(1);
    // String uno = codigo.toUpperCase().trim();
    // String otro = .toUpperCase().trim();
    // if (otro.compareTo(uno) == 0)
    // {
    // posicion_TablaClienteSalida(i+1,LONGITUD_REGISTRO);
    // encontro_TablaClienteSalida=1;
    // }
    // else {
    // lectura_TablaClienteSalida(total_TablaClienteSalida);
    // otro = .trim().equals("")?"0":.toUpperCase().trim();
    // if (uno.compareTo(otro) == 1 )
    // {
    // encontro_TablaClienteSalida = 0;
    // }
    // else
    // {
    // while(salir==0)
    // {
    // i = (b + t)/2;
    // lectura_TablaClienteSalida(i+1);
    // otro = .toUpperCase().trim();
    // if (uno.compareTo(otro) == 0)
    // {
    // encontro_TablaClienteSalida = i;
    // posicion_TablaClienteSalida(i+1,LONGITUD_REGISTRO);
    // salir=1;
    // }
    // else
    // {
    // if (b == i)
    // {
    // lectura_TablaClienteSalida(i+2);
    // otro = .toUpperCase().trim();
    // if (uno.compareTo(otro) == 0)
    // {
    // encontro_TablaClienteSalida=i;
    // posicion_TablaClienteSalida(i+1,LONGITUD_REGISTRO);
    // }
    // else
    // {
    // encontro_TablaClienteSalida=0;
    // }
    // salir=1;
    // }
    // else
    // {
    // if (uno.compareTo(otro) == 1)
    // {
    // b = i;
    // }
    // else
    // {
    // if (i-b==1)
    // {
    // b=i;
    // }
    // t = i;
    // }
    // }
    // }
    // }
    // }
    // }
    // }
}
