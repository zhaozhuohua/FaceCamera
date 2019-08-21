package com.aaron.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.View;

import com.aaron.utils.DrawFaceHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.Nullable;

public class FaceRectView extends View {

    private CopyOnWriteArrayList<Camera.Face> faceRectList = new CopyOnWriteArrayList<>();
    private int color = 0;
    private Rect resetDrawInfo = null;

    public FaceRectView(Context context) {
        this(context, null);
    }

    public FaceRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        color = Color.parseColor("#FFF600");
        Rect rect = new Rect(0, 0, 0, 0);
        resetDrawInfo = rect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (faceRectList != null && faceRectList.size() > 0) {
            DrawFaceHelper.INSTANCE.drawFaceRect(canvas, faceRectList.get(0).rect, color, 3);

            //不断进行刷新，实现人脸框动画更新
            postInvalidate();
        } else {
            DrawFaceHelper.INSTANCE.resetTimerInfo();
        }
    }

    public void clearFaceInfo() {
        faceRectList.clear();
        postInvalidate();
    }

    public void addFaceInfo(Camera.Face faceInfo) {
        faceRectList.add(faceInfo);
        postInvalidate();
    }

    public void addFaceInfo(List<Camera.Face> faceInfoList) {
        faceRectList.addAll(faceInfoList);
        postInvalidate();
    }
}