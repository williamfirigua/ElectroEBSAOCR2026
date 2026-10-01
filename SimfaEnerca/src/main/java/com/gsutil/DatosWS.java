
package com.gsutil;

import android.os.AsyncTask;

public class DatosWS extends AsyncTask<String, Void, Object> {

    public AsyncResponse delegate = null;
    private String respuesta;
    private String Url;
    private String paginaWs;
    private String Method;

    public DatosWS(String url, String web, String method, int x) {
        Url = url;
        paginaWs = web;
        Method = method;
    }

    protected Integer doInBackground(String... arg) {

        WSSoap wsoap = new WSSoap(Url, paginaWs);
        respuesta = wsoap.verificarWs(Method);
        return 1;
    }

    protected void onPostExecute(Object result) {

        delegate.processFinish(respuesta);
    }

    public String getUrl() {
        return Url;
    }//Ax borrar estos

    public void setUrl(String url) {
        Url = url;
    }

    public String getMethod() {
        return Method;
    }

    public void setMethod(String method) {
        Method = method;
    }

    public String getPaginaWs() {
        return paginaWs;
    }

    public void setPaginaWs(String paginaWs) {
        this.paginaWs = paginaWs;
    }

}