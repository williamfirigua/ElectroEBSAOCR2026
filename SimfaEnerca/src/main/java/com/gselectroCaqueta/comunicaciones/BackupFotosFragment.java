package com.gselectroCaqueta.comunicaciones;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gselectroCaqueta.accesoyseguridad.R;

public class BackupFotosFragment extends Fragment {

    OnHeadSelectedListener mCallBack;

    Button btnBkFotos;
    Button btnBorrarBackup;
    Button btnValidaVersion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.backup_fotos_layout, container, false);

        btnBkFotos = (Button) view.findViewById(R.id.btnBkFotos);
        btnBorrarBackup = (Button) view.findViewById(R.id.btnBorrarBackup);
        btnValidaVersion = (Button) view.findViewById(R.id.btnValidaVersion);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//       	btnBkFotos.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//
//				//Envia el mensaje a la actividad contenedora
//				mCallBack.onArticleSelected(v);
//			}
//		});
    }

    ;

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        Activity a = null;

        if (context instanceof Activity) {

            a = (Activity) context;
        }

        //Nos aseguramos que la actividad contenedora halla recibido la interfaz de retrollamada
        //Sino lanzamos una excepcion.
        try {

            mCallBack = (OnHeadSelectedListener) a;
        } catch (ClassCastException e) {

            throw new ClassCastException(a.toString() + " debe implementar OnHeadSelectedListener");
        }


    }

    //La activida contenedora debe implementar esta interface
    public interface OnHeadSelectedListener {

        public void onArticleSelected(View v);
    }
}