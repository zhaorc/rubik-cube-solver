package tthcc.rubikcube.solver;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import tthcc.rubikcube.solver.camera.ProcessingThread;



public class PhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final String TAG = PhotoActivity.class.getSimpleName();

    private String[] faceletStrings = new String[6];

    //URLDFB is the kociemba's twophase algorithm's face sequence
    public static final int MSG_FACE_U = 0;
    public static final int MSG_FACE_R = 1;
    public static final int MSG_FACE_F = 2;
    public static final int MSG_FACE_D = 3;
    public static final int MSG_FACE_L = 4;
    public static final int MSG_FACE_B = 5;
    public static final int MSG_FACE_DETECT_FAIL = 6;
    public static final int MSG_FACE_DETECT_SUCESS = 7;
    public static final int MSG_SHOW_STOPWATCH = 8;
    public static final int MSG_STOP_WATCH = 9;

    private static final int DefaultPreviewWidth = 1440;
    private static final int DefaultPreviewHeight = 1080;
    private SurfaceHolder surfaceHolder;
    private ProcessingThread processingThread;
    private Handler handler;
    private TextView movesTextView;
    private TextView stopwatchTextView;
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

        for(int i = 0; i< faceletStrings.length; i++) {
            this.faceletStrings[i] = "";
        }
        this.createTextViewOverlay();

        this.surfaceHolder = ((SurfaceView)this.findViewById(R.id.camera_surface_view)).getHolder();
        this.surfaceHolder.addCallback(this);
        this.createHandler();
        this.processingThread = new ProcessingThread(ProcessingThread.class.getSimpleName(), this.surfaceHolder, this.handler, this.DefaultPreviewWidth, this.DefaultPreviewHeight);
        this.processingThread.start();
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
        if(this.movesTextView != null) {
            this.movesTextView.setText("");
            this.movesTextView.setVisibility(View.INVISIBLE);
        }
        if(this.stopwatchTextView != null) {
            this.stopwatchTextView.setText("");
            this.stopwatchTextView.setVisibility(View.INVISIBLE);
        }
        this.processingThread.performOpenCamera();
        this.processingThread.performStartCamera();
        //
        try {
            Thread.sleep(200);
        }
        catch(Exception exp){
        }
        this.processingThread.performDetectFaceOrSolve();
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
                    case PhotoActivity.MSG_FACE_U:
                        showFacePicture((ImageView)findViewById(R.id.face_U), (Bitmap)msg.obj, 90);//(90 + msg.arg1));
                        break;
                    case PhotoActivity.MSG_FACE_F:
                        showFacePicture((ImageView)findViewById(R.id.face_F), (Bitmap)msg.obj, 90);//(90 + msg.arg1));
                        break;
                    case PhotoActivity.MSG_FACE_D:
                        showFacePicture((ImageView)findViewById(R.id.face_D), (Bitmap)msg.obj, 90);//(90 + msg.arg1));
                        break;
                    case PhotoActivity.MSG_FACE_B:
                        showFacePicture((ImageView)findViewById(R.id.face_B), (Bitmap)msg.obj, -90);//(-90 + msg.arg1));
                        break;
                    case PhotoActivity.MSG_FACE_L:
                        showFacePicture((ImageView)findViewById(R.id.face_L), (Bitmap)msg.obj, 0);//msg.arg1);
                        break;
                    case PhotoActivity.MSG_FACE_R:
                        showFacePicture((ImageView)findViewById(R.id.face_R), (Bitmap)msg.obj, 0);//msg.arg1);
                        break;
                    case PhotoActivity.MSG_FACE_DETECT_FAIL:
                        showMessageOnScreen((String)msg.obj);
                        break;
                    case PhotoActivity.MSG_FACE_DETECT_SUCESS:
                        showMovesOnScreenAndStartStopwatch((String)msg.obj);
                        break;
                    case PhotoActivity.MSG_SHOW_STOPWATCH:
                        showStopwatch((String)msg.obj);
                        break;
                    case PhotoActivity.MSG_STOP_WATCH:
                        stopwatch.stopWatch();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    /**
     * @param imageView
     * @param bitmap
     * @param  rotateDegree
     */
    private void showFacePicture(ImageView imageView, Bitmap bitmap, int rotateDegree) {
        // 旋转90度
        if(rotateDegree != 0) {
            Matrix m = new Matrix();
            m.setRotate(rotateDegree, (float) bitmap.getWidth(), (float) bitmap.getHeight());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        }
        imageView.setImageBitmap(bitmap);
    }

    /**
     *
     */
    private void createTextViewOverlay() {
        //用来显示moves的TextView
        this.movesTextView = new TextView(this.getApplicationContext());
        this.movesTextView.setVisibility(View.INVISIBLE);
        this.movesTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        this.movesTextView.setTextColor(Color.WHITE);
        this.movesTextView.setTextSize(20);
        FrameLayout.LayoutParams movesTvLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        movesTvLayoutParams.gravity = Gravity.TOP;
        movesTvLayoutParams.topMargin = 50;
        movesTvLayoutParams.leftMargin = 50;
        movesTvLayoutParams.rightMargin = 50;
        this.addContentView(this.movesTextView, movesTvLayoutParams);

        this.stopwatchTextView = new TextView(this.getApplicationContext());
        this.stopwatchTextView.setVisibility(View.INVISIBLE);
        this.stopwatchTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        this.stopwatchTextView.setTextColor(Color.WHITE);
        this.stopwatchTextView.setGravity(Gravity.CENTER_VERTICAL);
        this.stopwatchTextView.setTextSize(30);
        FrameLayout.LayoutParams stopwatchTvLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stopwatchTvLayoutParams.gravity = Gravity.TOP;
        stopwatchTvLayoutParams.topMargin = 300;
        this.addContentView(this.stopwatchTextView, stopwatchTvLayoutParams);
    }

    /**
     *
     */
    private void showMessageOnScreen(String message) {
        this.movesTextView.setText(message);
        this.movesTextView.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    private void showMovesOnScreenAndStartStopwatch(String moves) {
        this.movesTextView.setText(moves);
        this.movesTextView.setVisibility(View.VISIBLE);
        //start Stopwatch
        this.stopwatchTextView.setText("00:00.00");
        this.stopwatchTextView.setVisibility(View.VISIBLE);
        this.stopwatch.startWatch();
    }

//    /**
//     *
//     */
//    private void startStopwatch() {
//    }

    /**
     *
     * @param timeStr
     */
    private void showStopwatch(String timeStr) {
        this.stopwatchTextView.setText(timeStr);
    }

    /**
     *
     */
    private class Stopwatch extends Thread {
        private long startTimestamp = 0;
        private boolean running = false;

        /**
         *
         */
        public void startWatch() {
            this.startTimestamp = System.currentTimeMillis();
            this.running = true;
            this.start();
        }

        /**
         *
         */
        public void stopWatch() {
            this.running = false;

        }
        @Override
        public void run() {
            while(this.running) {
                try {
                    Thread.sleep(10);
                    long timepassed = System.currentTimeMillis() - startTimestamp;
                    String timeStr = String.format("%02d:%02d.%02d", (timepassed / 60000), (timepassed % 60000) / 1000, ((timepassed % 60000) % 1000) / 10);
                    Message message = handler.obtainMessage(PhotoActivity.MSG_SHOW_STOPWATCH, timeStr);
                    message.sendToTarget();
                }
                catch(Exception exp) {
                }
            }
        }
    }
}
