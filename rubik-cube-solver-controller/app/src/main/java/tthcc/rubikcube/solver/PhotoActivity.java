package tthcc.rubikcube.solver;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import tthcc.rubikcube.solver.camera.DetectResult;
import tthcc.rubikcube.solver.camera.ProcessingThread;



public class PhotoActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private final String TAG = PhotoActivity.class.getSimpleName();

    private String[] faceletStrings = new String[6];
    //index is RubikFacelet.Color
    private static final String[] FACENAME = new String[]{
            "F", "B", "U", "R", "L", "D"
    };

    //URLDFB is the kociemba's twophase algorithm's face sequence
    public static final int MSG_FACE_U = 0;
    public static final int MSG_FACE_R = 1;
    public static final int MSG_FACE_F = 2;
    public static final int MSG_FACE_D = 3;
    public static final int MSG_FACE_L = 4;
    public static final int MSG_FACE_B = 5;
    public static final int MSG_FACE_READY = 6;

    private static final int DefaultPreviewWidth = 1440;
    private static final int DefaultPreviewHeight = 1080;
    private SurfaceHolder surfaceHolder;
    private ProcessingThread processingThread;
    private Handler handler;
//    private int currentFace = 0;

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

//        this.findViewById(R.id.face_U).setOnClickListener(this.imageViewClickListener);
//        this.findViewById(R.id.face_F).setOnClickListener(this.imageViewClickListener);
//        this.findViewById(R.id.face_D).setOnClickListener(this.imageViewClickListener);
//        this.findViewById(R.id.face_B).setOnClickListener(this.imageViewClickListener);
//        this.findViewById(R.id.face_L).setOnClickListener(this.imageViewClickListener);
//        this.findViewById(R.id.face_R).setOnClickListener(this.imageViewClickListener);

        for(int i = 0; i< faceletStrings.length; i++) {
            this.faceletStrings[i] = "";
        }

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

//    private View.OnClickListener imageViewClickListener = new View.OnClickListener(){
//        @Override
//        public void onClick(View view) {
//            int messageId = 0;
//            switch(view.getId()) {
//                case R.id.face_U:
//                    messageId = PhotoActivity.MSG_FACE_U;
//                    break;
//                case R.id.face_F:
//                    messageId = PhotoActivity.MSG_FACE_F;
//                    break;
//                case R.id.face_D:
//                    messageId = PhotoActivity.MSG_FACE_D;
//                    break;
//                case R.id.face_B:
//                    messageId = PhotoActivity.MSG_FACE_B;
//                    break;
//                case R.id.face_L:
//                    messageId = PhotoActivity.MSG_FACE_L;
//                    break;
//                case R.id.face_R:
//                    messageId = PhotoActivity.MSG_FACE_R;
//                    break;
//            }
//            processingThread.performDetectFaceOrSolve(messageId);
//        }
//    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        this.processingThread.performOpenCamera();
        this.processingThread.performStartCamera();
        //
        try {
            Thread.sleep(200);
        }
        catch(Exception exp){
        }
