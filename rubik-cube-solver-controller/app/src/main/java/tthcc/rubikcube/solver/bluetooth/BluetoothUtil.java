package tthcc.rubikcube.solver.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import tthcc.rubikcube.solver.PhotoActivity;

public class BluetoothUtil {
    private String TAG = BluetoothUtil.class.getSimpleName();
    private static BluetoothUtil instance;
    private BluetoothSocket socket;
    private Handler photoActivityHandler;
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
     * @param photoActivityHandler
     */
    public void setPhotoActivityHandler(Handler photoActivityHandler) {
        this.photoActivityHandler = photoActivityHandler;
    }

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
        //XXX
        Log.i(TAG, "sendMessage=" + message);
        OutputStream outs = null;
        try {
            outs = this.socket.getOutputStream();
            outs.write((message + "\r\n").getBytes());
            outs.flush();
        }
        catch(Exception exp) {
            Log.i(TAG, exp.getMessage(), exp);
        }
        finally {
            try {
                if(outs != null) {
                    outs.close();
                }
            }
            catch(Exception exp) {

            }
        }
    }

    /**
     *
     */
    private void startReaderThread() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response;
                    while(true) {
                        response = reader.readLine();
                        if(response != null) {
                            //XXX
                            Log.i(TAG, "response=" + response);
                            if(response.equals("DONE\r\n")) {
                                photoActivityHandler.sendEmptyMessage(PhotoActivity.MSG_FACE_READY);
                            }
                        }
                        Thread.sleep(10);
                    }
                }
                catch(Exception exp) {
                    Log.e(TAG, exp.getMessage(), exp);
                }
                finally {
                    try {
                        if(reader != null) {
                            reader.close();
                        }
                    }
                    catch(Exception exp) {
                    }
                }
            }
        }).start();
    }
}
