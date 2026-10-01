package com.gsutil;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Ax on 09/12/2015.
 */
public class Utils {

    private static final int BUFFER = 80000;

    /**
     * @param stAlfanum : cadena con posible contenido alfanumérico < 10 tamano
     * @return : cadena solo numérica
     */
    public String SoloTxtEnteros(String stAlfanum) {

        for (char shar : stAlfanum.toCharArray()) {
            try {
                Long.parseLong(shar + "");
            } catch (NumberFormatException ex) {
                stAlfanum = stAlfanum.replace("" + shar, "|");//no perder posiciones loop
            }
        }
        stAlfanum = stAlfanum.replace("|", "");
        return stAlfanum;
    }

    /**
     * @param stAlfanum : cadena con posible contenido alfanumérico
     * @param tamano    : tamano maximo de la cadena
     * @return : cadena solo numérica con largo = tamano
     */
    public String SoloTxtEnteros(String stAlfanum, int tamano) {

        if (stAlfanum.length() > tamano) {
            stAlfanum = stAlfanum.substring(0, tamano);
            return stAlfanum;
        }

        for (char shar : stAlfanum.toCharArray()) {
            try {
                Long.parseLong(shar + "");//Tiene su limite
            } catch (NumberFormatException ex) {
                stAlfanum = stAlfanum.replace("" + shar, "|");//no perder posiciones loop
            }
        }
        stAlfanum = stAlfanum.replace("|", "");
        return stAlfanum;
    }

