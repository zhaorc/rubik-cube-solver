package tthcc.rubikcube.solver;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.catalinjurjiu.rubikdetector.model.RubikFacelet;

import tthcc.rubikcube.solver.camera.DetectResult;
import tthcc.rubikcube.solver.camera.ProcessingThread;


public class PhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final String TAG = PhotoActivity.class.getSimpleName();
    private static final int MSG_FACE_U = 1;
    private static final int MSG_FACE_F = 2;
    private static final int MSG_FACE_D = 3;
    private static final int MSG_FACE_B = 4;
    private static final int MSG_FACE_L = 5;
    private static final int MSG_FACE_R = 6;
    private static final int DefaultPreviewWidth = 1440;
    private static final int DefaultPreviewHeight = 1080;
    private SurfaceHolder surfaceHolder;
    private ProcessingThread processingThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_photo);

        Display display = getWindowManager().getDefaultDisplay();
        LinearLayout linearLayout = this.findViewById(R.id.linear_layout);
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        int width = display.getHeight() * 3 / 4 * this.DefaultPreviewHeight / this.DefaultPreviewWidth;
        layoutParams.width = width;
        linearLayout.setLayoutParams(layoutParams);

        this.createHandler();

        this.findViewById(R.id.face_U).setOnClickListener(this.imageViewClickListener);
        this.findViewById(R.id.face_F).setOnClickListener(this.imageViewClickListener);
        this.findViewById(R.id.face_D).setOnClickListener(this.imageViewClickListener);
        this.findViewById(R.id.face_B).setOnClickListener(this.imageViewClickListener);
        this.findViewById(R.id.face_L).setOnClickListener(this.imageViewClickListener);
        this.findViewById(R.id.face_R).setOnClickListener(this.imageViewClickListener);

        this.surfaceHolder = ((SurfaceView)this.findViewById(R.id.camera_surface_view)).getHolder();
        this.surfaceHolder.addCallback(this);
        this.processingThread = new ProcessingThread(ProcessingThread.class.getSimpleName(), this.surfaceHolder, this.handler, this.DefaultPreviewWidth, this.DefaultPreviewHeight);
        this.processingThread.start();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy - cleanup.");
        surfaceHolder.removeCallback(this);
        try {
            processingThread.performCleanup();
            Log.d(TAG, "calling quit!");
            //after cleanup, call quit
            processingThread.quit();
            Log.d(TAG, "now calling join!");
            //then wait for the thread to finish
            processingThread.join();
            Log.d(TAG, "after join!");
        } catch (Exception e) {
            Log.d(TAG, "onDestroy - exception when waiting for the processing thread to finish.", e);
        }
        Log.d(TAG, "calling super.onDestroy!");
        super.onDestroy();
    }

    private View.OnClickListener imageViewClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            int messageId = 0;
            switch(view.getId()) {
                case R.id.face_U:
                    messageId = PhotoActivity.MSG_FACE_U;
                    break;
                case R.id.face_F:
                    messageId = PhotoActivity.MSG_FACE_F;
                    break;
                case R.id.face_D:
                    messageId = PhotoActivity.MSG_FACE_D;
                    break;
                case R.id.face_B:
                    messageId = PhotoActivity.MSG_FACE_B;
                    break;
                case R.id.face_L:
                    messageId = PhotoActivity.MSG_FACE_L;
                    break;
                case R.id.face_R:
                    messageId = PhotoActivity.MSG_FACE_R;
                    break;
            }
            processingThread.performDetectFace(messageId);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        processingThread.performOpenCamera();
        processingThread.performStartCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        processingThread.performCleanup();
    }

    private void createHandler() {
        this.handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                ImageView imageView;
                switch(msg.what) {
                    case PhotoActivity.MSG_FACE_U:
                        imageView = findViewById(R.id.face_U);
                        handleDetectResult((DetectResult)(msg.obj), imageView);
                        break;
                    case PhotoActivity.MSG_FACE_F:
                        imageView = findViewById(R.id.face_F);
                        handleDetectResult((DetectResult)(msg.obj), imageView);
                        break;
                    case PhotoActivity.MSG_FACE_D:
                        imageView = findViewById(R.id.face_D);
                        handleDetectResult((DetectResult)(msg.obj), imageView);
                        break;
                    case PhotoActivity.MSG_FACE_B:
                        imageView = findViewById(R.id.face_B);
                        handleDetectResult((DetectResult)(msg.obj), imageView);
                        break;
                    case PhotoActivity.MSG_FACE_L:
                        imageView = findViewById(R.id.face_L);
                        handleDetectResult((DetectResult)(msg.obj), imageView);
                        break;
                    case PhotoActivity.MSG_FACE_R:
                        imageView = findViewById(R.id.face_R);
                        handleDetectResult((DetectResult)(msg.obj), imageView);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    /**
     *
     * @param detectResult
     * @param imageView
     */
    private void handleDetectResult(DetectResult detectResult, ImageView imageView) {
        Bitmap photoBitmap = detectResult.getFaceBitmap();
        // 旋转90度
        Matrix m = new Matrix();
        m.setRotate(90, (float) photoBitmap.getWidth(), (float) photoBitmap.getHeight());
        photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), m, true);
        imageView.setImageBitmap(photoBitmap);

        RubikFacelet[][] facelets = detectResult.getFacelets();
        RubikFacelet[][] actualFacelets = new RubikFacelet[3][3];

        actualFacelets[0][0] = facelets[2][0];
        actualFacelets[0][1] = facelets[1][0];
        actualFacelets[0][2] = facelets[0][0];

        actualFacelets[1][0] = facelets[2][1];
        actualFacelets[1][1] = facelets[1][1];
        actualFacelets[1][2] = facelets[0][1];

        actualFacelets[2][0] = facelets[2][2];
        actualFacelets[2][1] = facelets[1][2];
        actualFacelets[2][2] = facelets[0][2];

//        Log.i(TAG,">>>> " + actualFacelets[0][0].color + " " + actualFacelets[0][1].color + " " + actualFacelets[0][2].color);
//        Log.i(TAG,">>>> " + actualFacelets[1][0].color + " " + actualFacelets[1][1].color + " " + actualFacelets[1][2].color);
//        Log.i(TAG,">>>> " + actualFacelets[2][0].color + " " + actualFacelets[2][1].color + " " + actualFacelets[2][2].color);
    }
}
