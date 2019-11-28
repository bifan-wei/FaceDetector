### 前言
之前有个项目需要带人脸检测，检测完成后需要上传服务器进行人脸识别。目前有很多方案可以用，很多是第三方的，比如虹软、百度。但是缺点就是，商用收费，而且不支持离线，需要联网验证。再者，我们只需要人脸检测这么简单的功能就是了，不需要搞那么复杂。后面找到这个文章，https://blog.csdn.net/janecer/article/details/79092212，按照上面的，也实现起来了，项目中也用起来，但是也有其中的缺点：不够灵活，代码啰嗦，摄像头初始化慢，人脸检测流畅度差强人意，只能说能用。后面接触了些收费的人脸识别sdk，参考了它们的一些方案，于是做了这个人脸检测的，应该能满足这种需求了。

**实现原理请看：**

### 实现功能
- 摄像头预览同时检测人脸，并绘制人脸框
- 能获取到检测到人脸图片，方便进行下一步业务操作
- 使用TextureView预览摄像头，支持异步检测，画面流畅性好
- 灵活封装，检测时间与频率参数支持动态设置，满足不同设备的需求
- 支持无人脸时智能进入休眠检测机制

### 集成使用
- 引入FaceDetector
```xml
allprojects {
    repositories {
    ...
        maven { url 'https://jitpack.io' }
    }
}

 implementation 'com.github.bifan-wei:FaceDetector:V1.0'

```

- 引入FaceDetectView
 ```xml
  <com.bifan.detectlib.FaceDetectView
        android:id="@+id/faceDetectView"
        android:layout_width="800px"
        android:layout_height="800px"
        android:layout_centerInParent="true"
        android:background="#000000" />
```
- 添加画面监听，用于项目中的业务处理
```java
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
                Log.i(tag, "当前图片人脸个数：" + faces.length);
                //这个这帧preFrame处理了就是进行了回收，返回true
                //否则返回false，内部进行回收处理
                return true;
            }
        });
   ```
- 启动检测，开始前进行初始化参数
```java
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
```
- 停止检测
```java
 public void endDetect(View view) {
        faceDetectView.stopCameraPreview();
        faceDetectView.getFaceRectView().clearBorder();
    }
```
- 注意的问题
1.必须有摄像头权限：
```xml
 <uses-permission android:name="android.permission.CAMERA" />
 ```
