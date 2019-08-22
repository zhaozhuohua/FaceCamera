package com.aaron.camera

import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.TextureView
import java.io.IOException
import java.util.*

/**
 * 相机辅助类
 */
class CameraControllerHelper private constructor(builder: Builder) : Camera.PreviewCallback {
    private var mCamera: Camera? = null
    private var mCameraId: Int = 0
    private val previewViewSize: Point?
    private val previewDisplayView: TextureView?
    var previewSize: Camera.Size? = null
        private set
    private var pictureSize: Camera.Size? = null
    private val specificPreviewSize: Point?
    private var displayOrientation = 0  //设置预览方向
    private var rotation: Int = 0  //屏幕方向
    private var additionalRotation: Int = 0  //额外的旋转角度（用于适配一些定制设备）
    private var isMirror = false
    private val autoFocusCallback: Camera.AutoFocusCallback?
    private val mFaceDetectionListener: Camera.FaceDetectionListener?

    private var specificCameraId: Int? = null
    private val cameraListener: CameraControllerListener?

    private val currentPictureSize: Camera.Size?
        get() {
            val supportedPictureSizes = mCamera!!.parameters.supportedPictureSizes
            if (supportedPictureSizes != null && supportedPictureSizes.size > 0) {
                pictureSize = getBestSupportedSize(supportedPictureSizes, previewViewSize)
            }

            return pictureSize
        }

