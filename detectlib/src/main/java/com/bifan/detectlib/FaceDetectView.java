package com.bifan.detectlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * created by ： bifan-wei
 */
public class FaceDetectView extends RelativeLayout {
    public FaceDetectView(Context context) {
        super(context);
    }

    public FaceDetectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private FaceDetectTextureView faceDetectTextureView;
    private IFaceRectView faceRectView;
    private FaceDetectTextureView.IFramePreViewListener framePreViewListener;

    public void setFramePreViewListener(FaceDetectTextureView.IFramePreViewListener framePreViewListener) {
        this.framePreViewListener = framePreViewListener;
        if (faceDetectTextureView != null) {
            faceDetectTextureView.setFramePreViewListener(framePreViewListener);
        }
    }

    public void initView() {
        removeAllViews();
        faceDetectTextureView = new FaceDetectTextureView(getContext());
        faceDetectTextureView.setLayoutParams(getParam());
        // 初始化一个绘制框
        if(faceRectView==null) {
            faceRectView = new FaceBorderView(getContext());
        }
        ((View) faceRectView).setLayoutParams(getParam());
        faceDetectTextureView.setFaceRectView(faceRectView);
        faceDetectTextureView.setFramePreViewListener(framePreViewListener);
        addView(faceDetectTextureView);
        addView(((View) faceRectView));

    }



    private LayoutParams getParam() {
        return new LayoutParams(getMeasuredWidth(), getMeasuredHeight());
    }

    public boolean isHasInit() {
        if (faceDetectTextureView != null) {
            return faceDetectTextureView.isHasInit();
        }
        return false;
    }

    public void initCamera() {
        if (faceDetectTextureView != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    faceDetectTextureView.initCamera();
                }
            });

        }
    }

    public void initCamera(final int CameraType) {
        if (faceDetectTextureView != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    faceDetectTextureView.initCamera(CameraType);
                }
            });
        }
    }

    public void startCameraPreview() {
        if (faceDetectTextureView != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    faceDetectTextureView.startCameraPreview();
                }
            });

        }
    }

    public void stopCameraPreview() {
        if (faceDetectTextureView != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    faceDetectTextureView.stopCameraPreview();
                }
            });

        }
    }

    public DetectConfig getDetectConfig() {
        return faceDetectTextureView == null ? null : faceDetectTextureView.getDetectConfig();
    }
    public Bitmap getCurrentBitmap() {
        return faceDetectTextureView == null ? null : faceDetectTextureView.getBitmap();
    }

    public FaceDetectTextureView getFaceDetectTextureView() {
        return faceDetectTextureView;
    }

    public IFaceRectView getFaceRectView() {
        return faceRectView;
    }

    public void setFaceRectView(IFaceRectView faceRectView) {
        this.faceRectView = faceRectView;
    }

    public FaceDetectTextureView.IFramePreViewListener getFramePreViewListener() {
        return framePreViewListener;
    }


    public void release() {
        if (faceDetectTextureView != null) {
            faceDetectTextureView.release();
        }
        removeAllViews();
    }


}
