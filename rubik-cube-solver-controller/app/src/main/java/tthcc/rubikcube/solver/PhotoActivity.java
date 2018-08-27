package tthcc.rubikcube.solver;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import tthcc.rubikcube.solver.camera.CameraPreview;


public class PhotoActivity extends AppCompatActivity {
    private String TAG = PhotoActivity.class.getSimpleName();
    private Camera mCamera = null;
    private CameraPreview mPreview = null;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Display display = getWindowManager().getDefaultDisplay();
        mCamera = this.getCameraInstance();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        LinearLayout linearLayout = this.findViewById(R.id.linear_layout);
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        int width = display.getHeight() * 3 / 4 * previewSize.height / previewSize.width;
        layoutParams.width = width;
        linearLayout.setLayoutParams(layoutParams);
        mPreview = new CameraPreview(this, mCamera);

        FrameLayout preview = this.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        this.findViewById(R.id.face_U).setOnClickListener(imageViewClick);
        this.findViewById(R.id.face_F).setOnClickListener(imageViewClick);
        this.findViewById(R.id.face_D).setOnClickListener(imageViewClick);
        this.findViewById(R.id.face_B).setOnClickListener(imageViewClick);
        this.findViewById(R.id.face_L).setOnClickListener(imageViewClick);
        this.findViewById(R.id.face_R).setOnClickListener(imageViewClick);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.releaseCameraAndPreview();
    }

    private Camera getCameraInstance(){
        this.releaseCameraAndPreview();
        Camera mCamera = null;
        try{
            mCamera = Camera.open();
            Camera.Parameters param = mCamera.getParameters();
            param.setPreviewSize(1440, 1080);
            mCamera.setParameters(param);
            mCamera.setDisplayOrientation(90);
        }catch(Exception exp){
            Log.e(TAG, exp.getMessage());
            exp.printStackTrace();
        }
        return mCamera;
    }

    private void releaseCameraAndPreview(){
        if(mPreview != null){
            mPreview.setCamera(null);
        }
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    private View.OnClickListener imageViewClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            try {
                imageView = (ImageView)v;
                mCamera.takePicture(null, null, mPicture);
            }
            catch(Throwable th){
                Log.e(TAG, th.getMessage());
                th.printStackTrace();
            }
        }
    };

    /**
     *
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = createImageFile();
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }
            try {
                //旋转90度
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix m = new Matrix();
                m.setRotate(90,(float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
                Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                FileOutputStream fos = new FileOutputStream(pictureFile);
                BufferedOutputStream buffer = new BufferedOutputStream(fos);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, buffer);
                fos.close();
                Uri uri = Uri.fromFile(pictureFile);
                imageView.setImageURI(uri);
                mCamera.startPreview();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }


    };
    private File createImageFile() {
        // Create an image file name
        String imageFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (Exception e) {
            Log.d("PhotoActivity", "Could not create image file.", e);
        }
        return image;
    }
}
