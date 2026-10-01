package com.gselectroCaqueta.tablas;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class Generica {

    public int desde;
    public int hasta;
    public File nuevoFile;
    private File originalFile;

    /**
     * Trata de abrir un archivo que puede fallar por problemas en su extension uppercase o lowercase
     *
     * @return
     */
    private String comprobarNombreExtension(String x) {

        try {
            RandomAccessFile w = new RandomAccessFile(x, "rw");//esta parte del codigo revienta si esta mal la extension
            int fileSize = (int) w.length();
            byte[] byteArray = new byte[fileSize];
            w.readFully(byteArray, 0, fileSize);
            String texto = new String(byteArray);
            w.close();

            return x;
        } catch (Exception ex) {
            String y = x.substring(x.lastIndexOf(".", x.length()));

            if (y.equals(y.toLowerCase())) { //minusculas
                x = x.replace(y, y.toUpperCase());

            } else if (y.equals(y.toUpperCase())) {
                x = x.replace(y, y.toLowerCase());
            }
            return x;
        }
    }

    public String getOriginalFile() {
        return originalFile.getAbsolutePath();
    }

    public void setOriginalFile(String originalFile) {
        this.originalFile = new File(comprobarNombreExtension(originalFile));
    }

    public String getNuevoFile() {
        return nuevoFile.getAbsolutePath();
    }

    public void setNuevoFile(String nuevoFile) {
        this.nuevoFile = new File(nuevoFile);
    }

    //Ax: Obtiene el numero de lineas de un archivo
    public int getLines() {

        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(getOriginalFile()));
            lnr.skip(Long.MAX_VALUE);
            lnr.close();
            return lnr.getLineNumber() + 1; //se añade 1 porque inicia en 0

        } catch (Exception ex) {
            return 0;
        }
    }

    //Ax: Lee una linea en una posicion dada
    public String ReadFileByPos(int pos) {

        LineNumberReader rdr;

        try {
            rdr = new LineNumberReader(new FileReader(originalFile));
            String line;
            for (line = null; (line = rdr.readLine()) != null; ) {
                if (rdr.getLineNumber() >= pos) {
                    break;
                }
            }
            rdr.close();
            return line;
        } catch (Exception d) {
            d.printStackTrace();
            return null;
        }
    }

    /***
     * Ax: Revisa una cadena dentro de un archivo (A), mientras crea una copia (B) y opcionalmente otro archivo(C)
     * si se encuentra la cadena escribe el archivo (C) con la cadena remplazada
     * si se encuentra la cadena escribe el archivo (B) con la cadena remplazada
     * si No se encuentra la cadena escribe el archivo (B) de todas maneras pero con la cadena original
     * Se borra (A) y se le coloca el nombre  de (A) a (B)
     *
     * @param busca  cadena por remplazar
     * @param cambia cadena que remplaza
     * @param crea   opcion de crear o no el tercer archivo (C)
     * @return if return null-> no problem
     */
    public String WriteFileByPos(String busca, String cambia, boolean crea) {
        try {

            if (!originalFile.exists() || originalFile.length() < 4) {
                return null;
            }

            if (nuevoFile.exists()) {

                if (nuevoFile.delete() && crea) {

                    if (!nuevoFile.createNewFile()) { //trata de crearlo, si falla return ...

                        return "No se puedo crear" + nuevoFile.getAbsolutePath();
                    }
                }
            }

            PrintWriter pwNew = null;

            File temporalFile = new File(originalFile.getAbsolutePath() + ".tmp");

            BufferedReader br = new BufferedReader(new FileReader(originalFile));
            PrintWriter pwTmp = new PrintWriter(new FileWriter(temporalFile), true); //Ax: Flush

            if (crea) {

                pwNew = new PrintWriter(new FileWriter(nuevoFile, true), true); //Ax: Append), Flush

            }
            int contador = 0;
            String cadena = "";
            String line = null;

            while ((line = br.readLine()) != null) {

                cadena = line.substring(desde, hasta);
                if (cadena.equals(busca) || (cadena.equals(cambia) && crea)) { //Si encuentra EE o NO, el ultimo para metro es para no hacer posibles archivos nuevos en la tercera parte "_3"

                    contador++;
                    line = line.substring(0, desde) + cambia + line.substring(hasta, line.length());
                    pwTmp.print(line + "\r\n");
                    pwTmp.flush();

                    if (crea) {
                        pwNew.print(line + "\r\n");
                        pwNew.flush();
                    }
                } else {
                    pwTmp.print(line + "\r\n");
                    pwTmp.flush();
                }
            }
            pwTmp.close();

            if (crea) {
                pwNew.close();
            }

            br.close();

            if (contador > 0) {

                if (!originalFile.delete()) {
                    return ("No se pudo borrar" + originalFile.getName());
                    //return;
                }

                if (!temporalFile.renameTo(originalFile)) {
                    return ("No se pudo renombrar" + temporalFile.getName());
                }
                return "1";
            } else {
                temporalFile.delete();
            }
            return "";

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        } catch (IOException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }
}
