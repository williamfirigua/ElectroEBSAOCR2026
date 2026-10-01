package com.gselectroCaqueta.tablas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripcion breve del archivo a generar...tablaMedidorEntrada
/// </summary>

public class TablaMedidorEntrada { //Ax esta es E_CONTAD, Ya
    static final int LONGITUD_REGISTRO = 116;//116
    String sep = ";";
    String tablaMedidorEntrada_anio;
    String tablaMedidorEntrada_mes;
    String tablaMedidorEntrada_Cuenta;
    String tablaMedidorEntrada_Marca;
    String tablaMedidorEntrada_NUMero;
    String tablaMedidorEntrada_Tipomedidor;
    String tablaMedidorEntrada_Indfacturacionpr;
    String tablaMedidorEntrada_Estadomedidor;
    String tablaMedidorEntrada_Fechalectanterior;
    String tablaMedidorEntrada_Nroregistros;
    String tablaMedidorEntrada_Fechamedcambiado;
    String tablaMedidorEntrada_primerregistro;
    String tablaMedidorEntrada_cliente;
    String tablaMedidorEntrada_idcortado;
    String tablaMedidorEntrada_lecturacorte;
    String tablaMedidorEntrada_obsercorte;
    String tablaMedidorEntrada_fin;
    BufferedReader fin;
    byte[] byteArray;
    int ultimo_TablaMedidorEntrada;
    String buscar_TablaMedidorEntrada;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;
    private int _fileSize;
    private String archivo_TablaMedidorEntrada;
    private int total_TablaMedidorEntrada;
    private int encontro_TablaMedidorEntrada;

    public String gettablaMedidorEntrada_ANIO() {
        return tablaMedidorEntrada_anio;
    }

    public void settablaMedidorEntrada_anio(String tablaMedidorEntrada_anio) {
        this.tablaMedidorEntrada_anio = tablaMedidorEntrada_anio;
    }

    public String gettablaMedidorEntrada_MES() {
        return tablaMedidorEntrada_mes;
    }

    public void settablaMedidorEntrada_mes(String tablaMedidorEntrada_mes) {
        this.tablaMedidorEntrada_mes = tablaMedidorEntrada_mes;
    }

    public String gettablaMedidorEntrada_CUENTA() {
        return tablaMedidorEntrada_Cuenta;
    }

    public void settablaMedidorEntrada_Cuenta(String tablaMedidorEntrada_Cuenta) {
        this.tablaMedidorEntrada_Cuenta = tablaMedidorEntrada_Cuenta;
    }

    public String gettablaMedidorEntrada_MARCA() {
        return tablaMedidorEntrada_Marca;
    }

    public void settablaMedidorEntrada_Marca(String tablaMedidorEntrada_Marca) {
        this.tablaMedidorEntrada_Marca = tablaMedidorEntrada_Marca;
    }

    public String gettablaMedidorEntrada_NUMERO() {
        return tablaMedidorEntrada_NUMero;
    }

    public void settablaMedidorEntrada_NUMero(String tablaMedidorEntrada_NUMero) {
        this.tablaMedidorEntrada_NUMero = tablaMedidorEntrada_NUMero;
    }

    public String gettablaMedidorEntrada_TIPOMEDIDOR() {
        return tablaMedidorEntrada_Tipomedidor;
    }

    public void settablaMedidorEntrada_Tipomedidor(String tablaMedidorEntrada_Tipomedidor) {
        this.tablaMedidorEntrada_Tipomedidor = tablaMedidorEntrada_Tipomedidor;
    }

    public String gettablaMedidorEntrada_INDFACTURACIONPR() {
        return tablaMedidorEntrada_Indfacturacionpr;
    }

    public void settablaMedidorEntrada_Indfacturacionpr(String tablaMedidorEntrada_Indfacturacionpr) {
        this.tablaMedidorEntrada_Indfacturacionpr = tablaMedidorEntrada_Indfacturacionpr;
    }

    public String gettablaMedidorEntrada_ESTADOMEDIDOR() {
        return tablaMedidorEntrada_Estadomedidor;
    }

    public void settablaMedidorEntrada_Estadomedidor(String tablaMedidorEntrada_Estadomedidor) {
        this.tablaMedidorEntrada_Estadomedidor = tablaMedidorEntrada_Estadomedidor;
    }

