package tthcc.rubiksolver.solver.faceletlabel;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tthcc.rubiksolver.solver.faceletlabel.camera.ProcessingThread;


public class PhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final String TAG = PhotoActivity.class.getSimpleName();
    public static final int MSG_FACE_DETECT_SUCESS = 6;
    private static final int MSG_DETECT_NEXT_FACE = 7;

    private static final int DefaultPreviewWidth = 1440;
    private static final int DefaultPreviewHeight = 1080;
    private SurfaceHolder surfaceHolder;
    private ProcessingThread processingThread;
    private Handler handler;
    private TextView sampleNumTextView;
    private Stopwatch stopwatch = new Stopwatch();

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
        this.createTextViewOverlay();
        this.surfaceHolder = ((SurfaceView)this.findViewById(R.id.camera_surface_view)).getHolder();
        this.surfaceHolder.addCallback(this);
        this.createHandler();
        this.processingThread = new ProcessingThread(ProcessingThread.class.getSimpleName(), this.surfaceHolder, this.handler, this.DefaultPreviewWidth, this.DefaultPreviewHeight);
        this.processingThread.start();
        this.stopwatch.start();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy - cleanup.");
        this.surfaceHolder.removeCallback(this);
        try {
            this.processingThread.performCleanup();
            Log.d(TAG, "calling quit!");
            //after cleanup, call quit
            this.processingThread.quit();
            Log.d(TAG, "now calling join!");
            //then wait for the thread to finish
            this.processingThread.join();
            Log.d(TAG, "after join!");
        } catch (Exception e) {
            Log.d(TAG, "onDestroy - exception when waiting for the processing thread to finish.", e);
        }
        Log.d(TAG, "calling super.onDestroy!");
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if(this.sampleNumTextView != null) {
            this.sampleNumTextView.setText("");
            this.sampleNumTextView.setVisibility(View.INVISIBLE);
        }
        if(this.sampleNumTextView != null) {
            this.sampleNumTextView.setText("");
            this.sampleNumTextView.setVisibility(View.INVISIBLE);
        }
        this.processingThread.performOpenCamera();
        this.processingThread.performStartCamera();
        //
        try {
            Thread.sleep(200);
        }
        catch(Exception exp){
        }
        this.processingThread.performDetectFace();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        this.processingThread.performCleanup();
    }

    /**
     *
     */
    private void createHandler() {
        this.handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case PhotoActivity.MSG_FACE_DETECT_SUCESS:
                        showFacePicture((Bitmap)msg.obj, 90);
                        break;
                    case PhotoActivity.MSG_DETECT_NEXT_FACE:
                        detectNextface();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    /**
     * @param bitmap
     * @param  rotateDegree
     */
    private void showFacePicture(Bitmap bitmap, int rotateDegree) {
        try {
            ImageView imageView = this.findViewById(R.id.facelet);
            // 旋转90度
            if (rotateDegree != 0) {
                Matrix m = new Matrix();
                m.setRotate(rotateDegree, (float) bitmap.getWidth(), (float) bitmap.getHeight());
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            }
            imageView.setImageBitmap(bitmap);
            this.stopwatch.startWatch();
        }
        catch(Exception exp) {
            Log.e(TAG, exp.getMessage(), exp);
        }
    }

    /**
     *
     */
    private void detectNextface() {
        ImageView imageView = this.findViewById(R.id.facelet);
        imageView.setImageBitmap(null);
        this.processingThread.performDetectFace();
    }

    /**
     *
     */
    private void createTextViewOverlay() {
        this.sampleNumTextView = new TextView(this.getApplicationContext());
        this.sampleNumTextView.setVisibility(View.INVISIBLE);
        this.sampleNumTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        this.sampleNumTextView.setTextColor(Color.WHITE);
        this.sampleNumTextView.setGravity(Gravity.CENTER_VERTICAL);
        this.sampleNumTextView.setTextSize(30);
        FrameLayout.LayoutParams stopwatchTvLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stopwatchTvLayoutParams.gravity = Gravity.TOP;
        stopwatchTvLayoutParams.topMargin = 300;
        this.addContentView(this.sampleNumTextView, stopwatchTvLayoutParams);
    }

    /**
     *
     */
    private class Stopwatch extends Thread {
        private boolean running = false;

        /**
         *
         */
        public void startWatch() {
            this.running = true;
        }

        @Override
        public void run() {
            try{
                while(true) {
                    if(!this.running) {
                        Thread.sleep(10);
                        continue;
                    }
                    Thread.sleep(2000);
                    this.running = false;
                    handler.sendEmptyMessage(PhotoActivity.MSG_DETECT_NEXT_FACE);
                }
            }
            catch(Exception exp) {
            }
        }
    }
}
