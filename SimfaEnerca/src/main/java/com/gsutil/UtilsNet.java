package com.gsutil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by usuario on 07/09/2016.
 */
public class UtilsNet {

    //Ax: Verifica si hay conexion
    private static boolean hayRed(Context contexto1) {
        ConnectivityManager connectivityManager = (ConnectivityManager) contexto1.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    //Ax: Verifica si hay internet
    public static boolean hayInternet(Context context) {
        if (hayRed(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                return false;
                //Log.e(LOG_TAG, "Error checking internet connection", e);
            }
        } else {
            //Log.d(LOG_TAG, "No network available!");
            return false;
        }
    }
}