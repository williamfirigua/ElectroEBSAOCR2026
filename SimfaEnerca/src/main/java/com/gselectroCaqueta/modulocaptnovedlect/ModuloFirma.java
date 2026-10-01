package com.gselectroCaqueta.modulocaptnovedlect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gselectroCaqueta.accesoyseguridad.R;
import com.gsutil.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class ModuloFirma extends AppCompatActivity {

    RelativeLayout reloutmf;
    CustomView View;
    Button btnmfErase;
    Button btnmfDelete;
    Button btnmfDraw;
    Button btnmfSave;
    TextView lblmfCuenta;
    File logfile;
    Utils utils = new Utils();//Ax log y utilidades
    String cuenta = "";
    String AppPath = "";
    boolean dibujo = false;
    boolean firmo = false;
    String ruta = "";
    String rutaF = "";
    boolean firmaEst = false;
    private Bitmap DrawBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private Path mPath;
    private Paint DrawBitmapPaint;
    private Bitmap mBitmapFromSdcard = BitmapFactory.decodeFile("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo_firma);

        btnmfErase = (Button) findViewById(R.id.btnmfErase);
        btnmfDelete = (Button) findViewById(R.id.btnmfDelete);
        btnmfDraw = (Button) findViewById(R.id.btnmfDraw);
        btnmfSave = (Button) findViewById(R.id.btnmfSave);

        lblmfCuenta = (TextView) findViewById(R.id.lblmfCuenta);

        Bundle bundle = getIntent().getExtras();

        AppPath = bundle.getString("directorioactual");
        cuenta = bundle.getString("cuenta");
        lblmfCuenta.setText("Cuenta: " + cuenta);


        View = new CustomView(this);
        reloutmf = (RelativeLayout) findViewById(R.id.reloutmf);
        reloutmf.addView(View);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(android.R.color.holo_green_dark));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);

        if (bundle.getString("rutafirma") != null) {
            rutaF = bundle.getString("rutafirma");
            firmaEst = bundle.getBoolean("firmaEst");
            ruta = rutaF;
            Log.e("foto", ruta + " zz00 " + firmaEst);
            if (firmaEst) {
                firmo = firmaEst;
                try {
                    mBitmapFromSdcard = BitmapFactory.decodeFile(rutaF);
                } catch (Exception e) {
                }
            }
        }

        btnmfErase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                opociones(1);
            }
        });

        btnmfDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                opociones(2);
            }
        });

        btnmfDraw.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                opociones(3);
            }
        });

        btnmfSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                opociones(4);
            }
        });


        SetIconButtons();
    }//End Oncreate

    @Override
    public void onBackPressed() {
        if (firmaEst) {
            getSendData();

        } else {
            mensajes("Debe Firmar y Guardar");
        }
    }

    public void getSendData() {

        Intent iBackActivity = new Intent(this, ModuloAdicionarCertificado.class);
        iBackActivity.putExtra("firmo", firmo);
        iBackActivity.putExtra("ruta", ruta);
        setResult(RESULT_OK, iBackActivity);
        finish();
    }

    private void SetIconButtons() {

        int tamano = 14;
        Drawable drawable = getResources().getDrawable(R.mipmap.borrador);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 1), (int) (drawable.getIntrinsicHeight() * 1));
        ScaleDrawable sd = new ScaleDrawable(drawable, 0, tamano, tamano);
        Button btn = (Button) findViewById(R.id.btnmfErase);
        btn.setCompoundDrawables(sd.getDrawable(), null, null, null);

        Drawable drawable2 = getResources().getDrawable(R.mipmap.lapiz);
        drawable2.setBounds(0, 0, (int) (drawable2.getIntrinsicWidth() * 1), (int) (drawable2.getIntrinsicHeight() * 1));
        ScaleDrawable sd2 = new ScaleDrawable(drawable2, 0, tamano, tamano);
        Button btn2 = (Button) findViewById(R.id.btnmfDraw);
        btn2.setCompoundDrawables(sd2.getDrawable(), null, null, null);

        Drawable drawable3 = getResources().getDrawable(R.mipmap.borrarimagen);
        drawable3.setBounds(0, 0, (int) (drawable3.getIntrinsicWidth() * 1), (int) (drawable3.getIntrinsicHeight() * 1));
        ScaleDrawable sd3 = new ScaleDrawable(drawable3, 0, tamano, tamano);
        Button btn3 = (Button) findViewById(R.id.btnmfDelete);
        btn3.setCompoundDrawables(sd3.getDrawable(), null, null, null);
    }

    public void opociones(int i) {

        mPaint.setXfermode(null);
        switch (i) {
            case 1://Borrador
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                break;

            case 2://delete
                View = new CustomView(this);
                dibujo = false;
                break;

            case 3://draw
                mPaint.setXfermode(null);
                break;

            case 4://save

                if (!dibujo && !firmaEst) {
                    mensajes("debe crear firma!");
                    break;
                }
                if (firmaEst) {
                    getSendData();
                    break;
                }

                String path = (AppPath + "/DCIM/FOTOGRAFIASL/F" + cuenta + "CE" + getFecha() + ".png");
                File file = new File(path);
                ruta = path;

                try {
                    DrawBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                    firmo = true;
                    mensajes("Firma guardada:");
                    if (firmo) {
                        getSendData();
                    }

                } catch (Exception e) {
                    mensajes("ERROR" + e.toString());
                }
                break;
        }
    }

    public String getFecha() {

        Calendar calendar = Calendar.getInstance();
        String anno = String.format("%1$4s", calendar.get(Calendar.YEAR)).replace(" ", "0");
        String mes = String.format("%1$2s", calendar.get(Calendar.MONTH) + 1).replace(" ", "0");
        String fecha = (anno + mes);

        return fecha;
    }

    private void mensajes(String msg) {
        Toast toast = Toast.makeText(ModuloFirma.this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 10, 50);
        toast.show();
    }

    public class CustomView extends View {

        private static final float TOUCH_TOLERANCE = 4;
        private float mX, mY;

        @SuppressWarnings("deprecation")
        public CustomView(Context c) {

            super(c);
            Display Disp = getWindowManager().getDefaultDisplay();
            DrawBitmap = Bitmap.createBitmap(Disp.getWidth(), Disp.getHeight(),
                    Bitmap.Config.ARGB_4444);

            mCanvas = new Canvas(DrawBitmap);

            mPath = new Path();
            DrawBitmapPaint = new Paint(Paint.DITHER_FLAG);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            setDrawingCacheEnabled(true);
            if (firmaEst) {
                canvas.drawBitmap(mBitmapFromSdcard, 0, 0, DrawBitmapPaint);
            } else {
                canvas.drawBitmap(DrawBitmap, 0, 0, DrawBitmapPaint);
            }
            canvas.drawPath(mPath, mPaint);
            canvas.drawRect(mY, 0, mY, 0, DrawBitmapPaint);
        }

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {

            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {

            mPath.lineTo(mX, mY);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            dibujo = true;
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}