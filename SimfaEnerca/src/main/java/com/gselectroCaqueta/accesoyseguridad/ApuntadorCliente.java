package com.gselectroCaqueta.accesoyseguridad;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
/// <summary>
/// Descripci�n breve del archivo a generar...apuntadorCliente
/// </summary>

public class ApuntadorCliente {
    static final int LONGITUD_REGISTRO = 19;
    String sep = ";";
    String apuntadorCliente_codigo;
    String apuntadorCliente_apuntador;
    String apuntadorCliente_crnl;
    BufferedReader fin;
    byte[] byteArray;
    String archivo_ApuntadorCliente;
    int total_ApuntadorCliente;
    int ultimo_ApuntadorCliente;
    int encontro_ApuntadorCliente;
    String buscar_ApuntadorCliente;
    String texto;
    // File ruta_sd = Environment.getExternalStorageDirectory();
    // File ruta_sd = new File('C:\\Users\\SGlobal\\Documents\\PruebasJava\\CensoSalida.txt');
    RandomAccessFile rFile;

    public String getapuntadorCliente_CODIGO() {
        return apuntadorCliente_codigo;
    }

    public void setapuntadorCliente_codigo(String apuntadorCliente_codigo) {
        this.apuntadorCliente_codigo = apuntadorCliente_codigo;
    }

    public String getapuntadorCliente_APUNTADOR() {
        return apuntadorCliente_apuntador;
    }

    public void setapuntadorCliente_apuntador(String apuntadorCliente_apuntador) {
        this.apuntadorCliente_apuntador = apuntadorCliente_apuntador;
    }

    public String getapuntadorCliente_CRNL() {
        return apuntadorCliente_crnl;
    }

    public void setapuntadorCliente_crnl(String apuntadorCliente_crnl) {
        this.apuntadorCliente_crnl = apuntadorCliente_crnl;
    }

