package com.aaron.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Camera
import android.util.AttributeSet
import android.view.View

import com.aaron.utils.DrawFaceHelper
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Aaron on 2019/8/22.
 * 人脸识别动效界面
 */
class FaceRectView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val faceRectList = CopyOnWriteArrayList<Camera.Face>()
    private var color = 0
    private var resetDrawInfo: Rect? = null

    init {

        color = Color.parseColor("#FFF600")
        val rect = Rect(0, 0, 0, 0)
        resetDrawInfo = rect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (faceRectList != null && faceRectList.isNotEmpty()) {
            DrawFaceHelper.drawFaceRect(canvas, faceRectList[0].rect, color, 3)

            //不断进行刷新，实现人脸框动画更新
            postInvalidate()
        } else {
            DrawFaceHelper.resetTimerInfo()
        }
    }

    fun clearFaceInfo() {
        faceRectList.clear()
        postInvalidate()
    }

    fun addFaceInfo(faceInfo: Camera.Face) {
        faceRectList.add(faceInfo)
        postInvalidate()
    }

    fun addFaceInfo(faceInfoList: List<Camera.Face>) {
        faceRectList.addAll(faceInfoList)
        postInvalidate()
    }
}