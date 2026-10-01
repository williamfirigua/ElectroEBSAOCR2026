package com.gselectroCaqueta.accesoyseguridad;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CrudifNull extends RealmObject {


    @PrimaryKey
    private int id;
    String imei,fila,operario,impresora,path,nivelOperador;

    //Metodos Getter

    public int getId() { return id; }

    public void setId(int id) {this.id = id;}
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public String getOperario() {
        return operario;
    }

    public void setOperario(String operario) {
        this.operario = operario;
    }

    public String getImpresora() {
        return impresora;
    }

    public void setImpresora(String impresora) {
        this.impresora = impresora;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNivelOperador() {
        return nivelOperador;
    }

    public void setNivelOperador(String nivelOperador) {
        this.nivelOperador = nivelOperador;
    }
}
