package com.bifan.detectlib;

import android.hardware.Camera;

/**
 * created by ： bifan-wei
 */
public class DetectConfig {
    public int CameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;//摄像头类型默认是前置摄像头
    public long PreDetectTime = 0;//上一次检测时刻
    public long PreFaceTime = 0;//上一次检测到人脸的时刻
    public float Simple = 0.1f;//图片检测时的压缩取样率，0~1，越小检测越流畅
    public long MinDetectTime = 200; //最小检测时间，越小检测频率越高，可能会导致耗时卡顿加大
    public long MaxDetectTime = 1000;//最大检测时间，需要比MinDetectTime大，启动EnableIdleSleepOption后
                                     //检测到IdleSleepOptionJudgeNum次没有检测到人脸，将使用MaxDetectTime进行检测人脸
                                     //当处于空闲是，此操作用于缓解cpu
    public boolean EnableFaceDetect = true;//是否开启人脸检测
    public boolean EnableIdleSleepOption = false;//是启动空闲休眠机制
    public long IdleSleepOptionJudgeTime= 60*1000;//这个时间后，如果都没有检测到人脸，将进入空闲休眠检测状态
    public int DETECT_FACE_NUM = 1;//需要检测的人脸个数

}
