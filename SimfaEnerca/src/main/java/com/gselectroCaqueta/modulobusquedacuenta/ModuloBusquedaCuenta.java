package com.gselectroCaqueta.modulobusquedacuenta;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;
import com.gselectroCaqueta.tablas.TablaClienteSalida;
import com.gselectroCaqueta.tablas.TablaEntradaClientes;
import com.gselectroCaqueta.tablas.TablaMedidorEntrada;
import com.gsutil.Utils;

import java.io.File;

public class ModuloBusquedaCuenta extends AppCompatActivity {

    final static String ACTIVITY_INFO = "com.globalsolutions.accesoyseguridad.MenuDeLiquidacion";
    public View.OnClickListener TablaListener; //Ax: escucha para capturar clic en rows de la tabla generada
    public int ClickedRow;//Ax: guarda el ID (cliente) al dar clic a una fila.
    TablaEntradaClientes infoClienteEntrada;
    TablaClienteSalida infoClienteSalida;
    TablaMedidorEntrada infoMedidorEntrada;

    RadioButton rbContador;
    RadioButton rbCuenta;
    RadioButton rbRuta;
    RadioButton rbDireccion;
    RadioButton rbNombre;

    RadioGroup rgBotones;

    TextView txtBuscar;
    Utils utils = new Utils();
    File logfile;

    Resources rs;

    // Lista datos
    TableLayout tabla;
    TableLayout cabecera;
    TableRow.LayoutParams layoutCampo;
    TableRow.LayoutParams layoutValor;
    TableRow.LayoutParams layoutFila;
    TableRow fila;

