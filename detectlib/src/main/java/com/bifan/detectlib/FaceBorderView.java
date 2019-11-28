package com.bifan.detectlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * created by ： bifan-wei
 */
public class FaceBorderView extends View implements IFaceRectView {
    private final int BORDER_COLOR = Color.WHITE;
    private final int BORDER_WITH = 3;

    public FaceBorderView(Context context) {
        super(context);
        init();
    }

    public FaceBorderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private Paint paint;
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(BORDER_WITH);
        paint.setColor(BORDER_COLOR);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void drawFaceBorder(FaceDetector.Face[] mFace, float simple) {
        FaceDetector.Face face = mFace[0];
        //可行度大于0.5才进行绘制
        if (face.confidence() > 0.5) {
            float eyeDistance = (float) (face.eyesDistance() * 1.5)/simple;
            //float faceWidth = eyeDistance * 5;
            face.getMidPoint(eyeMidPoint);
            eyeMidPoint.x=eyeMidPoint.x/simple;
            eyeMidPoint.y=eyeMidPoint.y/simple;


            float leftX = eyeMidPoint.x - eyeDistance;
            float rightX = eyeMidPoint.x + eyeDistance;
            float topY = eyeMidPoint.y - eyeDistance;
            float bottomY = eyeMidPoint.y + eyeDistance*1.2f;


            borderPath.reset();
            borderPath.moveTo(leftX,topY);
            borderPath.lineTo(rightX,topY);
            borderPath.lineTo(rightX,bottomY);
            borderPath.lineTo(leftX,bottomY);
            borderPath.lineTo(leftX,topY);
            postInvalidate();
        }
    }

    private PointF eyeMidPoint = new PointF();
    private Path borderPath = new Path();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(borderPath, paint);
    }

    @Override
    public void clearBorder() {
        borderPath.reset();
        postInvalidate();
    }
}
