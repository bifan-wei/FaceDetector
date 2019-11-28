package com.bifan.detectlib;

import android.media.FaceDetector;

/**
 * created by ： bifan-wei
 */
public interface IFaceRectView {
    /**
     * @param mFace 人脸参数
     * @param simple 图片压缩率
     */
    void drawFaceBorder(FaceDetector.Face[] mFace, float simple);
     //清除边框线
    void clearBorder();
}
