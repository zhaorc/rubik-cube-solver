package tthcc.rubiksolver.solver.faceletlabel.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import tthcc.rubiksolver.solver.faceletlabel.camera.ProcessingThread;


public class BluetoothUtil {
    private String TAG = BluetoothUtil.class.getSimpleName();
    private static BluetoothUtil instance;
    private BluetoothSocket socket;
    private ProcessingThread processingThread;
    private BluetoothUtil() {
    }

    /**
     *
     * @return
     */
    public static BluetoothUtil getInstance() {
        if(instance == null) {
            instance = new BluetoothUtil();
        }
        return instance;
    }

    /**
     *
     * @param processingThread
     */
    public void setProcessingThread(ProcessingThread processingThread) {
        this.processingThread = processingThread;
    }

//    /**
//     *
//     * @param photoActivityHandler
//     */
//    public void setPhotoActivityHandler(Handler photoActivityHandler) {
//        this.photoActivityHandler = photoActivityHandler;
//    }

    /**
     *
     * @return
     */
    public boolean isBluetoothReady() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    /**
     *
     */
    public int connectBluetooth() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice hc06 = null;
        for (BluetoothDevice device : bondedDevices) {
            if (device.getName().equals("HC-06")) {
                hc06 = device;
                break;
            }
        }
        if(hc06 == null) {
            return -1; //未配对
        }
        try {
            final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            this.socket = hc06.createRfcommSocketToServiceRecord(uuid);
            if(this.socket != null) {
                this.socket.connect();
            }
            this.startReaderThread();
            return 0; //建立连接成功
        }
        catch(Exception exp) {
            Log.i(TAG, exp.getMessage(), exp);
            return -2; //建立连接失败
        }
    }

    /**
     *
     */
    public void disconnectBluetooth() {
        if(this.socket.isConnected()) {
            try {
                this.socket.close();
            }
            catch(Exception exp) {
                Log.i(TAG, exp.getMessage(), exp);
            }
        }
    }

    /**
     *
     * @param message
     */
    public void sendMessage(String message) {
        int size = message.length();
        String data = String.format("%04d%s", size, message);
        //XXX
        Log.i(TAG, "sendMessage=" + data);
        try {
            OutputStream outs = this.socket.getOutputStream();
            outs.write((data).getBytes());
            outs.flush();
        }
        catch(Exception exp) {
            Log.i(TAG, exp.getMessage(), exp);
        }
    }

    /**
     *
     */
    private void startReaderThread() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    InputStream ins = socket.getInputStream();
                    byte[] data = new byte[32];
                    int iread = 0;
                    while(true) {
                        if(!socket.isConnected() || ins == null) {
                            break;
                        }
                        if(ins.available() >= 4) {
                            iread = ins.read(data, 0, data.length);
                            String response = new String(data, 0, iread);
                            //XXX
                            Log.i(TAG, "response=" + response);
                            if(response.equals("DONE")) {
                                processingThread.performDetectFace();
                            }
                        }
                        Thread.sleep(10);
                    }
                }
                catch(Exception exp) {
                    Log.e(TAG, exp.getMessage(), exp);
                }
            }
        }).start();
    }
}
