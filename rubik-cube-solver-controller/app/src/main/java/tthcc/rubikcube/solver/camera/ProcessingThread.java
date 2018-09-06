package tthcc.rubikcube.solver.camera;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.catalinjurjiu.rubikdetector.RubikDetector;
import com.catalinjurjiu.rubikdetector.RubikDetectorUtils;
import com.catalinjurjiu.rubikdetector.config.DrawConfig;
import com.catalinjurjiu.rubikdetector.model.RubikFacelet;

import org.kociemba.twophase.Search;
import org.kociemba.twophase.Tools;

import java.nio.ByteBuffer;

import tthcc.rubikcube.solver.PhotoActivity;
import tthcc.rubikcube.solver.bluetooth.BluetoothUtil;

public class ProcessingThread extends HandlerThread implements Camera.PreviewCallback {

    private String TAG = ProcessingThread.class.getSimpleName();

    private static final int PreviewFormat = ImageFormat.NV21;

    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_START_CAMERA = 2;
    private static final int MSG_CLEANUP = 3;
    private static final int MSG_DETECT_FACE_OR_SOLVE = 4;

    private int cameraPreviewWidth;
    private int cameraPreviewHeight;
    private int currentConfigPreviewFrameByteCount = -1;

    private SurfaceHolder surfaceHolder;
    private Handler backgroundHandler;
    private Handler frontendHandler;
    private Camera camera;

    private RubikDetector rubikDetector;

    private final Object cleanupLock = new Object();
    private static final int[] FaceSequance = new int[] {
            PhotoActivity.MSG_FACE_U,
            PhotoActivity.MSG_FACE_F,
            PhotoActivity.MSG_FACE_D,
            PhotoActivity.MSG_FACE_B,
            PhotoActivity.MSG_FACE_L,
            PhotoActivity.MSG_FACE_R
    };
    private static final String[] RubikTurnfaceSequance = new String[] {
            "",
            "x2",
            "x",
            "x",
            "yx",
            "x",
            "xy'x",
    };
    private int currentFace = -1;
    private boolean faceDetacted = true;
    private boolean solveInProcessing = false;

    //index is RubikFacelet.Color
    private static final String[] Facename = new String[]{
            "F", "B", "U", "R", "L", "D"
    };
    private String[] facelets = new String[] {
            "","","","","",""
    };

    /**
     *
     * @param name
     * @param surfaceHolder
     */
    public ProcessingThread(String name, SurfaceHolder surfaceHolder, Handler frontendHandler, int cameraPreviewWidth, int cameraPreviewHeight) {
        super(name);
        this.surfaceHolder = surfaceHolder;
        this.frontendHandler = frontendHandler;
        this.cameraPreviewWidth = cameraPreviewWidth;
        this.cameraPreviewHeight = cameraPreviewHeight;
        BluetoothUtil.getInstance().setProcessingThread(this);
    }

    /**
     *
     */
    public void performOpenCamera() {
        this.backgroundHandler.sendEmptyMessage(ProcessingThread.MSG_OPEN_CAMERA);
    }

    /**
     *
     */
    public void performStartCamera() {
        this.backgroundHandler.sendEmptyMessage(ProcessingThread.MSG_START_CAMERA);
    }

    /**
     *
     */
    public void performCleanup() {
        this.backgroundHandler.sendEmptyMessage(ProcessingThread.MSG_CLEANUP);
    }

    public void performDetectFaceOrSolve() {
        this.backgroundHandler.sendEmptyMessage(ProcessingThread.MSG_DETECT_FACE_OR_SOLVE);
    }

    @Override
    protected void onLooperPrepared(){
        super.onLooperPrepared();
        this.backgroundHandler = new Handler(ProcessingThread.this.getLooper()){
            @Override
            public void handleMessage(Message msg){
                switch(msg.what){
                    case MSG_OPEN_CAMERA:
                        openCamera();
                        break;
                    case MSG_START_CAMERA:
                        startCamera();
                        break;
                    case MSG_CLEANUP:
                        cleanup();
                        break;
                    case MSG_DETECT_FACE_OR_SOLVE:
                        detectFaceOrSolve();
                        break;
                }
            }
        };
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) {
            Log.w(TAG, "Received null data array, or a data array of wrong size, from camera. Do nothing.");
            return;
        }

        if (data.length != this.currentConfigPreviewFrameByteCount) {
            Log.w(TAG, "Received data array of wrong size from camera. Do nothing.");
            return;
        }