    public String gettablaMedidorEntrada_FECHALECTANTERIOR() {
        return tablaMedidorEntrada_Fechalectanterior;
    }

    public void settablaMedidorEntrada_Fechalectanterior(String tablaMedidorEntrada_Fechalectanterior) {
        this.tablaMedidorEntrada_Fechalectanterior = tablaMedidorEntrada_Fechalectanterior;
    }

    public String gettablaMedidorEntrada_NROREGISTROS() {
        return tablaMedidorEntrada_Nroregistros;
    }

    public void settablaMedidorEntrada_Nroregistros(String tablaMedidorEntrada_Nroregistros) {
        this.tablaMedidorEntrada_Nroregistros = tablaMedidorEntrada_Nroregistros;
    }

    public String gettablaMedidorEntrada_FECHAMEDCAMBIADO() {
        return tablaMedidorEntrada_Fechamedcambiado;
    }

    public void settablaMedidorEntrada_Fechamedcambiado(String tablaMedidorEntrada_Fechamedcambiado) {
        this.tablaMedidorEntrada_Fechamedcambiado = tablaMedidorEntrada_Fechamedcambiado;
    }

    public String gettablaMedidorEntrada_PRIMERREGISTRO() {
        return tablaMedidorEntrada_primerregistro;
    }

    public void settablaMedidorEntrada_primerregistro(String tablaMedidorEntrada_primerregistro) {
        this.tablaMedidorEntrada_primerregistro = tablaMedidorEntrada_primerregistro;
    }

    public String gettablaMedidorEntrada_CLIENTE() {
        return tablaMedidorEntrada_cliente;
    }

    public void settablaMedidorEntrada_cliente(String tablaMedidorEntrada_cliente) {
        this.tablaMedidorEntrada_cliente = tablaMedidorEntrada_cliente;
    }

    public String gettablaMedidorEntrada_IDCORTADO() {
        return tablaMedidorEntrada_idcortado;
    }

    public void settablaMedidorEntrada_idcortado(String tablaMedidorEntrada_idcortado) {
        this.tablaMedidorEntrada_idcortado = tablaMedidorEntrada_idcortado;
    }

    public String gettablaMedidorEntrada_LECTURACORTE() {
        return tablaMedidorEntrada_lecturacorte;
    }

    public void settablaMedidorEntrada_lecturacorte(String tablaMedidorEntrada_lecturacorte) {
        this.tablaMedidorEntrada_lecturacorte = tablaMedidorEntrada_lecturacorte;
    }

    public String gettablaMedidorEntrada_OBSERCORTE() {
        return tablaMedidorEntrada_obsercorte;
    }

    public void settablaMedidorEntrada_obsercorte(String tablaMedidorEntrada_obsercorte) {
        this.tablaMedidorEntrada_obsercorte = tablaMedidorEntrada_obsercorte;
    }

    public String gettablaMedidorEntrada_FIN() {
        return tablaMedidorEntrada_fin;
    }

    public void settablaMedidorEntrada_fin(String tablaMedidorEntrada_fin) {
        this.tablaMedidorEntrada_fin = tablaMedidorEntrada_fin;
    }

