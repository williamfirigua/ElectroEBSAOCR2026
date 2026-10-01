package com.gselectroCaqueta.comunicaciones;

import android.content.Context;

import io.realm.Realm;

public class CrudComunicaciones {
    public CrudComunicaciones(Context ctx){
        Realm.init(ctx);}

    public static String setCofigUrl(BDComunicaciones bda){//int id,String URL,String paginaWs,String estado
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            BDComunicaciones bdc = realm.createObject(BDComunicaciones.class);
            bdc.setId(bda.getId());
            bdc.setURL(bda.getURL());
            bdc.setPaginaWs(bda.getPaginaWs());
            bdc.setEstado(bda.getEstado());
            bdc.setRutaAdministrador(bda.getRutaAdministrador());
            bdc.setPuertoApi(bda.getPuertoApi());
            BDComunicaciones realTrsn = realm.copyToRealm(bdc);
            realm.commitTransaction();
            if (realTrsn != null && realTrsn.getId() > 0){
                return "INSERCCION|URL:" + realTrsn.getId() + "EXITOSA";
            }
        }catch (Exception e){
            return e.toString();
        }finally {
            if (realm != null) realm.close();
        }
        return "No hubo insert";
    }

    public static boolean updateParams(BDComunicaciones bdc,int id){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            BDComunicaciones bdcup = realm.where(BDComunicaciones.class).equalTo("id",id).findFirst();
            if(bdcup != null){
                bdcup.setURL(bdc.getURL());
                bdcup.setPaginaWs(bdc.getPaginaWs());
                bdcup.setEstado(bdc.getEstado());
                bdcup.setPuertoApi(bdc.getPuertoApi());
                realm.insertOrUpdate(bdcup);
            }else {
                realm.commitTransaction();
                return false;
            }
            realm.commitTransaction();
            return true;

        }catch (Exception e){
            return false;
        }finally {
            if (realm != null) realm.close();
        }
    }
    public static boolean updateRutaAdmin(int id,String st){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            BDComunicaciones bdcup = realm.where(BDComunicaciones.class).equalTo("id",id).findFirst();
            if(bdcup != null){
                bdcup.setRutaAdministrador(st);
                realm.insertOrUpdate(bdcup);
            }else {
                realm.commitTransaction();
                return false;
            }
            realm.commitTransaction();
            return true;

        }catch (Exception e){
            return false;
        }finally {
            if (realm != null) realm.close();
        }
    }
    public static boolean updateStatus(int id,String st){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            BDComunicaciones bdcup = realm.where(BDComunicaciones.class).equalTo("id",id).findFirst();
            if(bdcup != null){
                bdcup.setEstado(st);
                realm.insertOrUpdate(bdcup);
            }else {
                realm.commitTransaction();
                return false;
            }
            realm.commitTransaction();
            return true;

        }catch (Exception e){
            return false;
        }finally {
            if (realm != null) realm.close();
        }
    }
    public static BDComunicaciones getParams(){
        BDComunicaciones datos = null;

        try(Realm realm = Realm.getDefaultInstance()){
            BDComunicaciones params = realm.where(BDComunicaciones.class).equalTo("estado","A").limit(1).findFirst();
            if (params != null) datos = realm.copyFromRealm(params);
        }
        return datos;
    }
    public static BDComunicaciones getParamsbyId(int id){
        BDComunicaciones datos = null;

        try(Realm realm = Realm.getDefaultInstance()){
            BDComunicaciones params = realm.where(BDComunicaciones.class).equalTo("id",id).limit(1).findFirst();
            if (params != null) datos = realm.copyFromRealm(params);
        }
        return datos;
    }
}
