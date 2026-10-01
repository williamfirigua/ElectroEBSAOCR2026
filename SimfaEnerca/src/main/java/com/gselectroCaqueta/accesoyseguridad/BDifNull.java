package com.gselectroCaqueta.accesoyseguridad;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmResults;

public class BDifNull {
    private Realm realm;
    public BDifNull(Context ctx){Realm.init(ctx);}

    public CrudifNull[] obtnerdatos(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<CrudifNull> realmResults = realm.where(CrudifNull.class).findAll();
        return realmResults.toArray(new CrudifNull[realmResults.size()]);
    }
    public CrudifNull obtenerdatabyid(int id){
        Realm realm = Realm.getDefaultInstance();
        CrudifNull results = realm.where(CrudifNull.class).equalTo("id",id).findFirst();
        return results;
    }
    public void actualizarData(CrudifNull crudifNull, String fila, String operario, String impresora, String path, String nivelOperador){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        crudifNull.setFila(fila);
        crudifNull.setOperario(operario);
        crudifNull.setImpresora(impresora);
        crudifNull.setPath(path);
        crudifNull.setNivelOperador(nivelOperador);
        realm.commitTransaction();
    }
    public void guardarDatos(int id, String imei, String fila, String operario, String impresora, String path, String nivelOperador){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        CrudifNull crudifNull =realm.createObject(CrudifNull.class,id);
        crudifNull.setImei(imei);
        crudifNull.setFila(fila);
        crudifNull.setOperario(operario);
        crudifNull.setImpresora(impresora);
        crudifNull.setPath(path);
        crudifNull.setNivelOperador(nivelOperador);
        realm.commitTransaction();
    }
    /*
    public BDifNull(Context ctx){Realm.init(ctx);}

    public static String setBDifnull(CrudifNull crudifNull){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            CrudifNull cdnull = realm.createObject(CrudifNull.class);
            cdnull.setId(crudifNull.getId());
            cdnull.setImei(crudifNull.getImei());
            cdnull.setFila(crudifNull.getFila());
            cdnull.setOperario(crudifNull.getOperario());
            cdnull.setImpresora(crudifNull.getImpresora());
            cdnull.setPath(crudifNull.getPath());
            cdnull.setNivelOperador(crudifNull.getNivelOperador());
            CrudifNull realmUser = realm.copyToRealm(cdnull);
            realm.commitTransaction();

            if (realmUser.getImei().trim() != "") return realmUser.getImei();
        }catch (Exception ex) {
            return ex.toString();
        } finally {
            if (realm != null) realm.close();
        }
        return "No hubo insert";
    }
//    public void GuardarData(int id,String imei,String fila,String operario,String impresora,String path,String nivelOperador){
//        realm.beginTransaction();
//        CrudifNull cn = realm.createObject(CrudifNull.class,id);
//        cn.setImei(imei);
//        cn.setFila(fila);
//        cn.setOperario(operario);
//        cn.setImpresora(impresora);
//        cn.setPath(path);
//        cn.setNivelOperador(nivelOperador);
//        realm.commitTransaction();
//    }
//    public CrudifNull[] obtenerN(){
//        RealmResults<CrudifNull> realmResults = realm.where(CrudifNull.class).findAll();
//        return realmResults.toArray( new CrudifNull[realmResults.size()]);
//    }
//    public CrudifNull obtenerData(int id){
//        CrudifNull result = realm.where(CrudifNull.class).equalTo("id",id).findFirst();
//        return result;
//    }
    public int actualizarDatat(String fila, String operario, String impresora, String path, String nivelOperador){
        int tmp = 0;
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            CrudifNull crudifNull = realm.where(CrudifNull.class).equalTo("id",1).findFirst();
            if (crudifNull != null){
                crudifNull.setFila(fila);
                crudifNull.setOperario(operario);
                crudifNull.setImpresora(impresora);
                crudifNull.setPath(path);
                crudifNull.setNivelOperador(nivelOperador);
                realm.insertOrUpdate(crudifNull);
            }
            realm.commitTransaction();
            if (crudifNull != null) {
                tmp = crudifNull.getId();
            }
        } catch (Exception ex) {

        } finally {
            if (realm != null) realm.close();
        }
        return tmp;

    }
    public static int GetID() {
        int id;

        try (Realm realm = Realm.getDefaultInstance()) {
            CrudifNull bdgse = realm.where(CrudifNull.class).equalTo("id", 1).findFirst();

            if (bdgse == null || bdgse.getId() > 0) {
                return 0;
            } else {
                id = bdgse.getId();
            }
        }
        return id;
    }
    public static String getTelefonoImei() {
        String imei;

        try (Realm realm = Realm.getDefaultInstance()) {
            CrudifNull bdgse = realm.where(CrudifNull.class).equalTo("id", 1).findFirst();

            if (bdgse == null || bdgse.getImei() == "") {
                return "0";
            } else {
                imei = bdgse.getImei();
            }
        }
        return imei;
    }
    public static String getniveloperario() {
        String nivel;

        try (Realm realm = Realm.getDefaultInstance()) {
            CrudifNull bdgse = realm.where(CrudifNull.class).equalTo("id", 1).findFirst();

            if (bdgse == null || bdgse.getNivelOperador() == "") {
                return "0";
            } else {
                nivel = bdgse.getNivelOperador();
            }
        }
        return nivel;
    }
    public static String Getfila() {
        String fila;

        try (Realm realm = Realm.getDefaultInstance()) {
            CrudifNull bdgse = realm.where(CrudifNull.class).equalTo("id", 1).findFirst();

            if (bdgse == null || bdgse.getFila() == "") {
                return "0";
            } else {
                fila = bdgse.getFila();
            }
        }
        return fila;
    }
    public static String GetImpresora() {
        String impresora;

        try (Realm realm = Realm.getDefaultInstance()) {
            CrudifNull bdgse = realm.where(CrudifNull.class).equalTo("id", 1).findFirst();

            if (bdgse == null || bdgse.getImpresora() == "") {
                return "0";
            } else {
                impresora = bdgse.getImpresora();
            }
        }
        return impresora;
    }
    public static String GetPath() {
        String path;

        try (Realm realm = Realm.getDefaultInstance()) {
            CrudifNull bdgse = realm.where(CrudifNull.class).equalTo("id", 1).findFirst();

            if (bdgse == null || bdgse.getPath() == "") {
                return "0";
            } else {
                path = bdgse.getPath();
            }
        }
        return path;
    }
    public static String GetOperario() {
        String operario;

        try (Realm realm = Realm.getDefaultInstance()) {
            CrudifNull bdgse = realm.where(CrudifNull.class).equalTo("id", 1).findFirst();

            if (bdgse == null || bdgse.getOperario() == "") {
                return "0";
            } else {
                operario = bdgse.getOperario();
            }
        }
        return operario;
    }
    public static void truncado() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(CrudifNull.class);
            }
        });
        if (realm != null) realm.close();
    }*/
}
