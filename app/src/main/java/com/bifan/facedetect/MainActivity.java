package com.bifan.facedetect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.bifan.detectlib.FaceDetectTextureView;
import com.bifan.detectlib.FaceDetectView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private String tag = "MainActivity";
    private FaceDetectView faceDetectView;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        faceDetectView = findViewById(R.id.faceDetectView);
        faceDetectView.setFramePreViewListener(new FaceDetectTextureView.IFramePreViewListener() {
            @Override
            public boolean onFrame(Bitmap preFrame) {
                //每一帧回调
                //这个这帧preFrame处理了就是进行了回收，返回true
                //否则返回false，内部进行回收处理
                return false;
            }

            @Override
            public boolean onFaceFrame(Bitmap preFrame, FaceDetector.Face[] faces) {
                //faces是检测出来的人脸参数
                //检测到人脸的回调,保存人脸图片到本地
                if (isSavingPic == false) {
                    isSavingPic = true;
                    executorService.submit(new SavePicRunnable(preFrame));
                }

                // Log.i(tag, "当前图片人脸个数：" + faces.length);
                //这个这帧preFrame处理了就是进行了回收，返回true
                //否则返回false，内部进行回收处理
                return true;
            }
        });
    }

    private boolean isSavingPic = false;

    private class SavePicRunnable implements Runnable {
        Bitmap bitmap;

        SavePicRunnable(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            saveFacePicToLocal(bitmap);
            isSavingPic = false;
        }
    }

    private void saveFacePicToLocal(Bitmap bitmap) {
        String picPath = Environment.getExternalStorageDirectory() + "/face.jpg";
        FileOutputStream fileOutputStream = null;
        File facePicFile = new File(picPath);
        try {
            facePicFile.createNewFile();
        } catch (IOException e) {
            Log.e(tag, "保存失败" + e.toString() + "," + picPath);
            e.printStackTrace();
        }
        try {
            fileOutputStream = new FileOutputStream(facePicFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fileOutputStream != null) {
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            try {
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(tag, e.toString());
            }
        }
        bitmap.recycle();
    }

    public void startDetect(View view) {
        if (!faceDetectView.isHasInit()) {
            //必须是在view可见后进行初始化
            faceDetectView.initView();
            faceDetectView.initCamera();
            faceDetectView.getDetectConfig().CameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
            faceDetectView.getDetectConfig().EnableFaceDetect = true;
            faceDetectView.getDetectConfig().MinDetectTime = 100;
            faceDetectView.getDetectConfig().Simple = 0.2f;//图片检测时的压缩取样率，0~1，越小检测越流畅
            faceDetectView.getDetectConfig().MaxDetectTime =2000;//进入智能休眠检测，以2秒一次的这个速度检测
            faceDetectView.getDetectConfig().EnableIdleSleepOption=true;//启用智能休眠检测机制
            faceDetectView.getDetectConfig().IdleSleepOptionJudgeTime=1000*10;//1分钟内没有检测到人脸，进入智能休眠检测
        }
        faceDetectView.startCameraPreview();
    }

    public void endDetect(View view) {
        faceDetectView.stopCameraPreview();
        faceDetectView.getFaceRectView().clearBorder();
    }

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment.newInstance(R.string.camera_permission_confirmation,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION,
                    R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (faceDetectView != null) {
            faceDetectView.release();
        }
    }

    public static class ConfirmationDialogFragment extends DialogFragment {
        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                                                             String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }
    }
}