    private val currentPreviewSize: Camera.Size?
        get() {
            val supportedPretureSizes = mCamera!!.parameters.supportedPreviewSizes
            if (supportedPretureSizes != null && supportedPretureSizes.size > 0) {
                previewSize = getBestSupportedSize(supportedPretureSizes, previewViewSize)
            }

            return previewSize
        }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            setSurfaceTexture(surfaceTexture)
        }

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: $width  $height")
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            stop()
            return false
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

        }
    }

    init {
        previewDisplayView = builder.previewDisplayView
        specificCameraId = builder.specificCameraId
        cameraListener = builder.cameraListener
        rotation = builder.rotation
        additionalRotation = builder.additionalRotation
        previewViewSize = builder.previewViewSize
        specificPreviewSize = builder.previewSize
        autoFocusCallback = builder.autoFocusCallback
        mFaceDetectionListener = builder.faceDetectionListener
        if (builder.previewDisplayView is TextureView) {
            isMirror = builder.isMirror
        } else if (isMirror) {
            throw RuntimeException("mirror is effective only when the preview is on a textureView")
        }
    }

    fun setSpecificCameraId(id: Int) {
        specificCameraId = id
    }

    fun init() {
        this.previewDisplayView!!.surfaceTextureListener = textureListener

        if (isMirror) {
            previewDisplayView.scaleX = -1f
        }
    }

    fun start() {
        synchronized(this) {
            if (mCamera != null) {
                return
            }
            //若指定了相机ID且该相机存在，则打开指定的相机
            if (specificCameraId != null) {
                mCameraId = specificCameraId!!
            } else {
                //相机数量为2则打开1,1则打开0,相机ID 1为前置，0为后置
                mCameraId = Camera.getNumberOfCameras() - 1
            }

            //没有相机
            if (mCameraId == -1) {
                cameraListener?.onCameraError(Exception("camera not found"))
                return
            }
            if (mCamera == null) {
                mCamera = Camera.open(mCameraId)
            }
            displayOrientation = getCameraOri(rotation)
            mCamera!!.setDisplayOrientation(displayOrientation)
            try {
                val parameters = mCamera!!.parameters
                parameters.previewFormat = ImageFormat.NV21

                //预览大小设置
                previewSize = parameters.previewSize
                val supportedPreviewSizes = parameters.supportedPreviewSizes
                if (supportedPreviewSizes != null && supportedPreviewSizes.size > 0) {
                    previewSize = currentPreviewSize
                }

                parameters.setPreviewSize(previewSize!!.width, previewSize!!.height)

                pictureSize = parameters.pictureSize
                val supportedPicviewSizes = parameters.supportedPreviewSizes
                if (supportedPicviewSizes != null && supportedPicviewSizes.size > 0) {
                    pictureSize = currentPictureSize
                }
                parameters.setPictureSize(pictureSize!!.width, pictureSize!!.height)


                //对焦模式设置
                val supportedFocusModes = parameters.supportedFocusModes
                if (supportedFocusModes != null && supportedFocusModes.size > 0) {
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                    }
                }
                setCameraParameters(parameters)
                mCamera!!.setPreviewTexture(previewDisplayView!!.getSurfaceTexture())
                mCamera!!.setPreviewCallback(this)
                mCamera!!.startPreview()
                if (mFaceDetectionListener != null) {
                    mCamera!!.setFaceDetectionListener(mFaceDetectionListener)
                    mCamera!!.startFaceDetection()
                }
                if (autoFocusCallback != null) {
                    mCamera!!.autoFocus(autoFocusCallback)
                }
                cameraListener?.onCameraOpened(mCamera!!, mCameraId, displayOrientation, isMirror)
            } catch (e: Exception) {
                if (cameraListener != null) {
                    cameraListener.onCameraError(e)
                } else {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun getCameraOri(rotation: Int): Int {
        var degrees = rotation * 90
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else -> {
            }
        }
        additionalRotation /= 90
        additionalRotation *= 90
        degrees += additionalRotation
        var result: Int
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(mCameraId, info)
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        return result
    }

    fun stop() {
        synchronized(this) {
            if (mCamera == null) {
                return
            }
            mCamera!!.setPreviewCallback(null)
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
            cameraListener?.onCameraClosed()
        }
    }

    private fun getBestSupportedSize(sizes: List<Camera.Size>?, previewViewSize: Point?): Camera.Size {
        var sizes = sizes
        if (sizes == null || sizes.size == 0) {
            return mCamera!!.parameters.previewSize
        }
        val tempSizes = sizes.toTypedArray()
        Arrays.sort(tempSizes) { o1, o2 ->
            if (o1.width > o2.width) {
                -1
            } else if (o1.width == o2.width) {
                if (o1.height > o2.height) -1 else 1
            } else {
                1
            }
        }
        sizes = Arrays.asList(*tempSizes)

        var bestSize: Camera.Size = sizes!![0]
        var previewViewRatio: Float
        if (previewViewSize != null) {
            previewViewRatio = previewViewSize.x.toFloat() / previewViewSize.y.toFloat()
        } else {
            previewViewRatio = bestSize.width.toFloat() / bestSize.height.toFloat()
        }

        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio
        }
        val isNormalRotate = additionalRotation % 180 == 0

        for (s in sizes) {
            if (specificPreviewSize != null && specificPreviewSize.x == s.width && specificPreviewSize.y == s.height) {
                return s
            }
            //预览界面高宽大于相机界面就行，比例和相机界面相似
            if (isNormalRotate) {
                if (Math.abs(s.height / s.width.toFloat() - previewViewRatio) <= Math.abs(bestSize.height / bestSize.width.toFloat() - previewViewRatio) && s.width >= previewViewSize!!.x && s.height >= previewViewSize.y) {
                    bestSize = s
                }
            } else {
                if (Math.abs(s.width / s.height.toFloat() - previewViewRatio) <= Math.abs(bestSize.width / bestSize.height.toFloat() - previewViewRatio) && s.width >= previewViewSize!!.x && s.height >= previewViewSize.y) {
                    bestSize = s
                }
            }
        }
        return bestSize
    }

    override fun onPreviewFrame(nv21: ByteArray, camera: Camera) {
        cameraListener?.onPreview(nv21, camera)
    }

    fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera!!.setPreviewTexture(surfaceTexture)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 方向改变
     *
     * @param rotation
     */
    fun changeDisplayOrientation(rotation: Int) {
        if (mCamera != null) {
            this.rotation = rotation
            displayOrientation = getCameraOri(rotation)
            mCamera!!.setDisplayOrientation(displayOrientation)
            cameraListener?.onCameraConfigurationChanged(mCameraId, displayOrientation)
        }
    }

    /**
     * 设置相机参数
     *
     * @param parameters
     */
    private fun setCameraParameters(parameters: Camera.Parameters): Boolean {
        try {
            mCamera!!.parameters = parameters
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    class Builder {

        /**
         * 预览显示的view，目前仅支持surfaceView和textureView
         */
        var previewDisplayView: TextureView? = null

        /**
         * 是否镜像显示，只支持textureView
         */
        var isMirror: Boolean = false
        /**
         * 指定的相机ID
         */
        var specificCameraId: Int? = null
        /**
         * 事件回调
         */
        var cameraListener: CameraControllerListener? = null
        /**
         * 屏幕的长宽，在选择最佳相机比例时用到
         */
        var previewViewSize: Point? = null
        /**
         * 传入getWindowManager().getDefaultDisplay().getRotation()的值即可
         */
        var rotation: Int = 0
        /**
         * 指定的预览宽高，若系统支持则会以这个预览宽高进行预览
         */
        var previewSize: Point? = null

        /**
         * 额外的旋转角度（用于适配一些定制设备）
         */
        var additionalRotation: Int = 0
        /**
         * 自动对焦
         */
        var autoFocusCallback: Camera.AutoFocusCallback? = null

        var faceDetectionListener: Camera.FaceDetectionListener? = null

        fun previewOn(`val`: TextureView): Builder {
            previewDisplayView = `val`
            return this
        }


        fun isMirror(`val`: Boolean): Builder {
            isMirror = `val`
            return this
        }

        fun previewSize(`val`: Point): Builder {
            previewSize = `val`
            return this
        }

        fun previewViewSize(`val`: Point): Builder {
            previewViewSize = `val`
            return this
        }

        fun rotation(`val`: Int): Builder {
            rotation = `val`
            return this
        }

        fun additionalRotation(`val`: Int): Builder {
            additionalRotation = `val`
            return this
        }

        fun specificCameraId(`val`: Int?): Builder {
            specificCameraId = `val`
            return this
        }

        fun cameraListener(`val`: CameraControllerListener): Builder {
            cameraListener = `val`
            return this
        }

        fun setAutoFocusCallback(focusCallback: Camera.AutoFocusCallback): Builder {
            autoFocusCallback = focusCallback
            return this
        }

        fun setFaceDetectionListener(faceDetectionListener: Camera.FaceDetectionListener): Builder {
            this.faceDetectionListener = faceDetectionListener
            return this
        }

        fun build(): CameraControllerHelper {
            if (previewViewSize == null) {
                Log.e(TAG, "previewViewSize is null, now use default previewSize")
            }
            if (cameraListener == null) {
                Log.e(TAG, "cameraListener is null, callback will not be called")
            }
            if (previewDisplayView == null) {
                throw RuntimeException("you must preview on a textureView or a surfaceView")
            }
            return CameraControllerHelper(this)
        }
    }

    companion object {
        private val TAG = "CameraHelper"
    }

}
