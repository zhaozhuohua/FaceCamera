package com.aaron.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.hardware.Camera
import android.util.AttributeSet
import android.view.View
import com.aaron.utils.CameraUtils
import com.aaron.utils.DrawFaceHelper

/**
 * Created by Aaron on 2019/8/22.
 *
 *
 * 显示人脸框界面
 */
class ShowFaceView : View {
    private val mRect = RectF()
    private var mFaces: Array<Camera.Face>? = null
    private var mIsBackCamera = false  //是否后置摄像头

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setFaces(faces: Array<Camera.Face>?) {
        mFaces = faces
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mFaces != null) {
            var displayOrientation: Int = if (DrawFaceHelper.isBackCameraId) { 90 } else { 270 }
            val matrix = CameraUtils.prepareMatrix(mIsBackCamera, displayOrientation, width, height)
            val myPaint = Paint()
            myPaint.color = Color.GREEN
            myPaint.style = Paint.Style.STROKE
            myPaint.strokeWidth = 3f  //设置位图上paint操作的参数
            for (face in mFaces!!) {
                mRect.set(face.rect)
                matrix.mapRect(mRect)
                canvas.drawRect(mRect, myPaint)
            }
        }
    }

    /**
     * 是否为后置摄像头
     * @param isBack
     */
    fun setIsBack(isBack: Boolean) {
        mIsBackCamera = isBack
    }
}