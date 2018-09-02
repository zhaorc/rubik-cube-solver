package tthcc.rubikcube.solver;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import tthcc.rubikcube.solver.bluetooth.BluetoothUtil;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 0x118;
    private static final int MSG_BLUETOOTH_THREAD_READY = 0x119;
    private static final int MSG_REQUEST_BLUETOOTH = 0x120;
    private static final int MSG_CONNECT_BLUETOOTH = 0x121;
    private static final int MSG_BLUETOOTH_CONNECTED = 0x122;
    private static final int MSG_BLUETOOTH_NOTBONDED = 0x123;
    private static final int MSG_BLUETOOTH_NOTCONNECTED = 0x124;
    private Handler frontendHandler;
    private BluetoothConnectionHandlerThread bluetoothConnectionHandlerThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = this.findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), PhotoActivity.class));
            }
        });

        if (!this.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                !this.isPermissionGranted(Manifest.permission.CAMERA) ||
                !this.isPermissionGranted(Manifest.permission.BLUETOOTH)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH},
                    PERMISSION_REQUEST_CODE);
        }

        this.frontendHandler = this.createHandler();
        this.bluetoothConnectionHandlerThread = new BluetoothConnectionHandlerThread("BluetoothConnectionHandlerThread", frontendHandler);
        this.bluetoothConnectionHandlerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothUtil.getInstance().disconnectBluetooth();
    }

    /**
     *
     * @param permissionId
     * @return
     */
    private boolean isPermissionGranted(String permissionId) {
        return ContextCompat.checkSelfPermission(getBaseContext(), permissionId) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case MainActivity.MSG_REQUEST_BLUETOOTH:
                if(resultCode == Activity.RESULT_OK) {
                    this.bluetoothConnectionHandlerThread.performConnectBluetooth();
                }
                else {
                    Toast toast = Toast.makeText(this.getApplicationContext(), "蓝牙已被禁用，请手动开启", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
        }
    }

    /**
     *
     * @return
     */
    private Handler createHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MainActivity.MSG_BLUETOOTH_THREAD_READY:
                        performConnectBluetooth();
                        break;
                    case MainActivity.MSG_REQUEST_BLUETOOTH:
                        performRequestBluetooth();
                        break;
                    case MainActivity.MSG_BLUETOOTH_CONNECTED:
                        performEnableStartButton();
                        break;
                    case MainActivity.MSG_BLUETOOTH_NOTBONDED:
                        performShowToast("蓝牙设备未配对，请建立与HC-06的配对");
                        break;
                    case MainActivity.MSG_BLUETOOTH_NOTCONNECTED:
                        performShowToast("与HC-06建立连接失败");
                }
            }
        };
    }

    private void performConnectBluetooth() {
        this.bluetoothConnectionHandlerThread.performConnectBluetooth();
    }

    /**
     *
     */
    private void performRequestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, MainActivity.MSG_REQUEST_BLUETOOTH);
    }

    /**
     *
     */
    private void performEnableStartButton() {
        Button startButton = this.findViewById(R.id.button_start);
        startButton.setEnabled(true);
        //set background:#4db65f
        startButton.setBackgroundColor(Color.rgb(0x4d, 0xb6,0x5f));
    }

    /**
     *
     * @param msg
     */
    private void performShowToast(String msg) {
        Toast toast = Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     *
     */
    final class BluetoothConnectionHandlerThread extends HandlerThread {
        private Handler frontendHandler;
        private Handler backgroundHandler;
        BluetoothConnectionHandlerThread(String name, Handler frontendHandler) {
            super(name);
            this.frontendHandler = frontendHandler;
        }

        /**
         *
         */
        public void performConnectBluetooth() {
            this.backgroundHandler.sendEmptyMessage(MainActivity.MSG_CONNECT_BLUETOOTH);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            this.backgroundHandler = new Handler(BluetoothConnectionHandlerThread.this.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch(msg.what) {
                        case MainActivity.MSG_CONNECT_BLUETOOTH:
                            connectBluetooth();
                            break;
                    }
                }
            };
            this.frontendHandler.sendEmptyMessage(MainActivity.MSG_BLUETOOTH_THREAD_READY);
        }

        /**
         *
         */
        private void connectBluetooth() {
            BluetoothUtil bluetoothUtil = BluetoothUtil.getInstance();
            if(!bluetoothUtil.isBluetoothReady()) {
                this.frontendHandler.sendEmptyMessage(MainActivity.MSG_REQUEST_BLUETOOTH);
                return;
            }
            Log.i(TAG, "bluetooth is enabled");
            int state = bluetoothUtil.connectBluetooth();
            if(state == 0) {
                //成功
                this.frontendHandler.sendEmptyMessage(MainActivity.MSG_BLUETOOTH_CONNECTED);
            }
            else if(state == -1) {
                //未配对　
                this.frontendHandler.sendEmptyMessage(MainActivity.MSG_BLUETOOTH_NOTBONDED);
            }
            else if(state == -2) {
                //建立连接失败
                this.frontendHandler.sendEmptyMessage(MainActivity.MSG_BLUETOOTH_NOTCONNECTED);
            }
        }
    }
}
