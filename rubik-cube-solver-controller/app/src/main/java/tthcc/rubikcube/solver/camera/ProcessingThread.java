package tthcc.rubikcube.solver.camera;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import com.catalinjurjiu.rubikdetector.RubikDetector;
import com.catalinjurjiu.rubikdetector.RubikDetectorUtils;
import com.catalinjurjiu.rubikdetector.config.DrawConfig;
import com.catalinjurjiu.rubikdetector.model.RubikFacelet;

import java.nio.ByteBuffer;

public class ProcessingThread extends HandlerThread implements Camera.PreviewCallback {

    private String TAG = ProcessingThread.class.getSimpleName();

    private static final int PreviewFormat = ImageFormat.NV21;

    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_START_CAMERA = 2;
    private static final int MSG_CLEANUP = 3;
    private static final int MSG_DETECT_FACE = 4;

    private int cameraPreviewWidth;
    private int cameraPreviewHeight;
    private int currentConfigPreviewFrameByteCount = -1;

    private SurfaceHolder surfaceHolder;
    private Handler backgroundHandler;
    private Handler frontendHandler;
    private int frontendMessageId;
    private Camera camera;

    private RubikDetector rubikDetector;

    private final Object cleanupLock = new Object();
    private boolean faceDetacted = true;


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

    public void performDetectFace(int frontendMessageId) {
        this.frontendMessageId = frontendMessageId;
        this.backgroundHandler.sendEmptyMessage(ProcessingThread.MSG_DETECT_FACE);
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
                    case MSG_DETECT_FACE:
                        detectFace();
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
            renderFrameInternal(data);
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

    private void detectFace() {
        this.faceDetacted = false;
    }

    private void renderFrameInternal(byte[] data) {
        if(this.faceDetacted) {
            return;
        }
        RubikFacelet[][] facelets = this.rubikDetector.findCube(data);
        if(facelets != null) {
            try {
                Bitmap photoBitmap = Bitmap.createBitmap(this.cameraPreviewWidth, this.cameraPreviewHeight, Bitmap.Config.ARGB_8888);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(this.rubikDetector.getRequiredMemory());
                byteBuffer.put(data, this.rubikDetector.getResultFrameBufferOffset(), this.rubikDetector.getResultFrameByteCount());
                byteBuffer.rewind();
                photoBitmap.copyPixelsFromBuffer(byteBuffer);
                byteBuffer.rewind();
                photoBitmap.copyPixelsFromBuffer(byteBuffer);
                // send to UI
                DetectResult detectResult = new DetectResult();
                detectResult.setFaceBitmap(photoBitmap);
                detectResult.setFacelets(facelets);
                Message resultMessage = this.frontendHandler.obtainMessage(this.frontendMessageId, detectResult);
                resultMessage.sendToTarget();

                this.faceDetacted = true;
            }
            catch(Exception exp) {
                Log.e(TAG, exp.getMessage(), exp);
            }
        }
    }

//    private Camera.Size findHighResValidPreviewSize(final Camera camera) {
//        int minWidthDiff = 100000;
//        Camera.Size desiredWidth = camera.new Size(cthis.ameraPreviewWidth, cameraPreviewHeight);
//        for (Camera.Size size : this.validPreviewFormatSizes) {
//            int diff = cameraPreviewWidth - size.width;
//
//            if (Math.abs(diff) < minWidthDiff) {
//                minWidthDiff = Math.abs(diff);
//                desiredWidth = size;
//            }
//        }
//        return desiredWidth;
//    }


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
}
