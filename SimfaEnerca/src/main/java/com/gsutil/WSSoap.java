package com.gsutil;

import android.util.Base64;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WSSoap implements Runnable { //Ax: usa las librerias KSOAP2

    public static final String WS_NAMESPACE = "http://tempuri.org/";
    public static String WS_URL;
    public static String WS_METHOD_NAME;// = "VALIDAR_CONEXION";
    public static String WS_PAGE = "";
    public static String WS_URL_PAGE = "";
    public FileOutputStream fileOutpStrm;
    public FileInputStream fileInpStrm;
    public String result;

    public WSSoap(String url, String paginaws) {
        WS_PAGE = paginaws;
        WS_URL = url;
        WS_URL_PAGE = url + "/" + paginaws;
    }

    //Ax: Se conecta al webservice y el metodo VALIDAR_CONEXION devuelve "1" si esta arriba, si no string con error
    public String verificarWs(String metodo) {

        WS_METHOD_NAME = metodo;

        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;
            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service
            SoapObject obj1 = (SoapObject) envelope.getResponse();
            result = obj1.getProperty(0).toString();

        } catch (IOException | XmlPullParserException e) {
            Log.d("error","service probar "+ e.getMessage());
            Log.d("logAx", e.getMessage());
            result = e.getMessage();
        }
        return result;
    }

    //Ax: Se conecta al webservice y el metodo RutasProgramadas devuelve...
    public String rutasProgramadas(String metodo, String serial, String ruta) {

        WS_METHOD_NAME = metodo;

        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        //Ax: Añadir parametros
        PropertyInfo p = new PropertyInfo();
        p.setName("dirCiclo");
        p.setValue(ruta);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("serial");
        p2.setValue(serial);
        p2.setType(String.class);
        request.addProperty(p2);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;
            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            result = response.toString();


        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    //Ax: Hace peticion al WS de un archivo localizado en WS y devuelve su tamaño
    public String ObtenerLongitudArchivo(String metodo, String archivoPorRecibir, int escomprimido, String ciclo, String ruta) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("rutaArchivo");
        p.setValue(archivoPorRecibir);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("Comprimido");
        p2.setValue(escomprimido);
        p2.setType(Long.class);
        request.addProperty(p2);

        PropertyInfo p3 = new PropertyInfo();
        p3.setName("Ciclo");
        p3.setValue(ciclo);
        p3.setType(Integer.class);
        request.addProperty(p3);

        PropertyInfo p4 = new PropertyInfo();
        p4.setName("Ruta");
        p4.setValue(ruta);
        p4.setType(Integer.class);
        request.addProperty(p4);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            result = response.toString();

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
            Log.e("error","longitud1 "+e.getMessage());
        } catch (Exception e) {
            Log.e("error","longitud2 "+e.getMessage());
            result = e.getMessage();
        }
        return result;
    }

    //Ax: Hace peticion al WS de una ruta y un archivo, si existe archivo lo borra y crea ruta en el server, devuelve booleano
    public String VerificarSiexisteDirectorioArchivo(String metodo, String directorio, String archivo) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("directorio");
        p.setValue(directorio);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("archivo");
        p2.setValue(archivo);
        p2.setType(Long.class);
        request.addProperty(p2);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            result = response.toString();

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    //Ax: Hace peticion al WS del archivo que se va a bajar o se bajo. REGISTRAR_PROGRAMACION_ENRUTADOR
    public String RegistrarProgramEnrutadosr(String metodo, String dirciclo, String condicion, String tamano, String serial, String rutarchivo, String nombrearchivo,int RegistrosLeidos,int CausasLeidas) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("Dirciclo");
        p.setValue(dirciclo);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("condicion");
        p2.setValue(condicion);
        p2.setType(String.class);
        request.addProperty(p2);

        PropertyInfo p3 = new PropertyInfo();
        p3.setName("tamano");
        p3.setValue(tamano);
        p3.setType(String.class);
        request.addProperty(p3);

        PropertyInfo p4 = new PropertyInfo();
        p4.setName("serial");
        p4.setValue(serial);
        p4.setType(String.class);
        request.addProperty(p4);

        PropertyInfo p5 = new PropertyInfo();
        p5.setName("ruta_archivo");
        p5.setValue(rutarchivo);
        p5.setType(String.class);
        request.addProperty(p5);

        PropertyInfo p6 = new PropertyInfo();
        p6.setName("nombre_archivo");
        p6.setValue(nombrearchivo);
        p6.setType(String.class);
        request.addProperty(p6);

        PropertyInfo p7 = new PropertyInfo();
        p7.setName("RegistrosLeidos");
        p7.setValue(RegistrosLeidos);
        p7.setType(Integer.class);
        request.addProperty(p7);

        PropertyInfo p8 = new PropertyInfo();
        p8.setName("CausasLeidas");
        p8.setValue(CausasLeidas);
        p8.setType(Integer.class);
        request.addProperty(p8);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            result = response.toString();

            try {
                int x = Integer.parseInt(result);
                if (x != 1) result = MensajeRespuestaWeb(x, condicion);

            } catch (Exception e) {
            }

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    //Ax: Hace peticion al WS y Trae el archivo por partes y numero de parte.
    public String ObtenerArchivo(String metodo, String archivoPorRecibir, String rutaAGrabar, int trama, int tamano) throws Exception {

        WS_METHOD_NAME = metodo;
        int i;
        long veces = 1, trozos = 0;
        File file;
        byte[] x = null;
        fileOutpStrm = null;
        int tamPaquetes = (trama * 1024);

        try {

            file = new File(rutaAGrabar);
            if (file.exists()) file.delete();

            fileOutpStrm = new FileOutputStream(rutaAGrabar, true);//True: se añade datos al final y no sobre escribe

            if (tamano > tamPaquetes) {

                if ((tamano % tamPaquetes) != 0) {
                    veces = (tamano / tamPaquetes) + 1;
                } else {
                    veces = (tamano / tamPaquetes);
                }
            }

            for (i = 0; i < veces; i++) {

                if (i == 0) {
                    trozos = 0;
                } else {
                    trozos = (tamPaquetes * i) + 1;
                }

                if (i == (veces - 1)) { //Ax: calcula el ultimo paquete
                    int y = (int) trozos;
                    tamPaquetes = tamano - y + 1;
                }

                SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

                PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
                p.setName("rutaArchivo");
                p.setValue(archivoPorRecibir);
                p.setType(String.class);
                request.addProperty(p);

                PropertyInfo p2 = new PropertyInfo();
                p2.setName("trozo");
                p2.setValue(trozos);
                p2.setType(Long.class);
                request.addProperty(p2);

                PropertyInfo p3 = new PropertyInfo();
                p3.setName("paquete");
                p3.setValue(tamPaquetes);
                p3.setType(Integer.class);
                request.addProperty(p3);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

                envelope.dotNet = true;

                envelope.setOutputSoapObject(request);

                HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

                try {
                    httpTransport.debug = true;
                    httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

                    x = null;
                    x = Base64.decode(response.toString().getBytes("ISO-8859-1"), Base64.DEFAULT);//Ax: convierte la cadena en Base64 codificacion ISO

                    fileOutpStrm.write(x, 0, x.length);//ok

                } catch (Exception e) {
                    result = "Error: " + e.getMessage();
                    Log.e("error", "1");
                    fileOutpStrm.close();
                }
            }
            if (fileOutpStrm != null) {
                fileOutpStrm.flush();
                fileOutpStrm.close();//getFD().sync();

                result = "1";
//                if (EliminarArchivoWs(archivoPorRecibir, "BorrarArchivoDelServidor")) {
//                    result = "2";
//                }
            }

        } catch (FileNotFoundException e) {
            Log.e("error", "2");
            result = "Error: " + e.getMessage();
        } catch (IOException e) {
            Log.e("error", "3");
            result = "Error: " + e.getMessage();
        }
        return result;
    }