    public Boolean abrir_ApuntadorCliente(String nombreArchivo) {
        if (nombreArchivo.length() == 0) {
            return false;
        }
        try {
            rFile = new RandomAccessFile(nombreArchivo, "rw"); // C:/Users/SGlobal/Documents/Pruebas Java/CensoSalida.txt
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        archivo_ApuntadorCliente = nombreArchivo;
        return abrir(archivo_ApuntadorCliente);
    }

    private Boolean abrir(String nombre_archivo) {
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, fileSize);
            texto = new String(byteArray);
            total_ApuntadorCliente = fileSize / LONGITUD_REGISTRO;
            return true;
        } catch (Exception ex) {
            // Log.e('Ficheros', 'Error al leer fichero desde tarjeta SD');
            ex.printStackTrace();
            return false;
        }
    }

    public void Cerrar_ApuntadorCliente() {
        // cerrar el archivo abierto
        try {
            rFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean escribir_ApuntadorCliente(int posicion) {
        posicion_ApuntadorCliente(posicion, LONGITUD_REGISTRO);
        rellenar_ApuntadorCliente();
        String texto = apuntadorCliente_codigo + sep + apuntadorCliente_apuntador + sep + apuntadorCliente_crnl;
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

    public void rellenar_ApuntadorCliente() {
        try {
            apuntadorCliente_codigo = String.format("%-9s", apuntadorCliente_codigo);
            apuntadorCliente_apuntador = String.format("%-6s", apuntadorCliente_apuntador);
            apuntadorCliente_crnl = String.format("%-2s", apuntadorCliente_crnl);
        } catch (Exception e) {
            System.out.println("Se presento problema al escribir en el archivo apuntadorCliente.dat..");
            e.printStackTrace();
        }

        return;
    }

    public void posicion_ApuntadorCliente(int registro, int tamano) {
        try {
            rFile.seek((registro - 1) * tamano);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir los registros atual en el archivo en posicion
    public void lectura_ApuntadorCliente(int registro) {
        String[] campos;
        encontro_ApuntadorCliente = 0;
        posicion_ApuntadorCliente(registro, LONGITUD_REGISTRO);
        try {
            int fileSize = (int) rFile.length();
            byteArray = new byte[fileSize];
            rFile.readFully(byteArray, 0, LONGITUD_REGISTRO);
            texto = new String(byteArray);
            setapuntadorCliente_codigo(texto.substring(0, 9));
            setapuntadorCliente_apuntador(texto.substring(10, 16));
            setapuntadorCliente_crnl(texto.substring(17, 19));
            ultimo_ApuntadorCliente = registro;
            // fin estructura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buscarbinario_ApuntadorCliente(String codigo) {
        int salir = 0;
        int i = 0;
        int t = total_ApuntadorCliente;
        int b = 0;
        lectura_ApuntadorCliente(1);
        String uno = codigo.trim();
        String otro = apuntadorCliente_codigo.trim();
        if (otro.equals(uno)) {
            posicion_ApuntadorCliente(i + 1, LONGITUD_REGISTRO);
            encontro_ApuntadorCliente = 1;
        } else {
            lectura_ApuntadorCliente(total_ApuntadorCliente);
            otro = apuntadorCliente_codigo.trim().equals("") ? "0" : apuntadorCliente_codigo.trim();
            if ((Integer.parseInt(uno)) > (Integer.parseInt(otro))) {
                encontro_ApuntadorCliente = 0;
            } else {
                while (salir == 0) {
                    i = (b + t) / 2;
                    lectura_ApuntadorCliente(i + 1);
                    otro = apuntadorCliente_codigo.trim();
                    if (otro.equals(uno)) {
                        encontro_ApuntadorCliente = i;
                        posicion_ApuntadorCliente(i + 1, LONGITUD_REGISTRO);
                        salir = 1;
                    } else {
                        if (b == i) {
                            lectura_ApuntadorCliente(i + 2);
                            otro = apuntadorCliente_codigo.trim();
                            if (uno == otro) {
                                encontro_ApuntadorCliente = i;
                                posicion_ApuntadorCliente(i + 1, LONGITUD_REGISTRO);
                            } else {
                                encontro_ApuntadorCliente = 0;
                            }
                            salir = 1;
                        } else {
                            if (Integer.parseInt(uno) > Integer.parseInt(otro)) {
                                b = i;
                            } else {
                                if (i - b == 1) {
                                    b = i;
                                }
                                t = i;
                            }
                        }
                    }
                }
            }
        }
    }

    public void buscarbinarioChar_ApuntadorCliente(String codigo) {
        int salir = 0;
        int i = 0;
        int t = total_ApuntadorCliente;
        int b = 0;
        lectura_ApuntadorCliente(1);
        String uno = codigo.toUpperCase().trim();
        String otro = apuntadorCliente_codigo.toUpperCase().trim();
        if (otro.compareTo(uno) == 0) {
            posicion_ApuntadorCliente(i + 1, LONGITUD_REGISTRO);
            encontro_ApuntadorCliente = 1;
        } else {
            lectura_ApuntadorCliente(total_ApuntadorCliente);
            otro = apuntadorCliente_codigo.trim().equals("") ? "0" : apuntadorCliente_codigo.toUpperCase().trim();
            if (uno.compareTo(otro) == 1) {
                encontro_ApuntadorCliente = 0;
            } else {
                while (salir == 0) {
                    i = (b + t) / 2;
                    lectura_ApuntadorCliente(i + 1);
                    otro = apuntadorCliente_codigo.toUpperCase().trim();
                    if (uno.compareTo(otro) == 0) {
                        encontro_ApuntadorCliente = i;
                        posicion_ApuntadorCliente(i + 1, LONGITUD_REGISTRO);
                        salir = 1;
                    } else {
                        if (b == i) {
                            lectura_ApuntadorCliente(i + 2);
                            otro = apuntadorCliente_codigo.toUpperCase().trim();
                            if (uno.compareTo(otro) == 0) {
                                encontro_ApuntadorCliente = i;
                                posicion_ApuntadorCliente(i + 1, LONGITUD_REGISTRO);
                            } else {
                                encontro_ApuntadorCliente = 0;
                            }
                            salir = 1;
                        } else {
                            if (uno.compareTo(otro) == 1) {
                                b = i;
                            } else {
                                if (i - b == 1) {
                                    b = i;
                                }
                                t = i;
                            }
                        }
                    }
                }
            }
        }
    }
}
