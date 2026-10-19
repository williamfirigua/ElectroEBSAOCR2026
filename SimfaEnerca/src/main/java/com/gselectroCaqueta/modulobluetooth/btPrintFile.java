package com.gselectroCaqueta.modulobluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gselectroCaqueta.accesoyseguridad.VariablesGlobales;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by hgode on 04.04.2014.
 */
public class btPrintFile {
    // Constants that indicate the current connection state
    /**
     * Candado de escritura al socket. ConnectedThread.write lo toma por llamada y ColaImpresion por
     * trabajo completo, asi un envio directo (reimpresion, prueba de cabeza) no se intercala entre los
     * bloques de una factura en cola. Es reentrante (monitor de Java).
     */
    public static final Object BLOQUEO_ESCRITURA = new Object();

    public static final int STATE_IDLE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_DISCONNECTED = 4;  // now connected to a remote device
    //vars
    // Debugging
    private static final String TAG = "btPrintFile";
    private static final boolean D = true;
    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mAdapter;
    private Context _context = null;
    private String _btMAC = "";
    private String _sFile = "";
    private BluetoothDevice mDevice = null;
    private Handler mHandler = null;
    private int mState;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public btPrintFile(Context context, Handler handler) {
        log("btPrintFile()");
        _context = context;
        mHandler = handler;
        //_btMAC=sBTmac;
        //_sFile=sFileName;
        mState = STATE_IDLE;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        addText("btPrintFile initialized 1");
    }
    // init a new btPrint with a conext for callbacks
    // the BT MAC as string and the file to be printed
    public btPrintFile(Handler handler, String sBTmac, String sFileName) {
        log("btPrintFile()");
        //_context=context;
        mHandler = handler;
        _btMAC = sBTmac;
        _sFile = sFileName;
        mState = STATE_IDLE;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        addText("btPrintFile initialized 2");
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        log("start");
        addText("start()");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_IDLE);   //idle
        addText("start done.");
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        log("stop");
        addText("stop()");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_DISCONNECTED);
        addText("stop() done.");
        VariablesGlobales.habilitadaimpresora = 0;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        addText(msgTypes.STATE, state);
    }

    public String printESCP() {
        String message = "w(          FUEL CITY\r\n" +
                "       8511 WHITESBURG DR\r\n" +
                "      HUNTSVILLE, AL 35802\r\n" +
                "         (256)585-6389\r\n\r\n" +
                " Merchant ID: 1312\r\n" +
                " Ref #: 0092\r\n\r\n" +
                "w)      Sale\r\n" +
                "w( XXXXXXXXXXX4003\r\n" +
                " AMEX       Entry Method: Swiped\r\n\r\n\r\n" +
                " Total:               $    53.22\r\n\r\n\r\n" +
                " 12/21/12               13:41:23\r\n" +
                " Inv #: 000092 Appr Code: 565815\r\n" +
                " Transaction ID: 001194600911275\r\n" +
                " Apprvd: Online   Batch#: 000035\r\n\r\n\r\n" +
                "          Cutomer Copy\r\n" +
                "           Thank You!\r\n\r\n\r\n\r\n";
        return message;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        addText("connecting to " + device);
        mDevice = device;
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            addText("already connected. Disconnecting first");
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        addText("new connect thread started");
        setState(STATE_CONNECTING);
    }
    public synchronized void disconect(){

        log("stop");
        addText("stop()");
        mConnectThread.cancel();
        setState(STATE_DISCONNECTED);
        addText("stop() done.");
        VariablesGlobales.habilitadaimpresora = 0;
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
        VariablesGlobales.habilitadaimpresora = 1;
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        addText("connectionLost()");
        setState(STATE_DISCONNECTED);
        // Ya no se deshabilita la impresora por perder el enlace (impresora apagada, lejos, sin bateria):
        // la siguiente impresion reconecta sola (reconectarSincrono). Deshabilitarla sigue siendo decision
        // del tecnico desde el menu "Des/Habilitar Impresora".

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.TOAST, "Conexion con la impresora perdida: se reconectara al imprimir");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Reconecta con la ultima impresora usada, bloqueando al llamador hasta {@code timeoutMs} (como el
     * Bluetooth_Printer de la app de lecturas EMSA). Llamar SOLO desde un hilo de trabajo (ColaImpresion),
     * nunca desde la pantalla. Intento normal por SPP con vigilante de tiempo (Android espera ~9 s si la
     * impresora esta apagada) y, si falla sin agotar el tiempo, un intento por el canal RFCOMM 1.
     *
     * @return true si quedo conectada (o ya lo estaba).
     */
    /** true si hay una impresora asociada en esta sesion (aunque el enlace este caido). */
    public synchronized boolean tieneImpresoraAsociada() {
        return mDevice != null;
    }

    public boolean reconectarSincrono(long timeoutMs) {
        final BluetoothDevice dispositivo;
        synchronized (this) {
            if (mState == STATE_CONNECTED && mConnectedThread != null) return true;
            dispositivo = mDevice;
            if (dispositivo == null || mAdapter == null || !mAdapter.isEnabled()) return false;
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
            setState(STATE_CONNECTING);
        }
        mAdapter.cancelDiscovery();
        BluetoothSocket socket = conectarConLimite(dispositivo, timeoutMs);
        if (socket == null) {
            synchronized (this) {
                setState(STATE_DISCONNECTED);
            }
            Log.w(TAG, "reconectarSincrono: sin conexion con " + dispositivo.getAddress());
            return false;
        }
        connected(socket, dispositivo);
        Log.i(TAG, "reconectarSincrono: reconectada " + dispositivo.getAddress());
        return true;
    }

    private BluetoothSocket conectarConLimite(BluetoothDevice dispositivo, long timeoutMs) {
        final java.util.concurrent.atomic.AtomicBoolean resuelto = new java.util.concurrent.atomic.AtomicBoolean(false);
        final java.util.concurrent.atomic.AtomicBoolean vencido = new java.util.concurrent.atomic.AtomicBoolean(false);
        BluetoothSocket socket = null;
        try {
            socket = dispositivo.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
            final BluetoothSocket vigilado = socket;
            Thread vigilante = new Thread(() -> {
                long limite = System.currentTimeMillis() + timeoutMs;
                try {
                    while (System.currentTimeMillis() < limite) {
                        if (resuelto.get()) return;
                        Thread.sleep(100);
                    }
                    if (!resuelto.get()) {
                        vencido.set(true);
                        vigilado.close(); // desbloquea el connect() colgado
                    }
                } catch (Exception ignored) {
                }
            }, "bt-vigilante");
            vigilante.setDaemon(true);
            vigilante.start();
            try {
                socket.connect();
            } finally {
                resuelto.set(true);
            }
            return socket;
        } catch (Exception e) {
            cerrarSilencioso(socket);
            if (vencido.get()) return null; // la impresora no responde: no se insiste
        }
        try { // segundo intento por el canal 1 (algunos modulos no publican bien el SDP)
            BluetoothSocket alterno = (BluetoothSocket) dispositivo.getClass()
                    .getMethod("createRfcommSocket", int.class).invoke(dispositivo, 1);
            if (alterno == null) return null;
            alterno.connect();
            return alterno;
        } catch (Exception e) {
            return null;
        }
    }

    private static void cerrarSilencioso(BluetoothSocket s) {
        if (s == null) return;
        try {
            s.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public boolean write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return false;
            }
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        if (r.write(out)) {
            return true;
        }
        return false;
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        addText("connectionFailed()");
        setState(STATE_DISCONNECTED);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.TOAST, "Fallo la conexion con impresora");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        VariablesGlobales.habilitadaimpresora = 0;
    }

    //helpers
    void addText(String s) {
        Message msg = mHandler.obtainMessage(msgTypes.MESSAGE_INFO);
        Bundle bundle = new Bundle();
        bundle.putString(msgTypes.INFO, "INFO: " + s);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    void addText(String msgType, int state) {
        // Give the new state to the Handler so the UI Activity can update
        msgTypes type;
        Message msg;
        Bundle bundle = new Bundle();
        if (msgType.equals(msgTypes.STATE)) {
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_STATE_CHANGE);// mHandler.obtainMessage(_Activity.MESSAGE_DEVICE_NAME);
        } else if (msgType.equals(msgTypes.DEVICE_NAME)) {
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_DEVICE_NAME);
        } else if (msgType.equals(msgTypes.INFO)) {
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_INFO);
        } else if (msgType.equals(msgTypes.TOAST)) {
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_TOAST);
        } else if (msgType.equals(msgTypes.READ)) {
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_READ);
        } else if (msgType.equals(msgTypes.WRITE)) {
            msg = mHandler.obtainMessage(msgTypes.MESSAGE_WRITE);
        } else {
            msg = new Message();
        }
        //msg = mHandler.obtainMessage(msgTypes.MESSAGE_STATE_CHANGE);// mHandler.obtainMessage(_Activity.MESSAGE_DEVICE_NAME);
        //mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        bundle.putInt(msgType, state);
        msg.setData(bundle);
        msg.arg1 = state;             //we can use arg1 or the bundle to provide additional information to the message handler
        mHandler.sendMessage(msg);
        Log.i(TAG, "addText: " + msgType + ", state=" + state);
    }

    void log(String msg) {
        if (D) Log.d(TAG, msg);
    }

    public class MyRunnable implements Runnable {
        private BluetoothSocket socket = null;

        public MyRunnable(BluetoothSocket btSocket) {
            this.socket = btSocket;
        }

        public void run() {

        }
        /*// usage
        Thread t = new Thread(new MyRunnable(parameter));
   		t.start();
    	*/
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                addText("createInsecureRfcommSocketToServiceRecord");
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
                //tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                btPrintFile.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (btPrintFile.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(msgTypes.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    // Solo el hilo vigente reporta la perdida: al reconectar, connected() cierra el socket
                    // del hilo anterior y su read() falla; sin esta guarda dejaba el estado en DESCONECTADO
                    // justo despues de conectar.
                    boolean vigente;
                    synchronized (btPrintFile.this) {
                        vigente = (mConnectedThread == this);
                    }
                    if (vigente) connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public boolean write(byte[] buffer) {
            // Sin MESSAGE_WRITE ni addText por escritura: cada uno posteaba al hilo UI y el handler
            // convertía el CPCL completo a String y lo acumulaba en un ArrayAdapter que nadie muestra
            // (memoria creciente y trabajo en el hilo de la pantalla en cada factura).
            // synchronized: dos llamadores (cola de impresión y rutas legadas) no intercalan bytes.
            synchronized (BLOQUEO_ESCRITURA) {
                try {
                    mmOutStream.write(buffer);
                    mmOutStream.flush();
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                    return false;
                }
            }
            return true;
        }

        public void cancel() {
            addText("cancel");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}