//    //Ax: sube archivo por partes y numero de parte.
//    //rutaAGrabar : ruta en el servidor sin el archivo
//    //rutaArchivofull: ruta del archivo por subir  .../sdcard0/archivosOut/algo.zip
//    public String TerminalToServerReceive(String metodo, String rutaAGrabar, String rutaArchivofull, int trama, int comprimido) throws Exception {
//
//        WS_METHOD_NAME = metodo;
//
//        int veces = 1, conteo = 1;
//        File file;
//        fileInpStrm = null;
//        int tamPaquetes = (trama * 1024);
//        ByteArrayOutputStream byteArrOutStrem = null;
//
//        try {
//            file = new File(rutaArchivofull);
//
//            if (file.length() > tamPaquetes) { //Ax: si el archivo es muy grande se divide en paquetes (varios envios)
//
//                if ((file.length() % tamPaquetes) != 0) {
//                    veces = ((int) file.length() / tamPaquetes) + 1; //Ax: la division no es exacta se le coloca una ultima vez (el ultimo paquete)
//                } else {
//                    veces = ((int) file.length() / tamPaquetes);
//                }
//            } else {
//                if (file.length() < 1024) {
//                    tamPaquetes = (int) file.length();
//                } else {
//
//                    tamPaquetes = 1024;//Ax: el archivo es muy pequeño (1 envio), se escoge el buffer estandard
//                }
//            }
//
//            String nombre = rutaAGrabar + "\\" + file.getName(); //Ax: ruta en el servidor donde dejar el zip
//            nombre = nombre.replace("\\\\", "\\");
//
//            fileInpStrm = new FileInputStream(file);
//            byteArrOutStrem = new ByteArrayOutputStream(tamPaquetes);
//            byte[] buffer = new byte[tamPaquetes];
//            int posFin;
//
//            while ((posFin = fileInpStrm.read(buffer)) >= 0) {
//
//                byte[] y;
//
//                if (veces > 1 && veces == conteo) { //es el ultimo paquete el buffer debe cambiar
//                    int pos = tamPaquetes * (veces - 1); //Ax. la posicion del paquete anterior  y es la posicion inicial del ultimo paquete
//                    tamPaquetes = (int) file.length() - pos;//Ax: saco el tamaño del ultimo paquete   (Ojo  Antes:  tamPaquetes = (int) file.length() - pos -1)
//
//                    FileInputStream fileInStr2 = new FileInputStream(file);//Ax: hay que volver a leer el archivo desde una posicion especifica
//                    ByteArrayOutputStream byteArrOutStrem2 = new ByteArrayOutputStream(tamPaquetes);
//
//                    fileInStr2.getChannel().position(pos);
//
//                    byte[] buffer2 = new byte[tamPaquetes];
//                    fileInStr2.read(buffer2);
//
//                    byteArrOutStrem2.write(buffer2, 0, tamPaquetes);
//                    y = new byte[tamPaquetes];
//                    y = buffer2;
//
//                    fileInStr2.close();
//                    byteArrOutStrem2.close();
//
//                } else { //Ax: aqui escribe los primeros bites para enviar
//
//                    y = new byte[tamPaquetes];
//                    byteArrOutStrem.write(buffer, 0, posFin);
//                    y = buffer;
//                }
//
//                SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);
//
//                PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
//                p.setName("rutaArchivo");
//                p.setValue(nombre);
//                p.setType(String.class);
//                request.addProperty(p);
//
//                PropertyInfo p2 = new PropertyInfo();
//                p2.setName("tamanoArchivo");
//                p2.setValue(file.length());
//                p2.setType(Double.class);
//                request.addProperty(p2);
//
//                PropertyInfo p3 = new PropertyInfo();
//                p3.setName("cadenaBytes");
//                p3.setValue(y);
//                p3.setType(MarshalBase64.class);//.BYTE_ARRAY_CLASS);// Byte.class
//                request.addProperty(p3);
//
//                PropertyInfo p4 = new PropertyInfo();
//                p4.setName("Comprimido");
//                p4.setValue(comprimido);
//                p4.setType(Integer.class);
//                request.addProperty(p4);
//
//                PropertyInfo p5 = new PropertyInfo();
//                p5.setName("DirectorioDondeDescomprimir");
//                p5.setValue("");
//                p5.setType(String.class);
//                request.addProperty(p5);
//
//                Log.e("error enviar", "entra 5 " + nombre + " " + file.length() + "  " + y + " " + comprimido);
//
//                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//                new MarshalBase64().register(envelope);   //serialization
//                envelope.encodingStyle = SoapEnvelope.ENC;
//
//                envelope.dotNet = true;
//
//                envelope.setOutputSoapObject(request);
//
//                HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);
//
//                try {
//                    httpTransport.debug = true;
//                    httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service
//
//                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//                    result = response.toString(); //Ax: 1 = parte ok, 4=fin ok
//
//                    if (!result.equals("1") && !result.equals("4")) {
//                        break;
//                    }
//                    conteo++;
//
//                } catch (Exception e) {
//                    result = "Error: " + e.getMessage();
//                }
//            }
//        } catch (FileNotFoundException e) {
//            result = "Error: " + e.getMessage();
//        } catch (IOException e) {
//            result = "Error: " + e.getMessage();
//        } finally {
//            fileInpStrm.close();
//            byteArrOutStrem.close();
//        }
//        return result;//4 total ok, 1 = parte ok(algo estuvo mal si llega aqui) el resto de numeros = error
//    }

    //Ax: sube archivo por partes y numero de parte.
    //rutaAGrabar : ruta en el servidor sin el archivo
    //rutaArchivofull: ruta del archivo por subir  .../sdcard0/archivosOut/algo.zip
    public String TerminalToServerReceive(String metodo, String rutaAGrabar, int comprimido, String rutaArchivofull, int trama) throws Exception {

        WS_METHOD_NAME = metodo;

        int posFin;
        File file;
        fileInpStrm = null;
        int Paquetes = (trama * 1024);
        int PaquetesMin = (8 * 1024);
        file = new File(rutaArchivofull);
        long filezise = file.length();
        byte[] buffer;
        boolean esMultiplo = false;

        if (filezise < 1) return "Archivo en 0";
        //----------------------------------------------se busca un multiplo entre 8k y 50k, para usarlo de tamaño en buffer
        // Ejemplo:  6/2= 3 => 2...2...2 = (byte's de 2, se usa 3 veces)  diferente de 7/2 =3.5 => 3...3...1 (toca calcular un nuevo byte para 1 )

        if (filezise < Paquetes) {
            esMultiplo = true;
            Paquetes = (int) filezise;
            buffer = new byte[Paquetes];
        } else {
            int p = Paquetes;

            while (true) {

                if (filezise % p == 0) break;
                p--;

                if (p < PaquetesMin) {
                    p = 0; // Se disminuyó hasta que llego a lo minimo que se desea usar
                    break;
                }
            }
            if (p > 1) { // p puede ser un multipo que dé un numero exacto de paquetes sin sobrar bytes

                Paquetes = p;
                buffer = new byte[Paquetes];
                esMultiplo = true;
            } else { //Se usa un numero de buffer opcional y se debe controlar la ultima, leida de bytes
                buffer = new byte[Paquetes];
            }
        }
        //----------------------------------------------
        ByteArrayOutputStream byteArrOutStrem = null;
        String res="-1";

        try {
            fileInpStrm = new FileInputStream(file);
            byteArrOutStrem = new ByteArrayOutputStream(Paquetes);

            if (esMultiplo) { //Aca se envia uno o varios pedazos, pero todos del mismo tamaño
                while ((posFin = fileInpStrm.read(buffer)) >= 0) {

                    byteArrOutStrem.write(buffer, 0, posFin);
                    res = TerminalToServerReceiveII(metodo, rutaAGrabar, comprimido, rutaArchivofull, buffer);

                    if (!res.equals("1")) break;
                }
            } else { //Ax: aca es donde se usa el "Paquetes" original y el último pedazo por enviar se recalcula
                long size = filezise;

                while ((posFin = fileInpStrm.read(buffer)) >= 0) {

                    if (size < buffer.length) {

                        long pos= Math.abs(filezise - size) ;

                        FileInputStream fis2 = new FileInputStream(file);
                        ByteArrayOutputStream baos2 = new ByteArrayOutputStream((int)size);
                        byte[] bf2 = new byte[(int)size];
                        fis2.skip(pos);//fileInpStrm2.getChannel().position(pos); //Ax: skip salta a una posicion de bytes

                        posFin = fis2.read(bf2);
                        baos2.write(bf2, 0, posFin);
                        res = TerminalToServerReceiveII(metodo, rutaAGrabar, comprimido, rutaArchivofull, bf2); //Ax: como es el último pedazo se sale del loop
                        fis2.close();
                        baos2.close();
                        break;
                    } else {
                        byteArrOutStrem.write(buffer, 0, posFin);
                        res = TerminalToServerReceiveII(metodo, rutaAGrabar, comprimido, rutaArchivofull, buffer);
                    }
                    size  = Math.abs(size - Paquetes);

                    if (!res.equals("1")) break;
                }
            }

        } catch (FileNotFoundException e) {
            result = "Error: " + e.getMessage();
        } catch (IOException e) {
            result = "Error: " + e.getMessage();
        } finally {
            fileInpStrm.close();
            byteArrOutStrem.close();
        }
        return result;//4 total ok, 1 = parte ok(algo estuvo mal si llega aqui) el resto de numeros = error
    }

    //Ax: sube archivo por partes (nuevo).
    //rutaRemota : ruta en el servidor sin el archivo
    //rutaArchivofull: ruta del archivo por subir  .../sdcard0/archivosOut/algo.zip
    public String TerminalToServerReceiveII(String metodo, String rutaRemota, int comprimido, String rutaLocalArchivo, byte[] bite) throws Exception {

        WS_METHOD_NAME = metodo;
        File file = new File(rutaLocalArchivo);
        try {
            String nombre = rutaRemota + "\\" + file.getName(); //Ax: ruta en el servidor donde dejar el zip
            nombre = nombre.replace("\\\\", "\\");

            SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

            PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
            p.setName("rutaArchivo");
            p.setValue(nombre);
            p.setType(String.class);
            request.addProperty(p);

            PropertyInfo p2 = new PropertyInfo();
            p2.setName("tamanoArchivo");
            p2.setValue(file.length());
            p2.setType(Double.class);
            request.addProperty(p2);

            PropertyInfo p3 = new PropertyInfo();
            p3.setName("cadenaBytes");
            p3.setValue(bite);
            p3.setType(MarshalBase64.class);//.BYTE_ARRAY_CLASS);// Byte.class
            request.addProperty(p3);

            PropertyInfo p4 = new PropertyInfo();
            p4.setName("Comprimido");
            p4.setValue(comprimido);
            p4.setType(Integer.class);
            request.addProperty(p4);

            PropertyInfo p5 = new PropertyInfo();
            p5.setName("DirectorioDondeDescomprimir");
            p5.setValue(nombre);
            p5.setType(String.class);
            request.addProperty(p5);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            new MarshalBase64().register(envelope);   //serialization
            envelope.encodingStyle = SoapEnvelope.ENC;

            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

            try {
                httpTransport.debug = true;
                httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

                SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                result = response.toString(); //Ax: 1 = parte ok, 4=fin ok

            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }
        } catch (Exception ex) {

        }
        return result;//4 total ok, 1 = parte ok(algo estuvo mal si llega aqui) el resto de numeros = error
    }

    //Ax: se el envia la ruta y descomprime en server.
    public String Descomprimir(String metodo, String pathDescomprimir, String zipFile, String hash, String indRuta) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("pathDescomprimir");
        p.setValue(pathDescomprimir);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("zipFile");
        p2.setValue(zipFile);
        p2.setType(String.class);
        request.addProperty(p2);

        PropertyInfo p3 = new PropertyInfo();
        p3.setName("hash");
        p3.setValue(hash);
        p3.setType(String.class);
        request.addProperty(p3);

        PropertyInfo p4 = new PropertyInfo();
        p4.setName("indRuta");
        p4.setValue(indRuta);
        p4.setType(String.class);
        request.addProperty(p4);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            result = RespuestasWs(response.toString());

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    //Ax: se el envia la ruta y descomprime en server.
    //rutaDescomServer : ruta en el servidor sin el archivo donde se descomprimirá
    //rutaArchivofull: ruta en el servidor con el archivo.zip
    public String DirectorioDescomprimir(String metodo, String rutaDescomServer, String rutaArchivofull, int trama) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("DirectorioDondeDescomprimir");
        p.setValue(rutaDescomServer);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("ArchivoADescomprimir");
        p2.setValue(rutaArchivofull);
        p2.setType(String.class);
        request.addProperty(p2);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            result = response.toString();

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * Sube los 2 archivos descomprimidos en el servidor
     *
     * @param metodo                InsertarDatosTemporales
     * @param rutaServer            C:\EnRutadorSIMFA
     * @param rutaCicloDescomServer CIC0241\DescargasEnvioGPRS
     * @param Serial                355236030406210
     * @param num                   "" vacío sin implementar
     * @return
     * @throws Exception
     */
    public String InsertarDatosTablasTemporales(String metodo, String rutaServer, String rutaCicloDescomServer, String Serial, String num) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("directorioActual");
        p.setValue(rutaServer);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("ciclo");
        p2.setValue(rutaCicloDescomServer);
        p2.setType(String.class);
        request.addProperty(p2);

        PropertyInfo p3 = new PropertyInfo();
        p3.setName("terminal");
        p3.setValue(Serial);
        p3.setType(String.class);
        request.addProperty(p3);

        PropertyInfo p4 = new PropertyInfo();
        p4.setName("estado1");
        p4.setValue(num);
        p4.setType(String.class);
        request.addProperty(p4);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            result = response.toString();

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    //Ax: Hace peticion al WS de borrar un archivo localizado en WS
    public Boolean EliminarArchivoWs(String archivo, String metodo) throws Exception {//BorrarArchivoDelServidor

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("archivo");
        p.setValue(archivo);
        p.setType(String.class);
        request.addProperty(p);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            result = response.toString();

            if (result.equals("true")) {
                return true;
            }

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return false;
    }

    public String chatRecibirMensajes(String metodo, String receptor) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("receptor1");
        p.setValue(receptor);
        p.setType(String.class);
        request.addProperty(p);


        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            //   SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            SoapObject response = (SoapObject) envelope.getResponse();
            result = response.toString();

            //result = response.getProperty(0).toString();


        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();

        } catch (Exception e) {
            result = e.getMessage();

        }
        return result;
    }

    /**
     * kim metodo que inserta los mensajes
     *
     * @param metodo    nombre del metodo que consuluta en el webservice
     * @param remitente codigo de quien lo envia
     * @param receptor  codigo de quien recibe
     * @param mensaje   cadena con el mensaje y asunto
     * @param estado    estado del mensaje
     * @return
     * @throws Exception
     */
    public String chatInsertarMensajes(String metodo, String remitente, String receptor, String mensaje, String estado) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";

        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo();
        p.setName("remitente1");
        p.setValue(remitente);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("receptor1");
        p2.setValue(receptor);
        p2.setType(String.class);
        request.addProperty(p2);

        PropertyInfo p3 = new PropertyInfo();
        p3.setName("mensaje1");
        p3.setValue(mensaje);
        p3.setType(String.class);
        request.addProperty(p3);

        PropertyInfo p4 = new PropertyInfo();
        p4.setName("estado1");
        p4.setValue(estado);
        p4.setType(String.class);
        request.addProperty(p4);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);

            Object response = (Object) envelope.getResponse();
            result = response.toString();

        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }


    //funcion para enviar la respuesta de la coneccion con el pc
    private String MensajeRespuestaWeb(int RespuestaWeb, String condicion) {
        String Respuesta = "";
        switch (RespuestaWeb) {
            case -20:
                Respuesta = "Problemas en el Metodo Programacion EnRutamiento en el SW. " + RespuestaWeb;
                break;
            case -2:
                Respuesta = "No Puede abrir el Archivo de Programacion Enrutamiento. " + RespuestaWeb;
                break;
            case 3:
                Respuesta = "No Existe Archivo de ruta para el lector. " + RespuestaWeb;
                break;
            case 0:
                Respuesta = "No Existe Archivo Programado. " + RespuestaWeb;
                break;
            case -1:
                Respuesta = "No hay Programacion en el Sistema. " + RespuestaWeb;
                break;
            case 1:
                if (condicion == "cargaf" || condicion == "descargaf")
                    Respuesta = "registro de programacion ✓ " + RespuestaWeb;
                break;
            case 4:
                Respuesta = "Esta Ruta ya tiene Descarga no se puede procesar carga alguna... " + RespuestaWeb;
                break;
            case 5:
                Respuesta = "Serial dispositivo no esta Programada... " + RespuestaWeb;
                break;
            default:
                break;
        }
        return Respuesta;
    }

    private String RespuestasWs(String RespuestaWeb) {

        switch (RespuestaWeb) {
            case "0":
                RespuestaWeb = "Archivo no existe "; //No implementado
                break;
            case "1":
                RespuestaWeb = "true"; //ok
                break;
            case "2":
                RespuestaWeb = "Falla de integridad ";
                break;
            case "3":
                RespuestaWeb = "Error al descomprimir ";
                break;
            case "4":
                RespuestaWeb = "Otros... " + RespuestaWeb;
                break;
            default:
                break;
        }
        return RespuestaWeb;
    }

    /**
     * kim metodo que valida si puede actualizar a una nueva version
     *
     * @param metodo     nombre del metodo que consuluta en el webservice
     * @param directorio donde esta alojada la nueva ersion
     * @param serial     serial del equipo
     * @return
     * @throws Exception
     */
    public String validarVersion(String metodo, String directorio, String serial) throws Exception {

        WS_METHOD_NAME = metodo;
        result = "";
        Log.e("datos", metodo + " " + directorio + " " + serial);
        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);

        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
        p.setName("DirectorioDondeExiste");
        p.setValue(directorio);
        p.setType(String.class);
        request.addProperty(p);

        PropertyInfo p2 = new PropertyInfo();
        p2.setName("serie");
        p2.setValue(serial);
        p2.setType(String.class);
        request.addProperty(p2);


        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);

        try {
            httpTransport.debug = true;

            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service

            //   SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            Object response = (Object) envelope.getResponse();
            result = response.toString();

            //result = response.getProperty(0).toString();


        } catch (IOException | XmlPullParserException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    //Ax: ...va por fecha?
//    public String tryAll(String metodo, String ser) {
//
//        WS_METHOD_NAME = metodo;
//        result = "";
//
//        SoapObject request = new SoapObject(WS_NAMESPACE, WS_METHOD_NAME);
//
//        PropertyInfo p = new PropertyInfo(); //Ax: Añadir parametros
//        p.setName("serial");
//        p.setValue(ser);
//        p.setType(String.class);
//        request.addProperty(p);
//
//        result = "";
//
//        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
//
//        envelope.dotNet = true;
//
//        envelope.setOutputSoapObject(request);
//
//        HttpTransportSE httpTransport = new HttpTransportSE(WS_URL_PAGE);
//
//        try {
////            httpTransport.debug = true;
////            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service
////            SoapObject obj1 = (SoapObject) envelope.getResponse();
////            result = obj1.getProperty(0).toString();
//
//            httpTransport.debug = true;
//
//            httpTransport.call((WS_NAMESPACE + WS_METHOD_NAME), envelope);// Ax: Enviando la petición al web service
//
//            //   SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
//
//            Object response = (Object) envelope.getResponse();
//            result = response.toString();
//
//        } catch (IOException | XmlPullParserException e) {
//            Log.d("logAx", e.getMessage());
//            result = e.getMessage();
//        } catch (Exception e) {
//            result = e.getMessage();
//        }
//        return result;
//    }

    public void run() {
        this.run();
    }
}