    int txtMaxLength = 16; //Ax: manejaran el largo de la entrada de texto respecto a cada radiobutton
    int txtMinLength = 3;
    int limitador = 0; //Ax: limita el numero de rows creadas a 6
    int opcion = 0; //Ax: define que busqueda se realiza y se la envia a liquidacion
    Button btnvol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_busqueda_cuenta);

        rs = this.getResources();

        infoClienteEntrada = new TablaEntradaClientes();
        infoClienteSalida = new TablaClienteSalida();
        infoMedidorEntrada = new TablaMedidorEntrada();

        rbContador = (RadioButton) findViewById(R.id.rbContador);
        rbCuenta = (RadioButton) findViewById(R.id.rbCuenta);
        rbRuta = (RadioButton) findViewById(R.id.rbRuta);
        rbDireccion = (RadioButton) findViewById(R.id.rbDireccion);
        rbNombre = (RadioButton) findViewById(R.id.rbNombre);
        rgBotones = (RadioGroup) findViewById(R.id.rgBotones);

        txtBuscar = (TextView) findViewById(R.id.txtBuscar);

        btnvol = (Button)findViewById(R.id.btnvol);

        tabla = (TableLayout) findViewById(R.id.tablaResumenDatos);
        cabecera = (TableLayout) findViewById(R.id.cabeceraDatos);
        layoutFila = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutCampo = new TableRow.LayoutParams(200, TableRow.LayoutParams.WRAP_CONTENT);
        layoutValor = new TableRow.LayoutParams(500, TableRow.LayoutParams.WRAP_CONTENT);

        txtBuscar.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (txtBuscar.getText().length() < txtMinLength) {
                    Toast.makeText(getApplicationContext(), "Rango de Caracteres de " + txtMinLength + " a " + txtMaxLength, Toast.LENGTH_LONG).show();
                    return false;
                }

                if (actionId == 6||actionId == 5)//Ax "ENTER" telefono chino ax = 5 otro = 6
                {
//                    if (txtBuscar.getText().length() == txtMaxLength && opcion == 2) {
//                        volveraLiquidacion(); //Cuando el numeo es de 9 digitos se busca por binario en liquidacion
//                    }
                    limitador = 0;
                    borrarListaInformacionCliente();

                    if (txtBuscar.getText().toString().trim().length() > 0) {
                        //llenar las variables para buscar la cuenta
                        //Variables.DatoDeBusqueda = txtBuscar.Text.Trim();
                        VariablesGlobales.datodebusqueda = "0";
                        txtBuscar.setText(txtBuscar.getText().toString().trim().toUpperCase());
                        buscarIdCuenta(txtBuscar.getText().toString().trim());
                        if (tabla.getChildCount() > 0) {
                            tabla.requestFocus();
                            //ListaResumen.Items[0].Selected = true;
                        } else {
                            Toast.makeText(getApplicationContext(), "Parametro de busqueda no Existe: " + txtBuscar.getText(), Toast.LENGTH_LONG).show();
                            //txtBuscar.setText("");
                            txtBuscar.requestFocus();
                        }
                    } else {
                        VariablesGlobales.datodebusqueda = "0";
                        finish();
                    }
                }
                return false;
            }
        });

        txtBuscar.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (txtBuscar.getText().length() > txtMaxLength) {
                    txtBuscar.setText(txtBuscar.getText().toString().substring(0, txtMaxLength));
                    EditText etext = (EditText) findViewById(R.id.txtBuscar);//Ax: Coloca el cursor al final
                    etext.setSelection(etext.getText().length());
                }
            }
        });

        rgBotones.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == rbContador.getId()) {
                    txtBuscar.setRawInputType(InputType.TYPE_CLASS_NUMBER);//Ax. convertir teclado numerico
                    txtMaxLength = 16;
                    txtMinLength = 3;
                    opcion = 1;
                    //"Contador";
                }
                if (checkedId == rbCuenta.getId()) {
                    txtBuscar.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                    txtMaxLength = 9;
                    txtMinLength = 6;
                    opcion = 2;
                    // "Cuenta";
                }
                if (checkedId == rbRuta.getId()) {
                    txtBuscar.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                    txtMaxLength = 13;
                    txtMinLength = 4;
                    opcion = 3;
                    //"Ruta";
                }
                if (checkedId == rbDireccion.getId()) {
                    txtBuscar.setRawInputType(InputType.TYPE_CLASS_TEXT);
                    txtMaxLength = 63;
                    txtMinLength = 8;
                    opcion = 4;
                    //"Direccion";
                }
                if (checkedId == rbNombre.getId()) {
                    txtBuscar.setRawInputType(InputType.TYPE_CLASS_TEXT);
                    txtMaxLength = 48;
                    txtMinLength = 3;
                    opcion = 5;
                    //"Nombre";
                }

                txtBuscar.setText("");
                txtBuscar.requestFocus();
                //Toast.makeText(getApplicationContext(), "El RadioButton " + nombre + " fue seleccionado", Toast.LENGTH_SHORT).show();
            }
        });

        TablaListener = new View.OnClickListener() {

            public void onClick(View v) {
                v.setBackgroundColor(Color.rgb(255, 255, 153));
                int ClickedRow_old = ClickedRow;
                ClickedRow = v.getId();
                if (ClickedRow == ClickedRow_old) {//Comprobar doble clic no seguido
                    if (opcion == 2)
                        opcion = 6; //Ax: ya lo encontro aqui por busqueda normal, se cambia opcion para no hacer de nuevo la busqueda alla (binaria)
                    volveraLiquidacion();
                }
            }
        };

        btnvol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        logfile = new File(VariablesGlobales.directorioactual + "/DATOSDESALIDA/LOGEVENTOS.LOG"); //Ax: para logs de error
        moduloBusquedaCuenta_Load();
        rbContador.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void abrirArchivosDeFacturacion() {
        try {
            infoMedidorEntrada.setArchivo_TablaMedidorEntrada(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/MEDIDOR.TXT");
            infoClienteEntrada.setArchivo_TablaEntradaClientes(VariablesGlobales.directorioactual + "/DATOSDEENTRADA/CLIENTE.TXT");
            infoClienteSalida.setArchivo_TablaClienteSalida(VariablesGlobales.directorioactual + "/DATOSDESALIDA/CLIENTE.SDA");

            infoClienteEntrada.abrir_TablaEntradaClientes(infoClienteEntrada.getArchivo_TablaEntradaClientes());
            infoMedidorEntrada.abrir_TablaMedidorEntrada(infoMedidorEntrada.getArchivo_TablaMedidorEntrada());
            infoClienteSalida.abrir_TablaClienteSalida(infoClienteSalida.getArchivo_TablaClienteSalida());
        } catch (Exception e) {
            utils.Log(logfile, "[ModuloBusquedaCuenta] Error en abrirArchivosDeFacturacion()" + e.getMessage());
            Toast.makeText(getApplicationContext(), "Problema Abriendo\nArchivos de Facturacion", Toast.LENGTH_LONG).show();
        }
    }

    private void cerraArchivosDeFacturacion() {
        try {
            infoClienteEntrada.Cerrar_TablaEntradaClientes();
            infoMedidorEntrada.Cerrar_TablaMedidorEntrada();
            infoClienteSalida.Cerrar_TablaClienteSalida();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Problema Cerrando\nArchivos de Facturacion", Toast.LENGTH_LONG).show();
        }
    }

    private void buscarIdCuenta(String idBuscar) {
        abrirArchivosDeFacturacion();

        if (rbContador.isChecked() || (rbCuenta.isChecked()))

            //buscar en medidor
            buscarIdMedidor(idBuscar);
        else {
            //buscar en cliente
            buscarIdCliente(idBuscar);
        }
        cerraArchivosDeFacturacion();
    }

    public void buscarIdMedidor(String codigo) {
        for (int i = 1; i <= (int) (infoMedidorEntrada.getTotal_TablaMedidorEntrada()); i++) {
            infoMedidorEntrada.lectura_TablaMedidorEntrada(i);
            if (rbContador.isChecked()) {
                if (infoMedidorEntrada.gettablaMedidorEntrada_NUMero().indexOf(codigo.trim()) >= 0) {
                    //leer cliente
                    infoClienteEntrada.lectura_TablaEntradaClientes(Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_cliente()));
                    infoClienteSalida.lectura_TablaClienteSalida(Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_cliente()));
                    //llenar la lista;
                    if (limitador == 6) {
                        limitador = 0;

                        return;
                    }
                    llenarListaResumen();
                }
            } else {
                if (infoMedidorEntrada.gettablaMedidorEntrada_Cuenta().indexOf(codigo.trim()) >= 0) {
                    //leer cliente
                    infoClienteEntrada.lectura_TablaEntradaClientes(Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_cliente()));
                    infoClienteSalida.lectura_TablaClienteSalida(Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_cliente()));
                    //llenar la lista;
                    if (limitador == 6) {
                        limitador = 0;
                        return;
                    }
                    llenarListaResumen();
                }
            }
        }
        return;
    }

    public void buscarIdCliente(String codigo) {
        for (int i = 1; i <= (int) (infoClienteEntrada.getTotal_TablaEntradaClientes()); i++) {
            infoClienteEntrada.lectura_TablaEntradaClientes(i);
            infoClienteSalida.lectura_TablaClienteSalida(i);
            if (rbRuta.isChecked()) {
                if (infoClienteEntrada.gettablaEntradaClientes_Ruta().indexOf(codigo.trim()) >= 0) {
                    //leer cliente
                    infoMedidorEntrada.lectura_TablaMedidorEntrada(Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_primermedidor()));
                    //llenar la lista;
                    if (limitador == 6) return;
                    llenarListaResumen();
                }
            } else {
                if (rbDireccion.isChecked()) {
                    if (infoClienteEntrada.gettablaEntradaClientes_Direccion().indexOf(codigo.trim()) >= 0) {
                        //leer cliente
                        infoMedidorEntrada.lectura_TablaMedidorEntrada(Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_primermedidor()));
                        //llenar la lista;
                        if (limitador == 6) return;
                        llenarListaResumen();
                    }
                } else {
                    if (infoClienteEntrada.gettablaEntradaClientes_Nombre().indexOf(codigo.trim()) >= 0) {
                        //leer cliente
                        infoMedidorEntrada.lectura_TablaMedidorEntrada(Integer.parseInt(infoClienteEntrada.gettablaEntradaClientes_primermedidor()));
                        //llenar la lista;
                        if (limitador == 6) return;
                        llenarListaResumen();
                    }
                }
            }
        }
        return;
    }

    private void llenarListaResumen() {

        limitador++;

        String campo;
        String valor;
        if (rbContador.isChecked()) {

            campo = infoMedidorEntrada.gettablaMedidorEntrada_NUMero();
        } else {

            if (rbCuenta.isChecked()) {

                campo = infoMedidorEntrada.gettablaMedidorEntrada_Cuenta();
            } else {

                if (rbRuta.isChecked()) {

                    campo = infoClienteEntrada.gettablaEntradaClientes_Ruta();
                } else {

                    if (rbDireccion.isChecked()) {

                        campo = infoClienteEntrada.gettablaEntradaClientes_Direccion();
                    } else {

                        campo = infoClienteEntrada.gettablaEntradaClientes_Nombre();
                    }
                }
            }
        }

        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);

        if (rbDireccion.isChecked())
            valor = infoMedidorEntrada.gettablaMedidorEntrada_idcortado();
        else
            valor = infoClienteEntrada.gettablaEntradaClientes_Direccion();

        insertarFilaListaResumen(campo, valor);
    }

    private void insertarFilaListaResumen(String campo, String valor) {

        TextView txtCampo;
        TextView txtValor;

        fila = new TableRow(this);
        fila.setLayoutParams(layoutFila);
        fila.setClickable(true);
        fila.setOnClickListener(TablaListener);

        if (opcion != 1) {
            fila.setId(Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_cliente().trim())); //Ax: colocarle la posicion del cliente
        } else {
            fila.setId(Integer.parseInt(infoMedidorEntrada.gettablaMedidorEntrada_primerregistro().trim()));
        }

        txtCampo = new TextView(this);
        txtValor = new TextView(this);

        txtCampo.setText(campo);
        txtCampo.setGravity(Gravity.LEFT);
        txtCampo.setTextAppearance(this, R.style.etiqueta);
        txtCampo.setBackgroundResource(R.drawable.tabla_celda);
        txtCampo.setLayoutParams(layoutCampo);

        txtValor.setText(valor);
        txtValor.setGravity(Gravity.LEFT);
        txtValor.setTextAppearance(this, R.style.campo);
        txtValor.setBackgroundResource(R.drawable.tabla_celda);
        txtValor.setLayoutParams(layoutValor);

        fila.addView(txtCampo);
        fila.addView(txtValor);
        tabla.addView(fila);
    }

    public void volveraLiquidacion() {//Ax: envia datos a la actividad que la llamo y se cierra

        Intent iBackActivity = new Intent(this, com.gselectroCaqueta.accesoyseguridad.MenuDeLiquidacion.class);
        if (txtBuscar.getText().length() == txtMaxLength && opcion == 2) {
            iBackActivity.putExtra("idcliente", txtBuscar.getText().toString());
        } else {
            iBackActivity.putExtra("idcliente", (ClickedRow + ""));
        }
        iBackActivity.putExtra("opcion", (opcion + ""));
        setResult(Activity.RESULT_OK, iBackActivity);
        finish();
    }

    private void borrarListaInformacionCliente() {
        tabla.removeAllViews();
    }

    private void moduloBusquedaCuenta_Load() {
        borrarListaInformacionCliente();
        txtBuscar.setText("");
        txtBuscar.requestFocus();
    }
}
