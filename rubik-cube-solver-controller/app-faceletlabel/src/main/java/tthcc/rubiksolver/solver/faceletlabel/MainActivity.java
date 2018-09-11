package tthcc.rubiksolver.solver.faceletlabel;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQUEST_CODE = 0x118;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startButton = this.findViewById(R.id.button_caputure);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), PhotoActivity.class));
            }
        });

        if (!this.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                !this.isPermissionGranted(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     *
     * @param permissionId
     * @return
     */
    private boolean isPermissionGranted(String permissionId) {
        return ContextCompat.checkSelfPermission(getBaseContext(), permissionId) == PackageManager.PERMISSION_GRANTED;
    }

//    /**
//     *
//     * @param msg
//     */
//    private void performShowToast(String msg) {
//        Toast toast = Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//    }
}
