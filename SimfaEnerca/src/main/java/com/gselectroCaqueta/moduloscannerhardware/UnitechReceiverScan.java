package com.gselectroCaqueta.moduloscannerhardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.gselectroCaqueta.accesoyseguridad.R;

public class UnitechReceiverScan extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if ("unitech.scanservice.software_scankey".equals(intent.getAction())) {

            final ModuloEscanerHardware meh = ModuloEscanerHardware.instance();
            final EditText txtScan = (EditText) meh.findViewById(R.id.txtScan);

            txtScan.requestFocus();

            txtScan.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                    if (i == EditorInfo.IME_ACTION_SEARCH || i == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {

                        if (!keyEvent.isShiftPressed()) {

                            if (!txtScan.getText().toString().equals("")) {

                                meh.setScannedInfo(txtScan.getText().toString());
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }
}
