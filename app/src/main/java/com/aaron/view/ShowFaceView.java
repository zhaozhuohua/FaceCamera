package com.aaron.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.View;

import com.aaron.utils.CameraUtils;

/**
 * Created by Aaron on 2019/5/16.
 * <p>
 * 显示人脸框界面
 */
public class ShowFaceView extends View {
    private RectF mRect = new RectF();
    private Camera.Face[] mFaces;
    private boolean mIsBackCamera = false;  //是否后置摄像头
    private int rotateDegree = 0;

    public ShowFaceView(Context context) {
        super(context);
    }

    public ShowFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowFaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRotateDegree(int rotate) {
        rotateDegree = rotate;
    }

    public void setFaces(Camera.Face[] faces) {
        mFaces = faces;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFaces != null) {

            Matrix matrix = CameraUtils.INSTANCE.prepareMatrix(mIsBackCamera, rotateDegree, getWidth(), getHeight());
            Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(3);  //设置位图上paint操作的参数
            for (Camera.Face face : mFaces) {
                mRect.set(face.rect);
                matrix.mapRect(mRect);
//                ToastUtils.INSTANCE.showShortToast(mRect.toString(), getContext());
                canvas.drawRect(mRect, myPaint);
            }
        }
    }

    /**
     * 是否为后置摄像头
     * @param isBack
     */
    public void setIsBack(boolean isBack) {
        mIsBackCamera = isBack;
    }
}