        Log.d(TAG, "onPreviewFrame, data buffer size: " + data.length);
        if (this.rubikDetector.isActive()) {
            detectRubikFace(data);
        }
        camera.addCallbackBuffer(data);
    }

    /**
     *
     */
    private void openCamera(){
        int cameraId = this.getMainCameraId();
        if(cameraId == -1) {
            return;
        }
        try{
            this.camera = Camera.open();
        }
        catch(Exception exp) {
            Log.e(TAG, "Cannot open camera", exp);
        }
        if(this.camera == null) {
            return;
        }
        Camera.Parameters cameraParameters = this.camera.getParameters();
        //FocusMode
        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //Format
        cameraParameters.setPreviewFormat(ProcessingThread.PreviewFormat);
        cameraParameters.setPreviewSize(this.cameraPreviewWidth, this.cameraPreviewHeight);
        this.camera.setParameters(cameraParameters);
        //方向
        this.camera.setDisplayOrientation(90);

        try {
            this.camera.setPreviewDisplay(this.surfaceHolder);
        }
        catch(Exception exp) {
            Log.e(TAG, "Cannot set SurfaceTexture", exp);
        }

        //RubikDetector
        this.rubikDetector = new RubikDetector.Builder()
                .debuggable(false)
                .drawConfig(DrawConfig.FilledCircles())
                .inputFrameSize(this.cameraPreviewWidth, this.cameraPreviewHeight)
                .inputFrameFormat(RubikDetectorUtils.convertAndroidImageFormat(ProcessingThread.PreviewFormat))
                .build();
        this.allocateAndSetBuffers();
    }

    /**
     *
     */
    private void startCamera() {
        this.camera.startPreview();
    }

    /**
     *
     */
    private void allocateAndSetBuffers() {
        byte[] dataBuffer = ByteBuffer.allocateDirect(this.rubikDetector.getRequiredMemory()).array();
        this.currentConfigPreviewFrameByteCount = dataBuffer.length;
        this.camera.addCallbackBuffer(dataBuffer);

        dataBuffer = ByteBuffer.allocateDirect(this.rubikDetector.getRequiredMemory()).array();
        this.camera.addCallbackBuffer(dataBuffer);

        dataBuffer = ByteBuffer.allocateDirect(this.rubikDetector.getRequiredMemory()).array();
        this.camera.addCallbackBuffer(dataBuffer);

        this.camera.setPreviewCallbackWithBuffer(this);
    }

    /**
     *
     */
    private void cleanup() {
        Log.d(TAG, "processing thread before cleanup sync area.");
        synchronized (this.cleanupLock) {
            Log.d(TAG, "processing thread inside cleanup sync area.");
            if (this.rubikDetector != null) {
                this.rubikDetector.releaseResources();
            }
            try {
                if (this.camera != null) {
                    this.camera.setPreviewCallback(null);
                    this.camera.stopPreview();
                    this.camera.release();
                }
            } catch (RuntimeException e) {
                Log.d(TAG, "Error when stopping camera. Ignored.", e);
            } finally {
                Log.d(TAG, "processing thread inside cleanup sync area, cleanup performed, notifying.");
                this.cleanupLock.notify();
                Log.d(TAG, "processing thread inside cleanup sync area, cleanup performed, after notify.");
            }
        }
        Log.d(TAG, "processing thread inside cleanup sync area, cleanup performed, after sync area.");
    }

    /**
     *
     */
    private void detectFaceOrSolve() {
        this.currentFace = this.getNextFace();
        if(this.currentFace != -1) {
            this.faceDetacted = false;
        }
        else {
            if(this.solveInProcessing) {
                this.frontendHandler.sendEmptyMessage(PhotoActivity.MSG_STOP_WATCH);
            }
            else {
                String moves = this.computeMoves();
                if (moves == null) {
                    Message message = this.frontendHandler.obtainMessage(PhotoActivity.MSG_FACE_DETECT_FAIL, "噢～～～～～");
                    message.sendToTarget();
                } else {
                    Message message = this.frontendHandler.obtainMessage(PhotoActivity.MSG_FACE_DETECT_SUCESS, moves);
                    message.sendToTarget();
                    this.solveInProcessing = true;
                    BluetoothUtil.getInstance().sendMessage(moves);
                }
            }
        }
    }

    /**
     *
     * @param data
     */
    private void detectRubikFace(byte[] data) {
        if(this.faceDetacted) {
            return;
        }
        RubikFacelet[][] facelets = this.rubikDetector.findCube(data);
        if(facelets != null) {
            this.faceDetacted = true;
            try {
                Bitmap photoBitmap = Bitmap.createBitmap(this.cameraPreviewWidth, this.cameraPreviewHeight, Bitmap.Config.ARGB_8888);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(this.rubikDetector.getRequiredMemory());
                byteBuffer.put(data, this.rubikDetector.getResultFrameBufferOffset(), this.rubikDetector.getResultFrameByteCount());
                byteBuffer.rewind();
                photoBitmap.copyPixelsFromBuffer(byteBuffer);
                byteBuffer.rewind();
                photoBitmap.copyPixelsFromBuffer(byteBuffer);
                // send to UI
                Message resultMessage = this.frontendHandler.obtainMessage(this.currentFace, photoBitmap);
                resultMessage.sendToTarget();
                this.makeupFaceletString(facelets);
                this.rubikTurnToNextFace();
            }
            catch(Exception exp) {
                Log.e(TAG, exp.getMessage(), exp);
            }
        }
    }

    /**
     *
     */
    private void rubikTurnToNextFace() {
        int nextFace = this.getNextFace();
        if(nextFace == -1) {
            BluetoothUtil.getInstance().sendMessage(ProcessingThread.RubikTurnfaceSequance[ProcessingThread.RubikTurnfaceSequance.length-1]);
        }
        else {
            BluetoothUtil.getInstance().sendMessage(ProcessingThread.RubikTurnfaceSequance[nextFace]);
        }
    }

    /**
     *
     * @return
     */
    private int getNextFace() {
        if(this.currentFace == -1) {
            return ProcessingThread.FaceSequance[0];
        }
        if(this.currentFace == ProcessingThread.FaceSequance[ProcessingThread.FaceSequance.length -1]) {
            return -1;
        }
        for(int i=0; i<ProcessingThread.FaceSequance.length-1; i++) {
           if(ProcessingThread.FaceSequance[i] == this.currentFace) {
               return ProcessingThread.FaceSequance[i+1];
           }
       }
       return -1;
    }

    /**
     *
     * @return cameraId
     */
    private int getMainCameraId(){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for(int i=0; i<Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @param rubikFacelets
     */
    private void makeupFaceletString(RubikFacelet[][] rubikFacelets) {

        RubikFacelet[][] actualFacelets = new RubikFacelet[3][3];

        // U F D 转90度
        if(this.currentFace == PhotoActivity.MSG_FACE_U || this.currentFace == PhotoActivity.MSG_FACE_F || this.currentFace == PhotoActivity.MSG_FACE_D) {
            actualFacelets[0][0] = rubikFacelets[2][0];
            actualFacelets[0][1] = rubikFacelets[1][0];
            actualFacelets[0][2] = rubikFacelets[0][0];

            actualFacelets[1][0] = rubikFacelets[2][1];
            actualFacelets[1][1] = rubikFacelets[1][1];
            actualFacelets[1][2] = rubikFacelets[0][1];

            actualFacelets[2][0] = rubikFacelets[2][2];
            actualFacelets[2][1] = rubikFacelets[1][2];
            actualFacelets[2][2] = rubikFacelets[0][2];
        }
        else if(this.currentFace == PhotoActivity.MSG_FACE_B) {
            // B 转-90度
            actualFacelets[0][0] = rubikFacelets[0][2];
            actualFacelets[0][1] = rubikFacelets[1][2];
            actualFacelets[0][2] = rubikFacelets[2][2];

            actualFacelets[1][0] = rubikFacelets[0][1];
            actualFacelets[1][1] = rubikFacelets[1][1];
            actualFacelets[1][2] = rubikFacelets[2][1];

            actualFacelets[2][0] = rubikFacelets[0][0];
            actualFacelets[2][1] = rubikFacelets[1][0];
            actualFacelets[2][2] = rubikFacelets[2][0];
        }
        else {
            // L R 不转
            actualFacelets[0][0] = rubikFacelets[0][0];
            actualFacelets[0][1] = rubikFacelets[0][1];
            actualFacelets[0][2] = rubikFacelets[0][2];

            actualFacelets[1][0] = rubikFacelets[1][0];
            actualFacelets[1][1] = rubikFacelets[1][1];
            actualFacelets[1][2] = rubikFacelets[1][2];

            actualFacelets[2][0] = rubikFacelets[2][0];
            actualFacelets[2][1] = rubikFacelets[2][1];
            actualFacelets[2][2] = rubikFacelets[2][2];
        }

        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                this.facelets[this.currentFace] += Facename[actualFacelets[i][j].color];
            }
        }
        //XXX
        Log.i(TAG, "facelets[" + this.currentFace + "]=" + this.facelets[this.currentFace]);

        //XXX
        Log.i(TAG, "currentFace=" + this.currentFace);
        Log.i(TAG,">>>> " + rubikFacelets[0][0].color + " " + rubikFacelets[0][1].color + " " + rubikFacelets[0][2].color);
        Log.i(TAG,">>>> " + rubikFacelets[1][0].color + " " + rubikFacelets[1][1].color + " " + rubikFacelets[1][2].color);
        Log.i(TAG,">>>> " + rubikFacelets[2][0].color + " " + rubikFacelets[2][1].color + " " + rubikFacelets[2][2].color);
        Log.i(TAG, ">>>>>");
        Log.i(TAG,">>>> " + actualFacelets[0][0].color + " " + actualFacelets[0][1].color + " " + actualFacelets[0][2].color);
        Log.i(TAG,">>>> " + actualFacelets[1][0].color + " " + actualFacelets[1][1].color + " " + actualFacelets[1][2].color);
        Log.i(TAG,">>>> " + actualFacelets[2][0].color + " " + actualFacelets[2][1].color + " " + actualFacelets[2][2].color);
    }

    /**
     *
     * @return
     */
    private String computeMoves() {
        String faceletString = "";
        for(String str : facelets) {
            faceletString += str;
        }
        int code = Tools.verify(faceletString);
        //XXX
        Log.i(TAG, "verify code=" + code);
        if(code == 0) {
            String moves = Search.solution(faceletString, 21, 5, false);
            //XXX
            Log.i(TAG, "moves=" + moves);
            return moves;
        }
        else {
            return null;
        }
    }
}