    /**
     * @param stAlfanum  : cadena con posible contenido alfanumérico
     * @param caracteres : caracteres que son permitidos menos pipe
     * @param tamano     : tamano maximo de la cadena
     * @return : cadena solo numérica con largo = tamano
     */
    public String SoloTxtEnteros(String stAlfanum, char[] caracteres, int tamano) { //Ax: sin probar

        if (stAlfanum.length() > tamano) {
            stAlfanum = stAlfanum.substring(0, tamano);
            return stAlfanum;
        }

        boolean isChar = true;

        for (char shar : stAlfanum.toCharArray()) {
            try {
                Long.parseLong(shar + "");//Tiene su limite
            } catch (NumberFormatException ex) {
                isChar = true;
                for (char sharok : caracteres) {
                    if (shar == sharok) {
                        isChar = false;
                        break;
                    } //Ax: cambiar este break no se ha probado
                }
                if (isChar)
                    stAlfanum = stAlfanum.replace("" + shar, "|");//no perder posiciones loop
            }
        }
        stAlfanum = stAlfanum.replace("|", "");
        return stAlfanum;
    }
//    public void log(File logFile, String msg) {
//        try {
//            if (!logFile.exists()) logFile.createNewFile();
//            Date d = new Date();
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));//append true
//            bufferedWriter.write(d.toString() + " | " + msg + "\r\n");
//            bufferedWriter.close();
//
//            if (logFile.length() > 3000000) logFile.delete(); //Ax: si tiene mas de 1 mega lo borra
//
//        } catch (Exception e) {
//            return;
//        }
//    }

    //Ax: descomprime un zip y extrae los archivos a un path mas(folderUnzip), compara tamano y si existe el archivo
    //debe recibir la ruta completa con el zip,una carpeta y el tamaño del zip
    public String DescomprimeZip(String fullRuta, String folderUnzip, int fulltamano, boolean borrar) {

        File file = new File(fullRuta);

        if (!file.exists()) return "Descomprime(Error: no existe arc.)";
        else if (file.length() != fulltamano) return "Descomprime(Error: tamano arc.)";

        String fileName = fullRuta.substring(fullRuta.lastIndexOf('/') + 1, fullRuta.length());
        String ruta = fullRuta.replace(fileName, folderUnzip);

        File newpath = new File(ruta);
        if (!newpath.exists()) {
            if (!newpath.mkdir()) return "Descomprime(Error: creando path.)"; //No se dejo crear
        }

        try {
            String outputPath = ruta;
            ZipFile zipFile = new ZipFile(new File(file.getAbsolutePath()));

            @SuppressWarnings("unchecked")
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();//Ax. obtiene la lista de los archivos del zip

            for (FileHeader fileHeader : fileHeaders) {

                String filename = fileHeader.getFileName();

                if (filename.contains("/")) {
                    filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length()); //Android
                } else {
                    filename = filename.substring(filename.lastIndexOf("\\") + 1, filename.length()); //Windows
                }
                zipFile.extractFile(fileHeader, outputPath, null, filename);//Ax: Para evitar errores:  fileheader,salida,zipparametros,nuevonombre
            }
        } catch (Exception ex) {
            return "Descomprime(Error:" + ex.getMessage() + ")";
        }

        if (borrar) {
            try {    //Ax: borrar zip si está habilitado
                file.delete();
            } catch (Exception ex) {
                return "Descomprime ✓; borrar Zip (error)";
            }
        }
        return "Descomprime ✓; borrar Zip ✓";
    }

    //A: crea zip con la libreria Zip4j
    public String CreaZip4j(String zipPorCrear, ArrayList filesToAdd) {

        try {

            File file = new File(zipPorCrear);
            if (file.exists()) {
                if (!file.delete()) return "Error al borrar zip " + file.getName();
            }

            ZipFile zipFile = new ZipFile(zipPorCrear);

            ZipParameters parameters = new ZipParameters();

            //parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);// metodo compresion: store compression

            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);//nivel compresion: de 0-9

            zipFile.createZipFile(filesToAdd, parameters);

        } catch (ZipException e) {
            return e.getMessage();
        }
        return "ok ✓";
    }

    public String CreaZip(String zipPorCrear, ArrayList filesToAdd) {//String[] _files, String zipFileName) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipPorCrear);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < filesToAdd.size(); i++) {

                FileInputStream fi = new FileInputStream(filesToAdd.get(i).toString());
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(filesToAdd.get(i).toString().substring(filesToAdd.get(i).toString().lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            return e.getMessage();
        }
        return "ok ✓";
    }

    //A: No se usa, Descomprime zip con la libreria Zip4j
    public String DescomprimeZip4j(String zipPorCrear, ArrayList filesToAdd) {

        try {

            File file = new File(zipPorCrear);
            if (file.exists()) {
                if (!file.delete()) return "Error al borrar zip " + file.getName();
            }

            ZipFile zipFile = new ZipFile(zipPorCrear);

            ZipParameters parameters = new ZipParameters();

            //parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);// metodo compresion: store compression

            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);//nivel compresion: de 0-9

            zipFile.createZipFile(filesToAdd, parameters);

        } catch (ZipException e) {
            return e.getMessage();
            //e.printStackTrace();
        }
        return "ok ✓";
    }

    //Ax: mueve los archivos por extension todos= ".*",solo zips =".zip"
    //Recibe carpetaEntrada: ...card01/sd/carpeta1, carpetaSalida: ...card01/sd/carpeta2, ".sda"
    public String MoverArchivosPorTipo(String carpetaEntrada, String carpetaSalida, final String ext) {

        File carpEnt = new File(carpetaEntrada);

        if (!carpEnt.isDirectory()) return "MoverExt(Error: No existe dir in.)";

        File carpSal = new File(carpetaSalida);

        if (!carpSal.isDirectory()) return "MoverExt(Error: No existe dir out.)";

        String[] lista = carpEnt.list(new FilenameFilter() { //Ax. filtro para capturar nombres de archivo por extension
            @Override
            public boolean accept(File dir, String name) {
                return name.toUpperCase().endsWith(ext);
            }
        });

        if (lista.length == 0) return "MoverExt(Error: Nada por mover)";

        for (String file : lista) {
            File filesin = new File(carpEnt, file);
            File filesout = new File(carpSal, file);
            if (!filesin.renameTo(filesout)) {
                return "Mover(Error: al mover" + file + ")";
            }
        }
        return "Mover ✓";
    }

    //Ax copia un archivo a otro sobreescribiendolo si ya existe
    public boolean CrearCopia(String inputFullFile, String outputFullFile) {

        File inn = new File(inputFullFile);
        if (!inn.exists() || inn.length() < 4) {
            return false;
        }

        InputStream in = null;
        OutputStream out = null;
        try {

            File dir = new File(outputFullFile);
            if (!dir.exists()) dir.createNewFile();

            in = new FileInputStream(inputFullFile);
            out = new FileOutputStream(outputFullFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            out.flush();// write the output file
            out.close();
            out = null;

        } catch (FileNotFoundException fe) {
            return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    //Ax copia un archivo a otro SIN sobreescribir si ya existe
    public boolean CrearCopiaSin(String inputFullFile, String outputFullFile) {

        File inn = new File(inputFullFile);
        if (!inn.exists() || inn.length() < 4) {
            return false;
        }

        InputStream in = null;
        OutputStream out = null;
        try {

            File dir = new File(outputFullFile);
            if (!dir.exists()) dir.createNewFile();

            in = new FileInputStream(inputFullFile);
            out = new FileOutputStream(outputFullFile, true);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            out.flush();// write the output file
            out.close();
            out = null;

        } catch (FileNotFoundException fe) {
            return false;
        } catch (Exception ex) {
            return false;
        }
        return true;
    }


    //Ax. Para reducir calidad de imagenes JPG
    public String ReduceImagen(String filePath, String fecha, String latitud, String longitud) {//, int calidad, int oWidth, int oHeight
        String err = "";
        try {
            File _filePath = new File(filePath);
            if (_filePath.isDirectory()) return "no es foto";
            if (_filePath.length() < 100000) return "tamaño < 100k";
            if (!_filePath.getName().toUpperCase().contains(".JPG")) return "No es jpg";

            File file2 = new File(_filePath.getAbsolutePath().replace(".jpg", "temp.jpg"));

            try {
                InputStream in = new FileInputStream(_filePath);
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                bitmap = getResizedBitmap(bitmap, 640); //Ax: Todo: cambiar cuando la camara se coloca horizontal
                OutputStream out = new FileOutputStream(file2);

                Canvas canvas = new Canvas(bitmap); //Se inicia el proceso de escribir en la foto

                Paint paint = new Paint();
                paint.setColor(Color.RED); // Text Color alpha de 98%, rojo
                paint.setStrokeWidth(24); // Text Size
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // text Overlapping Pattern

                canvas.drawBitmap(bitmap, 0, 0, paint);
                canvas.drawText(fecha + "; Lat " + latitud + " Lon " + longitud, 15, 15, paint);

                try {
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)) {
                        {
                            file2.renameTo(_filePath); //Se borra el nuevo imagen y se le coloca el nombre de la original

                        }
                    } else {
                        return "Failed to save the image as a JPEG"; //throw new Exception("Failed to save the image as a JPEG");
                    }

                } catch (Exception ex) {
                    err = "" + ex.getMessage();
                } finally {
                    out.close();
                    in.close();
                    return err;
                }
            } catch (Exception ex) {
                err = "" + ex.getMessage();
                return err;
            }
        } catch (Exception ex) {
            err = "" + ex.getMessage();
            return err;
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio >1) {//ojo antes: if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }

    /**
     * Trata de crear un loG de eventos en datos de entrada, si falla no hace nada
     *
     * @param logFile ruta completa en SDcard +archivo.log
     * @param msg     mensage
     */
    public void Log(File logFile, String msg) {
        try {
            if (!logFile.exists()) logFile.createNewFile();
            Date d = new Date();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));//append true
            bufferedWriter.write(d.toString() + " | " + msg + "\r\n");
            bufferedWriter.close();

            if (logFile.length() > 1000000) logFile.delete(); //Ax: si tiene mas de 1 mega lo borra

        } catch (Exception e) {
            return;
        }
    }

    /**
     * Trata de crear un archivo, sobrescribiendo siempre, si falla no hace nada (ojo crea nueva linea tambien)
     *
     * @param logFile ruta completa en SDcard + archivo.log
     * @param msg     mensage
     */
    public void WriteLine(File logFile, String msg) {

        try {
            if (!logFile.exists()) logFile.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, false));//append
            bufferedWriter.write(msg + "\r\n");
            bufferedWriter.close();

        } catch (Exception e) {
            return;
        }
    }

    /**
     * Trata de crear un archivo, añadiendo texto siempre, si falla no hace nada, no añade nueva linea y no sobreescribe, solo añade
     *
     * @param logFile ruta completa en SDcard + archivo.log
     * @param msg     mensage
     */
    public Boolean EscribirLinea(File logFile, String msg) {

        try {
            if (!logFile.exists()) logFile.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));//append true, añade a lo que ya existe
            bufferedWriter.write(msg);
            bufferedWriter.close();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /***
     * Trata de leer la linea de texto del archivo de parametro. Si no existe el archivo lo crea
     *
     * @param txtFile
     * @return
     */
    public String ReadLine(File txtFile) {

        FileReader r = null;

        try {
            if (!txtFile.exists()) txtFile.createNewFile();

            r = new FileReader(txtFile);
            BufferedReader reader = new BufferedReader(r);
            String linea = "";

            while ((linea = reader.readLine()) != null) {

                if (!linea.trim().equals("")) return linea.trim();
            }
        } catch (IOException e) {
        } finally {
            try {
                if (r != null) r.close();
            } catch (IOException e) {
            }

        }
        return "";
    }

    //Ax: Devuelve double de String
    public double parseStringToDouble(String x) {
        try {
            double y = Double.parseDouble(x.trim());
            return y;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error.Double parseo " + x);
        } catch (Exception e) {
            throw new RuntimeException("Error.Double parseo " + x);
        }
    }

    //Ax: Devuelve entero de String
    public int parseStringToInteger(String x) {
        try {
            if (x.contains(".")) {
                int y = (int) parseStringToDouble(x);
                return y;
            }
            int y = Integer.parseInt(x.trim());
            return y;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error.Integer parseo " + x);
        } catch (Exception e) {
            throw new RuntimeException("Error.Integer parseo " + x);
        }
    }

    public String devolverTxtEntreChars(int posicion, String separador, String cadena) { /// ej: "ABC|DEF|GHI|JKL|"   ABC= posicion 1, DEF = pos 2  Suponiendo que primer caracter no separador

        try {
            if (posicion == 1) {
                return cadena.substring(0, cadena.indexOf(separador, 0));
            }

            if (cadena.substring(0, 1).equals(separador)) {

            }
            int conteo = 0;
            int f = 0;

            for (int i = 0; i < cadena.length(); i++) {
                f = cadena.indexOf(separador, i);
                if (f == -1) {
                    return "";
                }
                conteo++;
                f++;
                i = f;
                if (conteo == (posicion - 1)) {
                    return cadena.substring(f, cadena.indexOf(separador, f));
                }
            }
        } catch (Exception ex) {
            return "";
        }
        return "";
    }

    /**
     * genera un HASH MD5  de un archivo y devuelve la cadena en mayusculas
     *
     * @param path
     * @return
     */
    public String Md5Hash(String path) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(path);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString().toUpperCase();
        } catch (Exception ex) {
            return "";
        }
    }


    public String ReduceImagen2(String filePath, String mensajeFoto) {//rx

        int qualitys = 60;
        InputStream in = null;
        OutputStream out = null;
        File fotoTomada = new File(filePath);
        File fotoTemp = new File(filePath.replace(".jpg", "temp.jpg"));
        Canvas canvas = null;
        Bitmap bitmap = null;
        try {
            //Random random = new Random();  ||  random.nextBoolean()

            /*if (fotoTomada.length() < 400000) {
                return "Tamaño < 400kb, de: " + fotoTomada.length(); //foto negra 500k //rx
            }*/

            if (fotoTomada.length() < 2000000) {
                qualitys = 68;
            }

            in = new FileInputStream(fotoTomada);
            bitmap = BitmapFactory.decodeStream(in);
//
//            Matrix rotateMatrix = new Matrix();  //Nuevo, Las fotos salen horizontales hay que voltearlas (ver camara nueva)setRutayNombreFoto
//            rotateMatrix.postRotate(90);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);  //fin Nuevo,

            bitmap = getResizedBitmap3(bitmap, 640); //Ax: Todo: cambiar cuando la camara se coloca horizontal
            out = new FileOutputStream(fotoTemp);

            Paint paint = new Paint();
            paint.setColor(Color.RED); // Text Color alpha de 98%, rojo
            paint.setStrokeWidth(2); // Text Size
            paint.setFakeBoldText(true);
            paint.setTextSize(16); // Text Size
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // text Overlapping Pattern

            for (int i = 0; i < 15; i++) {

                canvas = new Canvas(bitmap); //Se inicia el proceso de escribir en la foto
                canvas.drawBitmap(bitmap, 0, 0, paint);
                canvas.drawText(mensajeFoto, 15, 17, paint);

                fotoTemp.createNewFile();
                out = new FileOutputStream(fotoTemp);

                if (bitmap.compress(Bitmap.CompressFormat.JPEG, qualitys, out)) {//60 ---ojo esta calidad depende de los mpx de la camara

                    long tam1 = fotoTemp.length();

                    if (tam1 < 13000) {
                        qualitys = qualitys + 7;
                        fotoTemp.delete();
                        continue;
                    }

                    if (tam1 > 100000) {
                        qualitys = qualitys - 30;
                        fotoTemp.delete();
                        continue;
                    }

                    if (tam1 > 70000) {
                        qualitys = qualitys - 15;
                        fotoTemp.delete();
                        continue;
                    }

                    if (tam1 > 50000) {
                        qualitys = qualitys - 9;
                        fotoTemp.delete();
                        continue;
                    }

                    if (tam1 > 40000) {
                        qualitys = qualitys - 6;
                        fotoTemp.delete();
                        continue;
                    }

                    fotoTemp.renameTo(fotoTomada); //Se borra el nuevo imagen y se le coloca el nombre de la original

                    Log.e("INFO", "fotoTomada: " + fotoTomada.length());

                    if (fotoTomada.length() < 10000) {
                        return "Tamaño < 10kb, de: " + fotoTomada.length();//rx
                    }

                    return "";

                } else {
                    return "Fallo al salvar como JPEG";
                }
            }

            fotoTemp.renameTo(fotoTomada);

            if (fotoTomada.length() > 13000 && fotoTomada.length() < 40000)
                return "";
            else {
                return "error Foto final:" + fotoTomada.length();
            }
        } catch (Exception ex) {
            return "Foto. " + ex.getMessage();
        } finally {
            try {
                if (out != null) { //rx
                    out.close();
                }
                if (in != null) {//rx
                    in.close();
                }

                File fotoTemp_pslm = new File(filePath.replace(".jpg", "temp.jpg"));
                if (fotoTemp_pslm.exists()) fotoTemp_pslm.delete();

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (canvas != null) {
                    canvas = null;
                }

            } catch (Exception ex) {
                Log.e("ERROR", "ReduceImagen2: " + ex.getMessage());
            }
        }
    }

    public Bitmap getResizedBitmap3(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {//ojo antes: if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private static final int MAX_QUALITY_SEARCH_ITERATIONS = 8;
    private static final int MIN_JPEG_QUALITY = 1;
    private static final int MAX_JPEG_QUALITY = 100;

    /**
     * Redimensiona una imagen, le superpone un texto y comprime iterativamente
     * (búsqueda binaria de calidad JPEG) hasta caer dentro de [minTargetBytes, maxTargetBytes].
     *
     * @param filePath        Ruta del JPEG origen (se sobreescribe con el resultado).
     * @param mensajeFoto     Texto a dibujar sobre la imagen.
     * @param maxAncho        Ancho máximo (px) para el resize.
     * @param minTargetBytes  Tamaño mínimo aceptado (bytes).
     * @param maxTargetBytes  Tamaño máximo aceptado (bytes).
     * @return "" si quedó dentro de rango, o un mensaje describiendo el resultado/error.
     */
    public String reduceImagen(String filePath, String mensajeFoto, int maxAncho,
                               long minTargetBytes, long maxTargetBytes) {

        File fotoOriginal = new File(filePath);
        Bitmap bitmapOriginal = null;
        Bitmap bitmapFinal = null;

        try {
            try (InputStream in = new FileInputStream(fotoOriginal)) {
                bitmapOriginal = BitmapFactory.decodeStream(in);
            }
            if (bitmapOriginal == null) {
                return "No se pudo decodificar la imagen: " + filePath;
            }

            bitmapFinal = getResizedBitmap2(bitmapOriginal, maxAncho);
            if (bitmapFinal != bitmapOriginal) {
                bitmapOriginal.recycle(); // ya no se necesita el bitmap sin redimensionar
                bitmapOriginal = null;
            }

            // El watermark se dibuja UNA sola vez, no en cada intento de compresión
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(2);
            paint.setFakeBoldText(true);
            paint.setTextSize(16);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            new Canvas(bitmapFinal).drawText(mensajeFoto, 15, 17, paint);

            byte[] resultado = comprimirDentroDeRango(bitmapFinal, minTargetBytes, maxTargetBytes);

            File fotoTemp = new File(filePath.replace(".jpg", ".tmp"));
            try (OutputStream out = new FileOutputStream(fotoTemp)) {
                out.write(resultado);
            }

            if (!fotoTemp.renameTo(fotoOriginal)) {
                fotoTemp.delete();
                return "No se pudo reemplazar el archivo original";
            }

            long tamFinal = fotoOriginal.length();
            if (tamFinal < minTargetBytes || tamFinal > maxTargetBytes) {
                return "Fuera de rango, tamaño final: " + tamFinal; // mejor logrado, pero informa
            }
            return "";

        } catch (Exception ex) {
            return "Foto. " + ex.getMessage();
        } finally {
            if (bitmapOriginal != null) bitmapOriginal.recycle();
            if (bitmapFinal != null) bitmapFinal.recycle();
        }
    }

    /**
     * Búsqueda binaria de calidad JPEG. Compresión en memoria, sin tocar disco
     * hasta encontrar el mejor resultado. Complejidad O(log 100) ≈ 7 iteraciones.
     */
    private byte[] comprimirDentroDeRango(Bitmap bitmap, long minBytes, long maxBytes) {
        int low = MIN_JPEG_QUALITY;
        int high = MAX_JPEG_QUALITY;
        byte[] mejorResultado = null;
        long mejorDiferencia = Long.MAX_VALUE;

        for (int i = 0; i < MAX_QUALITY_SEARCH_ITERATIONS && low <= high; i++) {
            int calidadMedia = (low + high) / 2;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, calidadMedia, baos);
            byte[] candidato = baos.toByteArray();
            long size = candidato.length;

            long diferencia = size < minBytes ? (minBytes - size)
                    : size > maxBytes ? (size - maxBytes) : 0;

            if (diferencia < mejorDiferencia) {
                mejorDiferencia = diferencia;
                mejorResultado = candidato;
            }

            if (size >= minBytes && size <= maxBytes) {
                break; // dentro del rango, listo
            } else if (size > maxBytes) {
                high = calidadMedia - 1; // muy pesado, bajar calidad
            } else {
                low = calidadMedia + 1;  // muy liviano, subir calidad
            }
        }
        return mejorResultado;
    }
    /**
     * Redimensiona proporcionalmente solo si excede maxSize. Nunca hace upscaling.
     * @return la misma instancia de 'image' si ya cumple el tamaño (evita copia innecesaria),
     *         o un nuevo Bitmap escalado en caso contrario.
     */
    public Bitmap getResizedBitmap2(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Bitmap con dimensiones inválidas: " + width + "x" + height);
        }

        // Ya cumple el límite: no escalar (evita upscaling y una copia innecesaria)
        if (width <= maxSize && height <= maxSize) {
            return image;
        }

        float bitmapRatio = (float) width / (float) height;
        int newWidth, newHeight;

        if (bitmapRatio > 1) {
            newWidth = maxSize;
            newHeight = Math.round(newWidth / bitmapRatio);
        } else {
            newHeight = maxSize;
            newWidth = Math.round(newHeight * bitmapRatio);
        }

        // Salvaguarda: el redondeo con ratios extremos puede colapsar a 0
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    //Ax. Para reducir calidad de imagenes JPG
//    public String ReduceImagen2a(String filePath, String fecha, String latitud, String longitud, File log) {//, int calidad, int oWidth, int oHeight
//        String err = "";
//        try {
//            File _filePath = new File(filePath);
//            if (_filePath.isDirectory()) return "no es foto";
//            if (_filePath.length() < 10000) return "tamaño < 10k";
//            if (!_filePath.getName().toUpperCase().contains(".JPG")) return "No es jpg";
//
//            File file2 = new File(_filePath.getAbsolutePath().replace(".jpg", "temp.jpg"));
//            int qualitys = 60;
//            InputStream in = null;
//            OutputStream out = null;
//
//            try {
//
//                in = new FileInputStream(_filePath);
//                Bitmap bitmap = BitmapFactory.decodeStream(in);
//                bitmap = getResizedBitmap(bitmap, 640); //Ax: Todo: cambiar cuando la camara se coloca horizontal
//                out = new FileOutputStream(file2);
//
//                long tam1 = _filePath.length();
//
//                if (tam1 > 13000 && tam1 < 40000)return "";
//
//                for (int i = 0; i < 6; i++) {
//
//                    Canvas canvas = new Canvas(bitmap); //Se inicia el proceso de escribir en la foto
//                    Paint paint = new Paint();
//                    paint.setColor(Color.RED); // Text Color alpha de 98%, rojo
//                    paint.setStrokeWidth(2); // Text Size
//                    paint.setFakeBoldText(true);
//                    paint.setTextSize(16); // Text Size
//                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // text Overlapping Pattern
//                    canvas.drawBitmap(bitmap, 0, 0, paint);
//                    canvas.drawText(fecha + " " + latitud + " " + longitud, 15, 17, paint);
//
//                    file2.createNewFile();
//                    out = new FileOutputStream(file2);
//
//                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, qualitys, out)) {//60 ---ojo esta calidad d.epende de los mpx de la camara
//                        {
//                            tam1 = file2.length();
//
//                            // Log(log, "tamaño: " + tam1 + " + " + qualitys + " + " + _filePath.getAbsolutePath());
//
//                            if (tam1 < 13000) {//10k
//                                qualitys = qualitys + 7;
//                                file2.delete();
//                                continue;
//                            }
//
//                            if (tam1 > 100000) {
//                                qualitys = qualitys - 30;
//                                file2.delete();
//                                continue;
//                            }
//
//                            if (tam1 > 70000) {
//                                qualitys = qualitys - 15;
//                                file2.delete();
//                                continue;
//                            }
//
//                            if (tam1 > 50000) {
//                                qualitys = qualitys - 9;
//                                file2.delete();
//                                continue;
//                            }
//
//                            if (tam1 > 40000) {
//                                qualitys = qualitys - 6;
//                                file2.delete();
//                                continue;
//                            }
//
//                            file2.renameTo(_filePath); //Se borra el nuevo imagen y se le coloca el nombre de la original
//                            return "";
//                        }
//                    } else {
//                        return "Failed to save the image as a JPEG";
//                    }
//                }
//                if (_filePath.length() > 13000 && _filePath.length() < 40000)return "";
//                else
//                    return "Debe tomar foto de nuevo *";
//            } catch (Exception ex) {
//                err = "g. " + ex.getMessage();
//            } finally {
//                out.close();
//                in.close();
//                return err;
//            }
//
//        } catch (Exception ex) {
//            err = "j. " + ex.getMessage();
//            return err;
//        }
//    }

    public void MensajeTime(String msg, String titulo, Context context, int tiempo) { //Ax: crea un mensaje que se puede cerrar o dura 5 segundos

        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            builder.setTitle(titulo);
            builder.setMessage(msg);
            builder.setCancelable(true);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            final androidx.appcompat.app.AlertDialog dlg = builder.create();

            dlg.show();

            if (tiempo < 1) tiempo = 5000;
            else tiempo = tiempo * 1000;

            final Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                public void run() {
                    dlg.dismiss();
                    timer.cancel();
                }
            }, tiempo);

        } catch (Exception ex) {
            //Todo: Implementar algo
        }
    }

}