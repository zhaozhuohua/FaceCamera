package com.aaron.utils

import android.graphics.*
import java.util.*

/**
 * Created by Aaron on 2019-07-19.
 *
 * 绘制人脸框特效
 */
object DrawFaceHelper {

    private const val useCenter = false

    //弧形大小偏移量
    private const val faceOffset0 = 20
    private const val faceOffset1 = 25
    private const val faceOffset2 = 33
    private const val faceOffset3 = 40

    //弧形透明度
    private const val faceAlpha0 = 128
    private const val faceAlpha1 = 200
    private const val faceAlpha2 = 128
    private const val faceAlpha3 = 220

    //弧形起始角度
    private const val faceStartAngle0 = 300f
    private const val faceStartAngle1 = 60f
    private const val faceStartAngle2 = 240f
    private const val faceStartAngle3 = 120f
    private const val faceStartAngle4 = 300f

    //弧形角度偏移量
    private const val faceSweepAngle0 = 240f
    private const val faceSweepAngle1 = 240f
    private const val faceSweepAngle2 = 240f
    private const val faceSweepAngle3 = 60f
    private const val faceSweepAngle4 = 60f

    //遮罩层透明度
    private const val maskAlpha = 150
    var isBackCameraId = true

    var cameraWidth = 0f
    var cameraHeight = 0f

    /**
     * 绘制数据信息到view上，若 [DrawInfo.getName] 不为null则绘制 [DrawInfo.getName]
     *
     * @param canvas            需要被绘制的view的canvas
     * @param drawInfo          绘制信息
     * @param color             rect的颜色
     * @param faceRectThickness 人脸框线条粗细
     */
    fun drawFaceRect(canvas: Canvas?, drawInfo: Rect?, color: Int, faceRectThickness: Int) {

        var displayOrientation: Int = if (isBackCameraId) { 90 } else { 270 }

        //将返回的人脸信息，转为view可识别的信息
        val matrix = CameraUtils.prepareMatrix(isBackCameraId, displayOrientation, cameraWidth.toInt(), cameraHeight.toInt())
        val rectF = RectF(drawInfo)
        matrix.mapRect(rectF)

        if (canvas == null || drawInfo == null) {
            return
        }
        if (timer == null) {
            initTimerInfo()
        }
        val paint = Paint()
        paint.style = Paint.Style.STROKE  //设置空心
        paint.strokeWidth = faceRectThickness.toFloat()

        val spic = 100
        val rect = Rect(rectF.left.toInt() - spic, rectF.top.toInt() - spic, rectF.right.toInt() + spic, rectF.bottom.toInt() + spic)

        val width = rect.right - rect.left
        val height = rect.bottom - rect.top
        //比较小的设置为半径
        val radius = if (height > width) { width / 2 } else { height / 2 }
        val cx = (rect.left + rect.right) / 2
        val cy = (rect.top + rect.bottom) / 2

        //绘制遮罩层
        val maskPaint = Paint()
        //整个相机预览界面进行遮罩，透明度用来设置遮罩半透明
        canvas.saveLayerAlpha(0f, 0f, cameraWidth, cameraHeight, maskAlpha, Canvas.ALL_SAVE_FLAG)
        maskPaint.color = Color.BLACK
        //整个相机预览界面进行遮罩
        canvas.drawRect(Rect(0, 0, cameraWidth.toInt(), cameraHeight.toInt()), maskPaint)
        //重叠区域进行处理
        //只在源图像和目标图像不相交的地方绘制【源图像】，相交的地方根据目标图像的对应地方的alpha进行过滤，
        //目标图像完全不透明则完全过滤，完全透明则不过滤
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        maskPaint.color = Color.BLACK
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), maskPaint)

        //自定义人脸框样式
        paint.flags = Paint.ANTI_ALIAS_FLAG  //抗锯齿
        paint.color = color

        //设置正方形，一遍下面设置弧形为圆的弧形
        rect.left = cx - radius
        rect.top = cy - radius
        rect.right = cx + radius
        rect.bottom = cy + radius

        val rectF0 = getRectF(faceOffset0, rect)
        paint.strokeWidth = 4f
        paint.alpha = faceAlpha0
        canvas.drawArc(rectF0, getStartAngle(faceStartAngle0, false), faceSweepAngle0, useCenter, paint)

        val rectF1 = getRectF(faceOffset1, rect)
        paint.strokeWidth = 8f
        paint.alpha = faceAlpha1
        canvas.drawArc(rectF1, getStartAngle(faceStartAngle1, true), faceSweepAngle1, useCenter, paint)

        val rectF2 = getRectF(faceOffset2, rect)
        paint.strokeWidth = 3f
        paint.alpha = faceAlpha2
        canvas.drawArc(rectF2, getStartAngle(faceStartAngle2, false), faceSweepAngle2, useCenter, paint)

        paint.alpha = faceAlpha3
        val rectF3 = getRectF(faceOffset3, rect)
        paint.strokeWidth = 9f
        canvas.drawArc(rectF3, getStartAngle(faceStartAngle3, true), faceSweepAngle3, useCenter, paint)

        val rectF4 = getRectF(faceOffset3, rect)
        paint.strokeWidth = 9f
        canvas.drawArc(rectF4, getStartAngle(faceStartAngle4, true), faceSweepAngle4, useCenter, paint)
    }

    /**
     * 设置圆环偏移量
     * @param startAngle 角度
     * @param clockwise  是否顺时针
     */
    private fun getStartAngle(startAngle:Float, clockwise: Boolean):Float {
        val angle = if (clockwise) { startAngle + timeIndex } else { startAngle - timeIndex}

        return angle % 360
    }

    private fun getRectF(offset: Int, rect: Rect): RectF {
        val rectF = RectF(rect)
        rectF.left = rectF.left - offset
        rectF.top = rectF.top - offset
        rectF.right = rectF.right + offset
        rectF.bottom = rectF.bottom + offset

        return rectF
    }

    private var timer:Timer? = null
    private var timeIndex = 0
    private var timerTask: MyTimerTask? = null

    class MyTimerTask:TimerTask() {
        override fun run() {
            timeIndex += 1
        }
    }

    /**
     * 初始化
     */
    @Synchronized
    private fun initTimerInfo() {
        resetTimerInfo()
        timer = Timer()
        timerTask = MyTimerTask()
        timer!!.schedule(timerTask, 10, 10)
//        cameraWidth = FaceApplication.sInstance.resources.getDimension(R.dimen.camera_w)
//        cameraHeight = FaceApplication.sInstance.resources.getDimension(R.dimen.camera_h)
    }


    /**
     * 重置
     */
    @Synchronized
    fun resetTimerInfo() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
        timerTask = null
        //todo 不进行角度偏移量初始化，有可能两次显示人脸的时间间隔很短，初始化的话有可能造成动画不连贯
//        timeIndex = 0
    }
}