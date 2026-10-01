package com.gselectroCaqueta.moduloscannerhardware;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.scandecode.ScanDecode;
import com.scandecode.inf.ScanInterface;

public class ModuloScannerKT40Q extends AppCompatActivity {

    private Button btnStopScan, btnScan;
    private ScanInterface scanDecode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_scanner_kt40_q);

        scanDecode = new ScanDecode(this);
        scanDecode.initService("true");//Initialize the scan service
        btnStopScan = (Button) findViewById(R.id.btnStopScan);
        btnScan = (Button) findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                scanDecode.starScan();//Start scan
            }
        });

        btnStopScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                scanDecode.stopScan();//stop scan
                handler.removeCallbacks(startTask);
            }
        });

        scanDecode.getBarCode(new ScanInterface.OnScanListener() {
            @Override
            public void getBarcode(String data) {

                getSendData(data);
                //String h= ("Scan times："+scancount+"");
                //mReception.append(data+"\n");
            }
        });
    }//end oncreate

    Handler handler = new Handler();

    //Continuous scan
    private Runnable startTask = new Runnable() {
        @Override
        public void run() {
            scanDecode.starScan();
            handler.postDelayed(startTask, 300);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanDecode.onDestroy();//Restore the initial state
    }

    public void getSendData(String bcode) { //Ax. envia datos a la actividad que la llamo y cierra esta

        Intent iBackActivity = new Intent(this, com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion.class);
        iBackActivity.putExtra("barcode", bcode);
        setResult(Activity.RESULT_OK, iBackActivity);
        finish();
    }
}
