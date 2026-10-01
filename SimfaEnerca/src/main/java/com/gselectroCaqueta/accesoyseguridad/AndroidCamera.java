package com.gselectroCaqueta.accesoyseguridad;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.journeyapps.barcodescanner.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AndroidCamera extends Activity {
    private Camera myCamera;
    private CameraPreview camPreview;
    private PictureCallback pictureCallBk;
    private ImageButton btnCapture, btnChangeCancel, btnFlashOk;
    private Context myContext;
    private LinearLayout cameraPreview,camera_sc;
    private boolean isCamFront = false;
    private ImageView imgViewFoto;
    private FrameLayout cameraframe;
    private boolean isFlashOn = false;
    private boolean hayFlash = false;
    private boolean bloqueoCam = false;
    private boolean hayZoom = false;
    private boolean bloqueoCanOk = true;
    private byte[] byteCameraData;
    private int rotacion = 0;
    private AudioManager audiom;
    private String nombre = "_";
    private ZoomControls zoomControls;
    private SeekBar seekBar;
    private int maxZoom = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        nombre = bundle.getString("Nombre");
        myContext = this;

        // camera_sc= (LinearLayout) findViewById(R.id.camera_sc);

      //  initialize();
    }
//
//    public void initialize() {
//        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
//        camPreview = new CameraPreview(myContext, myCamera);
//        cameraPreview.addView(camPreview);
//
//        btnCapture = (ImageButton) findViewById(R.id.button_capture);
//        btnCapture.setOnClickListener(captrureListener);
//
//        btnChangeCancel = (ImageButton) findViewById(R.id.btnChangeCancel);
//        //btnChangeCancel.setOnClickListener(switchCameraListener);
//        btnChangeCancel.setEnabled(false);
//        btnChangeCancel.setOnClickListener(vistaPreviaListener);
//
//        imgViewFoto = (ImageView) findViewById(R.id.imgViewFoto);
//        btnFlashOk = (ImageButton) findViewById(R.id.btnFlashOk);
//
//        if (getBaseContext().getPackageManager().hasSystemFeature(
//                PackageManager.FEATURE_CAMERA_FLASH)) {
//            hayFlash = true;
//        }
//
//        btnFlashOk.setOnClickListener(flashListener);
//
//        cameraframe = (FrameLayout) findViewById(R.id.camera_frame);
//
//        seekBar = (SeekBar) findViewById(R.id.seekBar);
//        seekBar.setProgress(0);
//
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                zoomCamera(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
//
//    }
//
//    private void focusin() { //Ax: parametros de calidad
//        try {
//            Camera.Parameters params = myCamera.getParameters();
//            List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
//            Camera.Size sizePicture = supportedSizes.get(0);
//            params.setPictureSize(sizePicture.width, sizePicture.height);
//            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
//                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            }
//
//            if (!params.isZoomSupported()) {
//                hayZoom = false;
//                seekBar.setEnabled(false);
//            } else {
//                hayZoom = true;
//                maxZoom = params.getMaxZoom();
//                seekBar.setMax(maxZoom);
//            }
//            myCamera.setParameters(params);
//            myCamera.setDisplayOrientation(90);
//
//        } catch (Exception ex) {
//            Toast toast = Toast.makeText(myContext, "ex: " + ex, Toast.LENGTH_LONG);
//            toast.show();
//        }
//    }
//
//    private int findFrontFacingCamera() {
//        int cameraId = -1;
//        int numberOfCameras = Camera.getNumberOfCameras();
//
//        for (int i = 0; i < numberOfCameras; i++) {
//            CameraInfo info = new CameraInfo();
//            Camera.getCameraInfo(i, info);
//            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//                cameraId = i;
//                isCamFront = true;
//                break;
//            }
//        }
//        return cameraId;
//    }
//
//    private int findBackFacingCamera() {
//        int cameraId = -1;
//        int numberOfCameras = Camera.getNumberOfCameras();
//
//        for (int i = 0; i < numberOfCameras; i++) {
//            CameraInfo info = new CameraInfo();
//            Camera.getCameraInfo(i, info);
//            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
//                cameraId = i;
//                isCamFront = false;
//                break;
//            }
//        }
//        return cameraId;
//    }
//
//    public void onResume() {
//        super.onResume();
//        if (!hasCamera(myContext)) {
//            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
//            toast.show();
//            finish();
//        }
//        if (myCamera == null) {
//            if (findFrontFacingCamera() < 0) {
//                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
//                btnChangeCancel.setVisibility(View.GONE);
//            }
//            myCamera = Camera.open(findBackFacingCamera());
//
//            focusin();
//
//            pictureCallBk = getPictureCallback();
//            camPreview.refreshCamera(myCamera);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        releaseCamera();
//    }
//
//    private boolean hasCamera(Context context) {
//        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private PictureCallback getPictureCallback() {
//        PictureCallback picture = new PictureCallback() {
//
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//
//                byteCameraData = data;
//                setVistaPreviaFoto(data);
//                // camPreview.refreshCamera(myCamera);//refresh camera to continue preview
//            }
//        };
//        return picture;
//    }
//
//    private void setVistaPreviaFoto(byte[] data) {
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        seekBar.setVisibility(View.GONE);
//        imgViewFoto.setImageBitmap(bitmap);
//        myCamera.stopPreview();
//        cameraPreview.setVisibility(View.INVISIBLE);
//        imgViewFoto.setVisibility(View.VISIBLE);
//        btnCapture.setVisibility(View.INVISIBLE);
//        btnChangeCancel.setEnabled(true);
//        btnChangeCancel.setImageResource(R.drawable.icon_camera_cancel);
//
//        btnFlashOk.setOnClickListener(okListener);
//        btnFlashOk.setImageResource(R.drawable.icon_camera_ok);
//
//        float angle = cameraframe.getRotation();
//        if (angle == 0) {
//            rotacion = 90;
//        }
//        imgViewFoto.setRotation(rotacion); //Ax se gira y vuelve pequeño ¡?
//        imgViewFoto.setScaleX(2);
//        imgViewFoto.setScaleY(2);
//    }
//
//    private void tomarFoto() {
//        myCamera.takePicture(null, null, pictureCallBk);
//    }
//
//    private void setVistaPreviaCamara() {
//        imgViewFoto.setVisibility(View.INVISIBLE);
//        cameraPreview.setVisibility(View.VISIBLE);
//        btnCapture.setVisibility(View.VISIBLE);
//        myCamera.startPreview();
//        btnChangeCancel.setEnabled(false);
//        btnChangeCancel.setImageResource(R.drawable.icon_camera_flip);
//        seekBar.setVisibility(View.VISIBLE);
//        btnFlashOk.setOnClickListener(flashListener);
//        isFlashOn = false;
//        btnFlashOk.setImageResource(R.drawable.icon_camera_flashno); //Ax: dejamos apagado
//        bloqueoCam = false;
//        bloqueoCanOk = true;
//    }
//
//    OnClickListener captrureListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//
//            if (!bloqueoCam) {
//                bloqueoCam = !bloqueoCam; //Ax: esto evita mas clics
//                playSound(100);
//                tomarFoto();
//            }
//        }
//    };
//
//    private OnClickListener vistaPreviaListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (bloqueoCanOk) {
//                bloqueoCanOk = false;
//                playSound(-1);
//                setVistaPreviaCamara();
//            }
//        }
//    };
//
//    OnClickListener flashListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            playSound(1);
//            if (!hayFlash) return;
//
//            if (myCamera != null) {
//                try {
//                    Camera.Parameters param = myCamera.getParameters();
//                    param.setFlashMode(!isFlashOn ? Camera.Parameters.FLASH_MODE_TORCH
//                            : Camera.Parameters.FLASH_MODE_OFF);
//                    myCamera.setParameters(param);
//                } catch (Exception e) {
//                    // TODO: handle exception
//                }
//            }
//
//            if (isFlashOn) {
//                isFlashOn = false;
//                btnFlashOk.setImageResource(R.drawable.icon_camera_flashno);
//            } else {
//                isFlashOn = true;
//                btnFlashOk.setImageResource(R.drawable.icon_camera_flash);
//            }
//            bloqueoCanOk = true;
//        }
//    };
//
//    OnClickListener okListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (bloqueoCanOk) {//bloqueo el listener de cancel para evitar clics
//                bloqueoCanOk = false;
//                playSound(100);
//                setRutayNombreFoto();
//            }
//        }
//    };
//
//    private void setRutayNombreFoto() {
//
////        File mediaStorageDir = new File("/sdcard/", "/DCIM/Camera/");
////
////        if (!mediaStorageDir.exists()) {
////
////            if (!mediaStorageDir.mkdirs()) {
////                return;
////            }
//        //       }
//        FileOutputStream fos = null;
//        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        try {
//
//            //File pictureFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
//            File pictureFile = new File(nombre);
//
//            if (pictureFile == null) {
//                return;
//            }
//
////            Bitmap loadedImage;
////            Bitmap rotatedBitmap;
////            loadedImage = BitmapFactory.decodeByteArray(byteCameraData, 0, byteCameraData.length);
////
////            Matrix rotateMatrix = new Matrix();  //Las fotos salen horizontales hay que voltearlas
////            rotateMatrix.postRotate(rotacion);
////            rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), rotateMatrix, false);
////
////            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
////            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
//
//            fos = new FileOutputStream(pictureFile);
//            fos.write(byteCameraData);
//            fos.close();
//        } catch (FileNotFoundException e) {
//            setResult(RESULT_CANCELED);
//        } catch (IOException e) {
//            setResult(RESULT_CANCELED);
//        } finally {
//            try {
//                if (fos != null) {
//                    fos.close();
//                }
//
//            } catch (Exception ex) {
//            }
//        }
//        setResult(RESULT_OK);
//        finish();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        releaseCamera();
//    }
//
//    @Override
//    public void onBackPressed() {
////        setResult(RESULT_CANCELED);
////        finish();
//    }
//
//    private void releaseCamera() {
//        if (myCamera != null) {
//            myCamera.release();
//            myCamera = null;
//        }
//        if (byteCameraData != null) byteCameraData = null;
//    }
//
//    private void playSound(int keyCode) {
//
//        audiom = (AudioManager) getSystemService(AUDIO_SERVICE);
//
//        switch (keyCode) {
//
//            case -1:
//                audiom.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, 1f);
//                break;
//            case 100:
//                audiom.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID, 1f);
//                break;
//            default:
//                audiom.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 1f);
//        }
//    }
//
//    private void enableZoom() {
//        zoomControls = new ZoomControls(this);
//        zoomControls.setIsZoomInEnabled(true);
//        zoomControls.setIsZoomOutEnabled(true);
//        //zoomControls.hide();
//
//
////        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
////
////            @Override
////            public void onClick(View v) {
////                // TODO Auto-generated method stub
////                zoomCamera(false);
////
////            }
////        });
////
////        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
////
////            @Override
////            public void onClick(View v) {
////                // TODO Auto-generated method stub
////
////                zoomCamera(true);
////            }
////        });
//        //  camera_preview.addView(zoomControls);
//
//
//    }
//
//    public void zoomCamera(int zoomInOrOut) {
//
//        if (myCamera != null && hayZoom) {
//
//            Camera.Parameters parameter = myCamera.getParameters();
//
//            if (zoomInOrOut >= 0 && zoomInOrOut <= maxZoom) {
//                parameter.setZoom(zoomInOrOut);
//            }
//            myCamera.setParameters(parameter);
//        }
//    }

//    public void zoomCamera(boolean zoomInOrOut) {
//        if (myCamera != null) {
//            Camera.Parameters parameter = myCamera.getParameters();
//
//            if (parameter.isZoomSupported()) {
//                int MAX_ZOOM = parameter.getMaxZoom();
//                int currnetZoom = parameter.getZoom();
//                if (zoomInOrOut && (currnetZoom < MAX_ZOOM && currnetZoom >= 0)) {
//                    parameter.setZoom(++currnetZoom);
//                } else if (!zoomInOrOut && (currnetZoom <= MAX_ZOOM && currnetZoom > 0)) {
//                    parameter.setZoom(--currnetZoom);
//                }
//            } else
//                Toast.makeText(this, "Zoom Not Avaliable", Toast.LENGTH_LONG).show();
//
//            myCamera.setParameters(parameter);
//        }
//    }


//    OnClickListener switchCameraListener = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            int camerasNumber = Camera.getNumberOfCameras();
//            if (camerasNumber > 1) {
//
//                releaseCamera();
//                chooseCamera();
//            } else {
//                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
//                toast.show();
//            }
//        }
//    };

//    public void chooseCamera() {
//        if (isCamFront) {
//            int cameraId = findBackFacingCamera();
//            if (cameraId >= 0) {
//
//                myCamera = Camera.open(cameraId);
//                pictureCallBk = getPictureCallback();
//                camPreview.refreshCamera(myCamera);
//            }
//        } else {
//            int cameraId = findFrontFacingCamera();
//            if (cameraId >= 0) {
//                myCamera = Camera.open(cameraId);
//                pictureCallBk = getPictureCallback();
//                camPreview.refreshCamera(myCamera);
//            }
//        }
//    }

}