//        this.currentFace = PhotoActivity.MSG_FACE_U;
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
//                ImageView imageView;
                //XXX
                Log.i(TAG, "msg.what=" + msg.what);
                switch(msg.what) {
                    case PhotoActivity.MSG_FACE_U:
                        ((ImageView)findViewById(R.id.face_U)).setImageBitmap(((DetectResult)msg.obj).getFaceBitmap());
//                        imageView = findViewById(R.id.face_U);
//                        handleDetectResult(msg, imageView);
                        break;
                    case PhotoActivity.MSG_FACE_F:
                        ((ImageView)findViewById(R.id.face_F)).setImageBitmap(((DetectResult)msg.obj).getFaceBitmap());
//                        imageView = findViewById(R.id.face_F);
//                        handleDetectResult(msg, imageView);
                        break;
                    case PhotoActivity.MSG_FACE_D:
                        ((ImageView)findViewById(R.id.face_D)).setImageBitmap(((DetectResult)msg.obj).getFaceBitmap());
//                        imageView = findViewById(R.id.face_D);
//                        handleDetectResult(msg, imageView);
                        break;
                    case PhotoActivity.MSG_FACE_B:
                        ((ImageView)findViewById(R.id.face_B)).setImageBitmap(((DetectResult)msg.obj).getFaceBitmap());
//                        imageView = findViewById(R.id.face_B);
//                        handleDetectResult(msg, imageView);
                        break;
                    case PhotoActivity.MSG_FACE_L:
                        ((ImageView)findViewById(R.id.face_L)).setImageBitmap(((DetectResult)msg.obj).getFaceBitmap());
//                        imageView = findViewById(R.id.face_L);
//                        handleDetectResult(msg, imageView);
                        break;
                    case PhotoActivity.MSG_FACE_R:
                        ((ImageView)findViewById(R.id.face_R)).setImageBitmap(((DetectResult)msg.obj).getFaceBitmap());
//                        imageView = findViewById(R.id.face_R);
//                        handleDetectResult(msg, imageView);
                        break;
//                    case PhotoActivity.MSG_FACE_READY:
//                        if(currentFace == PhotoActivity.MSG_FACE_U) {
//                            currentFace = PhotoActivity.MSG_FACE_F;
//                            processingThread.performDetectFaceOrSolve(currentFace);
//                        }
//                        else if(currentFace == PhotoActivity.MSG_FACE_F) {
//                            currentFace = PhotoActivity.MSG_FACE_D;
//                            processingThread.performDetectFaceOrSolve(currentFace);
//                        }
//                        else if(currentFace == PhotoActivity.MSG_FACE_D) {
//                            currentFace = PhotoActivity.MSG_FACE_B;
//                            processingThread.performDetectFaceOrSolve(currentFace);
//                        }
//                        else if(currentFace == PhotoActivity.MSG_FACE_B) {
//                            currentFace = PhotoActivity.MSG_FACE_L;
//                            processingThread.performDetectFaceOrSolve(currentFace);
//                        }
//                        else if(currentFace == PhotoActivity.MSG_FACE_L) {
//                            currentFace = PhotoActivity.MSG_FACE_R;
//                            processingThread.performDetectFaceOrSolve(currentFace);
//                        }
//                        else if(currentFace == PhotoActivity.MSG_FACE_R) {
//                            //TODO
////                            currentFace = -1;
////                            String moves = computeMoves();
////                            BluetoothUtil.getInstance().sendMessage(moves);
//                        }
//                        else if(currentFace == -1) {
//                            //Solved
//                            //TODO
//                            Log.i(TAG, "+++++++ SOLVED +++++++");
//                        }
//                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

//    /**
//     *
//     * @param msg
//     * @param imageView
//     */
//    private void handleDetectResult(Message msg, ImageView imageView) {
//        DetectResult detectResult = (DetectResult)msg.obj;
//        Bitmap photoBitmap = detectResult.getFaceBitmap();
//        // 旋转90度
//        Matrix m = new Matrix();
//        m.setRotate(90, (float) photoBitmap.getWidth(), (float) photoBitmap.getHeight());
//        photoBitmap = Bitmap.createBitmap(photoBitmap, 0, 0, photoBitmap.getWidth(), photoBitmap.getHeight(), m, true);
//        imageView.setImageBitmap(photoBitmap);
//
//        RubikFacelet[][] facelets = detectResult.getFacelets();
//        RubikFacelet[][] actualFacelets = new RubikFacelet[3][3];
//
//        actualFacelets[0][0] = facelets[2][0];
//        actualFacelets[0][1] = facelets[1][0];
//        actualFacelets[0][2] = facelets[0][0];
//
//        actualFacelets[1][0] = facelets[2][1];
//        actualFacelets[1][1] = facelets[1][1];
//        actualFacelets[1][2] = facelets[0][1];
//
//        actualFacelets[2][0] = facelets[2][2];
//        actualFacelets[2][1] = facelets[1][2];
//        actualFacelets[2][2] = facelets[0][2];
//
//        int faceIndex = msg.what;
//        for(int i=0; i<3; i++) {
//            for(int j=0; j<3; j++) {
//                this.faceletStrings[faceIndex] += FACENAME[actualFacelets[i][j].color];
//            }
//        }
//
//        Log.i(TAG,">>>> " + actualFacelets[0][0].color + " " + actualFacelets[0][1].color + " " + actualFacelets[0][2].color);
//        Log.i(TAG,">>>> " + actualFacelets[1][0].color + " " + actualFacelets[1][1].color + " " + actualFacelets[1][2].color);
//        Log.i(TAG,">>>> " + actualFacelets[2][0].color + " " + actualFacelets[2][1].color + " " + actualFacelets[2][2].color);
//
//        switch(msg.what) {
//            case PhotoActivity.MSG_FACE_U:
//                BluetoothUtil.getInstance().sendMessage("x");
//                break;
//            case PhotoActivity.MSG_FACE_F:
//                BluetoothUtil.getInstance().sendMessage("x");
//                break;
//            case PhotoActivity.MSG_FACE_D:
//                BluetoothUtil.getInstance().sendMessage("x");
//                break;
//            case PhotoActivity.MSG_FACE_B:
//                BluetoothUtil.getInstance().sendMessage("y'x");
//                break;
//            case PhotoActivity.MSG_FACE_L:
//                BluetoothUtil.getInstance().sendMessage("x2");
//                break;
//            case PhotoActivity.MSG_FACE_R:
//                BluetoothUtil.getInstance().sendMessage("xy'x");
//                break;
//        }
//    }
//
//    /**
//     *
//     * @return
//     */
//    private String computeMoves() {
//        String faceletString = "";
//        for(String str : faceletStrings) {
//            faceletString += str;
//        }
//        int code = Tools.verify(faceletString);
//        //XXX
//        Log.i(TAG, "verify code=" + code);
//        if(code == 0) {
//            String moves = Search.solution(faceletString, 21, 5, false);
//            //XXX
//            Log.i(TAG, "moves=" + moves);
//            return moves;
//        }
//        return null;
//    }
}