    public Boolean abrir_TablaMedidorEntrada(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setArchivo_TablaMedidorEntrada(nombreArchivo);
        return abrir(getArchivo_TablaMedidorEntrada());
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            _fileSize = (int) rFile.length();//Ax: fileSize

            if (_fileSize > 0) {
                byteArray = new byte[LONGITUD_REGISTRO];//Ax:  fileSize
                rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);//Ax: fileSize
                // texto = new String(byteArray);
                setTotal_TablaMedidorEntrada(_fileSize / LONGITUD_REGISTRO);
            } else {
                byteArray = new byte[_fileSize];
                rFile.readFully(byteArray, 0, _fileSize);
                setTotal_TablaMedidorEntrada(0);
            }
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_TablaMedidorEntrada() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_TablaMedidorEntrada(int posicion) {
        posicion_TablaMedidorEntrada(posicion, LONGITUD_REGISTRO);
        rellenar_TablaMedidorEntrada();
        String texto = tablaMedidorEntrada_anio + sep + tablaMedidorEntrada_mes + sep + tablaMedidorEntrada_Cuenta + sep + tablaMedidorEntrada_Marca + sep + tablaMedidorEntrada_NUMero + sep
                + tablaMedidorEntrada_Tipomedidor + sep + tablaMedidorEntrada_Indfacturacionpr + sep + tablaMedidorEntrada_Estadomedidor + sep + tablaMedidorEntrada_Fechalectanterior + sep
                + tablaMedidorEntrada_Nroregistros + sep + tablaMedidorEntrada_Fechamedcambiado + sep + tablaMedidorEntrada_primerregistro + sep + tablaMedidorEntrada_cliente + sep
                + tablaMedidorEntrada_idcortado + sep + tablaMedidorEntrada_lecturacorte + sep + tablaMedidorEntrada_obsercorte + sep + tablaMedidorEntrada_fin;
        try {
            if (null != rFile && (texto.length() == LONGITUD_REGISTRO)) {
                rFile.writeBytes(texto);
                System.out.println("Los Datos fueron grabados correctamente");
                return true;
            } else {
                System.out.println("Se presento problema al escribir en el archivo; longitud Errada: tamano texto: " + texto.length() + texto);
                return false;
            }
        } catch (IOException ioe) {
            System.out.println("Se presento problema al escribir en el archivo Longitud Errada: " + LONGITUD_REGISTRO);
            return false;
        }
    }

