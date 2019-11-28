package com.bifan.detectlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by ： bifan-wei
 */
public class FaceDetectTextureView extends TextureView implements View.OnLayoutChangeListener {
    private String tag = "FaceDetectView";
    public Camera mCamera;
    private Camera.Parameters CameraParam;
    private Bitmap captureBitmap;

    private int mWidth = 0;
    private int mHeight = 0;
    private DetectConfig detectConfig = new DetectConfig();
    private IFaceRectView faceRectView;//人脸检测绘制框，不指定的话，初始化摄像头是默认使用一个
    private boolean hasInit = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public FaceDetectTextureView(Context context) {
        super(context);
    }

    public FaceDetectTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetectConfig getDetectConfig() {
        return detectConfig;
    }

    //初始化摄像头
    public void initCamera() {
        if (!hasInit) {
            openCamera();
            boolean success = initCameraParam();
            if(success) {
                initOthers();
                this.setSurfaceTextureListener(surfaceTextureListener);
            }
            hasInit = true;
        }
    }

    //初始化摄像头，指定是哪个摄像头
    public void initCamera(int CameraType) {
        if (!hasInit) {
            getDetectConfig().CameraType = CameraType;
            openCamera();
            boolean success = initCameraParam();
            if(success) {
                initOthers();
                this.setSurfaceTextureListener(surfaceTextureListener);
            }
            hasInit = true;
        }
    }

    private void initOthers() {
        if (captureBitmap == null && mWidth > 0 && mHeight > 0) {
            captureBitmap = Bitmap.createBitmap((int) (mWidth * getDetectConfig().Simple), (int) (mHeight * getDetectConfig().Simple), Bitmap.Config.RGB_565);
        }
    }

    private void openCamera() {
        if (mCamera == null && getDetectConfig().CameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCamera = openFrontCamera();
        }
        if (mCamera == null && getDetectConfig().CameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCamera = Camera.open();
        }
        //如果都初始化失败了，不区别摄像头类型重新初始化一遍
        if (mCamera == null) {
            mCamera = openFrontCamera();
        }
        if (mCamera == null) {
            mCamera = Camera.open();
        }
    }

    public static Camera openFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return Camera.open(cameraId);
            }
        }
        return null;
    }


    private SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            release();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            if (captureBitmap != null && !captureBitmap.isRecycled() && getDetectConfig().EnableFaceDetect) {
                //here to preview each frame
                long currentTime = System.currentTimeMillis();
                long detectTime = getDetectConfig().MinDetectTime;
                if (getDetectConfig().EnableIdleSleepOption) {
                    if (currentTime - getDetectConfig().PreFaceTime > getDetectConfig().IdleSleepOptionJudgeTime) {
                        detectTime = getDetectConfig().MaxDetectTime;
                       // Log.i(tag, "进入空闲休眠检测状态");
                    }
                }

                if (currentTime - getDetectConfig().PreDetectTime >= detectTime) {
                    getDetectConfig().PreDetectTime = currentTime;
                    executorService.execute(new FaceCapturedRunnable());
                }
                if (framePreViewListener != null) {
                    Bitmap frameBitmap = captureBitmap.copy(Bitmap.Config.RGB_565, false);
                    boolean deal = framePreViewListener.onFrame(frameBitmap);
                    if (!deal && !frameBitmap.isRecycled()) {
                        frameBitmap.recycle();
                    }
                }
            }

        }
    };

    private class FaceCapturedRunnable implements Runnable {
        @Override
        public void run() {
            getBitmap(captureBitmap);
            FaceDetector.Face[] faces = detectFace(captureBitmap);
            boolean hasFace = faces != null;
            if (hasFace && getDetectConfig().EnableFaceDetect) {
                if (framePreViewListener != null) {
                    Bitmap faceBitmap = captureBitmap.copy(Bitmap.Config.RGB_565, false);
                    boolean deal = framePreViewListener.onFaceFrame(faceBitmap, faces);
                    if (!deal && !faceBitmap.isRecycled()) {
                        faceBitmap.recycle();
                    }

                }
            }
        }
    }


    private FaceDetector.Face[] detectFace(Bitmap captureBitmap) {
        FaceDetector mFaceDetector = new FaceDetector(captureBitmap.getWidth(), captureBitmap.getHeight(), getDetectConfig().DETECT_FACE_NUM);
        FaceDetector.Face[] mFace = new FaceDetector.Face[getDetectConfig().DETECT_FACE_NUM];
        int detectedFaceNum = mFaceDetector.findFaces(captureBitmap, mFace);
        if (detectedFaceNum > 0) {
            getDetectConfig().PreFaceTime = System.currentTimeMillis();
            // Log.i(tag, "找到人脸了，耗时：" + (System.currentTimeMillis() - time));
            if (faceRectView != null) {
                faceRectView.drawFaceBorder(mFace, getDetectConfig().Simple);
            }
            return mFace;
        } else {
            if (faceRectView != null) {
                faceRectView.clearBorder();
            }
        }
        return null;
    }

    private boolean initCameraParam() {
        if (mCamera == null) {
            Log.e(tag, "如果这里抛出空指针，确定设备摄像头是正常的？");
            return false;
        }
        CameraParam = mCamera.getParameters();
        CameraParam.setPictureFormat(PixelFormat.JPEG);
        CameraParam.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        CameraParam.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        int displayRotation = 0;
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                displayRotation = 0;
                break;
            case Surface.ROTATION_90:
                displayRotation = 90;
                break;
            case Surface.ROTATION_180:
                displayRotation = 180;
                break;
            case Surface.ROTATION_270:
                displayRotation = 270;
                break;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        int orientation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            orientation = (info.orientation - displayRotation + 360) % 360;
        } else {
            orientation = (info.orientation + displayRotation) % 360;
            orientation = (360 - orientation) % 360;
        }
        mCamera.setDisplayOrientation(orientation);
        mCamera.setParameters(CameraParam);
        return true;
    }

    public void startCameraPreview() {
        try {
            mCamera.setPreviewTexture(getSurfaceTexture());
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopCameraPreview() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewTexture(null);
                mCamera.stopPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        mWidth = right - left;
        mHeight = bottom - top;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = right - left;
        mHeight = bottom - top;
    }

    public void release() {
        getDetectConfig().EnableFaceDetect = false;
        stopCameraPreview();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (captureBitmap != null) {
            if (!captureBitmap.isRecycled()) {
                captureBitmap.recycle();
            }
            captureBitmap = null;
        }
        setFramePreViewListener(null);
        setFaceRectView(null);
        try {
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private IFramePreViewListener framePreViewListener;

    public void setFramePreViewListener(IFramePreViewListener framePreViewListener) {
        this.framePreViewListener = framePreViewListener;
    }

    //是否开启人脸检测，默认是开启的
    public boolean isEnableFaceDetect() {
        return getDetectConfig().EnableFaceDetect;
    }

    public boolean isHasInit() {
        return hasInit;
    }

    public void setFaceRectView(IFaceRectView faceRectView) {
        this.faceRectView = faceRectView;
    }

    //PreView each frame of the camera
    public interface IFramePreViewListener {
        //这个preFrame没次都复制一份出来，记得主动回收
        //如果处理了，返回true，如果没处理回收bitmap，返回false
        //会自动进行处理
        boolean onFrame(Bitmap eachFrame);

        //这个preFrame没次都复制一份出来，记得主动回收
        boolean onFaceFrame(Bitmap faceFrame, FaceDetector.Face[] faces);
    }
}
