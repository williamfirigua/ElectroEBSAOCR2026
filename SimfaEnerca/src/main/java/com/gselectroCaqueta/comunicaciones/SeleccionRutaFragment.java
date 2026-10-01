package com.gselectroCaqueta.comunicaciones;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.gselectroCaqueta.accesoyseguridad.R;

public class SeleccionRutaFragment extends Fragment {

    EditText txtCiclo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seleccion_ruta_layout, container, false);

        txtCiclo = (EditText) view.findViewById(R.id.txtUno);

        return view;
    }
}