    public void rellenar_TablaMedidorEntrada() {
        try {
            tablaMedidorEntrada_anio = String.format("%-4s", tablaMedidorEntrada_anio);
            tablaMedidorEntrada_mes = String.format("%-2s", tablaMedidorEntrada_mes);
            tablaMedidorEntrada_Cuenta = String.format("%-9s", tablaMedidorEntrada_Cuenta);
            tablaMedidorEntrada_Marca = String.format("%-12s", tablaMedidorEntrada_Marca);
            tablaMedidorEntrada_NUMero = String.format("%-16s", tablaMedidorEntrada_NUMero);
            tablaMedidorEntrada_Tipomedidor = String.format("%-3s", tablaMedidorEntrada_Tipomedidor);
            tablaMedidorEntrada_Indfacturacionpr = String.format("%-1s", tablaMedidorEntrada_Indfacturacionpr);
            tablaMedidorEntrada_Estadomedidor = String.format("%-1s", tablaMedidorEntrada_Estadomedidor);
            tablaMedidorEntrada_Fechalectanterior = String.format("%-10s", tablaMedidorEntrada_Fechalectanterior);
            tablaMedidorEntrada_Nroregistros = String.format("%-1s", tablaMedidorEntrada_Nroregistros);
            tablaMedidorEntrada_Fechamedcambiado = String.format("%-10s", tablaMedidorEntrada_Fechamedcambiado);
            tablaMedidorEntrada_primerregistro = String.format("%-5s", tablaMedidorEntrada_primerregistro);
            tablaMedidorEntrada_cliente = String.format("%-5s", tablaMedidorEntrada_cliente);
            tablaMedidorEntrada_idcortado = String.format("%-8s", tablaMedidorEntrada_idcortado);
            tablaMedidorEntrada_lecturacorte = String.format("%-8s", tablaMedidorEntrada_lecturacorte);
            tablaMedidorEntrada_obsercorte = String.format("%-2s", tablaMedidorEntrada_obsercorte);
            tablaMedidorEntrada_fin = String.format("%-2s", tablaMedidorEntrada_fin);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo tablaMedidorEntrada.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_TablaMedidorEntrada(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_TablaMedidorEntrada(int registro) {
        //String[] campos;
        setEncontro_TablaMedidorEntrada(0);
        posicion_TablaMedidorEntrada(registro, LONGITUD_REGISTRO);
        try {
            //Ax: int fileSize = (int) rFile.length();
            byteArray = new byte[LONGITUD_REGISTRO];//Ax: _fileSize
            rFile.read(byteArray);//Ax: rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            settablaMedidorEntrada_anio(texto.substring(0, 4));
            settablaMedidorEntrada_mes(texto.substring(5, 7));
            settablaMedidorEntrada_Cuenta(texto.substring(8, 17));
            settablaMedidorEntrada_Marca(texto.substring(18, 30));
            settablaMedidorEntrada_NUMero(texto.substring(31, 47));
            settablaMedidorEntrada_Tipomedidor(texto.substring(48, 51));
            settablaMedidorEntrada_Indfacturacionpr(texto.substring(52, 53));
            settablaMedidorEntrada_Estadomedidor(texto.substring(54, 55));
            settablaMedidorEntrada_Fechalectanterior(texto.substring(56, 66));
            settablaMedidorEntrada_Nroregistros(texto.substring(67, 69));
            settablaMedidorEntrada_Fechamedcambiado(texto.substring(70, 80));
            settablaMedidorEntrada_primerregistro(texto.substring(81, 86));
            settablaMedidorEntrada_cliente(texto.substring(87, 92));
            settablaMedidorEntrada_idcortado(texto.substring(93, 101));
            settablaMedidorEntrada_lecturacorte(texto.substring(102, 110));
            settablaMedidorEntrada_obsercorte(texto.substring(111, 113));
            settablaMedidorEntrada_fin(texto.substring(114, 116));
            ultimo_TablaMedidorEntrada = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarSecuencialecontad(String codigo, int campo) {

        String codigobuscar = "";
        setEncontro_TablaMedidorEntrada(0);

        for (int i = 0; i < (int) (getTotal_TablaMedidorEntrada()); i++) {
            lectura_TablaMedidorEntrada(i + 1);
            if (campo == 0) {
                codigobuscar = tablaMedidorEntrada_NUMero.trim().replace("T", "");
                codigobuscar = codigobuscar.trim().replace("A", "");
                codigobuscar = codigobuscar.trim().replace("U", "");
                if (codigobuscar.length() >= codigo.trim().length()) {

                    codigobuscar = codigobuscar.substring(codigobuscar.length() - codigo.trim().length(), codigo.trim().length());
                }
                codigobuscar = codigobuscar.trim().replace("M", "");


            } else
                codigobuscar = tablaMedidorEntrada_Cuenta.substring(1, codigo.trim().length() + 1);

            // poner la posibilidad
            if (codigo.trim() == codigobuscar || codigobuscar.indexOf(codigo) >= 0) {

                setEncontro_TablaMedidorEntrada(1);
                posicion_TablaMedidorEntrada(i + 1, LONGITUD_REGISTRO);
                i = ((int) (getTotal_TablaMedidorEntrada())) + 1;
            } else {
                setEncontro_TablaMedidorEntrada(0);
            }

        }
        return;
    }
//
//	public void buscarSecuencial_TablaMedidorEntrada(String codigo)
//                {                       
//                        encontro_TablaMedidorEntrada=0;
//                        for (int i = 0; i<(int)(total_TablaMedidorEntrada); i++)
//                        {       
//                                lectura_TablaMedidorEntrada(i+1);
//                                if (Integer.parseInt(codigo.trim())==Integer.parseInt(.trim()))
//                                {       
//                                        encontro_TablaMedidorEntrada = i+1;
//                                        posicion_TablaMedidorEntrada(i+1, LONGITUD_REGISTRO);   
//                                        i = total_TablaMedidorEntrada + 10;
//                                }
//                                else
//                                {
//                                        encontro_TablaMedidorEntrada = 0;
//                                }
//                        }
//                        return;         
//                }

    public int getTotal_TablaMedidorEntrada() {
        return total_TablaMedidorEntrada;
    }

    public void setTotal_TablaMedidorEntrada(int total_TablaMedidorEntrada) {
        this.total_TablaMedidorEntrada = total_TablaMedidorEntrada;
    }

    public String getArchivo_TablaMedidorEntrada() {
        return archivo_TablaMedidorEntrada;
    }

    public void setArchivo_TablaMedidorEntrada(String archivo_TablaMedidorEntrada) {
        this.archivo_TablaMedidorEntrada = archivo_TablaMedidorEntrada;
    }

    public int getEncontro_TablaMedidorEntrada() {
        return encontro_TablaMedidorEntrada;
    }

    public void setEncontro_TablaMedidorEntrada(int encontro_TablaMedidorEntrada) {
        this.encontro_TablaMedidorEntrada = encontro_TablaMedidorEntrada;
    }
}
