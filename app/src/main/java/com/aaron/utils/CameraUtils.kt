package com.aaron.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.Camera
import android.util.Size
import android.view.Surface
import android.view.WindowManager

/**
 * Created by Aaron on 2019/8/21.
 */
object CameraUtils {

    /**
     * 获取旋转角度
     */
    fun getDisplayOrientation(cameraId: Int, context: Context): Int {
        val camInfo = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, camInfo)

        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val rotation = display.rotation
        var degrees = 0
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }
        } else {
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 180
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }
        }

        return (camInfo.orientation - degrees) % 360
    }

    /**
     * 获取人脸框信息
     */
    fun faceCoordinate(matrix: Matrix, face: Camera.Face): RectF {
        val rectF = RectF()
        rectF.set(face.rect)
        matrix.mapRect(rectF)

        return rectF
    }

    /**
     * 准备用于旋转的矩阵工具
     *
     * @param matrix
     * @param isBackCamera
     * @param displayOrientation
     * @param viewWidth
     * @param viewHeight
     */
    fun prepareMatrix(isBackCamera: Boolean, displayOrientation: Int,
                      viewWidth: Int, viewHeight: Int): Matrix {
        val matrix = Matrix()
        //可以调节绿框位置镜像关系
        matrix.setScale(1f, (if (!isBackCamera) -1 else 1).toFloat())
        // This mIsBackCamera the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation.toFloat())
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f)
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f)

        return matrix
    }


    /**
     * 根据相机预览界面获取新的拍照界面大小
     */
    fun updateTextureViewSize(w:Int, h:Int, previewW:Int, previewH:Int): Size {
        var ratio = updateTextureViewRatio(w, h, previewW, previewH)

        return Size((previewW * ratio).toInt(), (previewH * ratio).toInt())
    }

    /**
     * 计算需要调整的预览界面比例
     */
    fun updateTextureViewRatio(w:Int, h:Int, previewW:Int, previewH:Int):Float {
        val ratioW = w * 1.0f / previewW
        val ratioH = h * 1.0f / previewH

        return if (ratioW > ratioH) ratioH else ratioW
    }
}