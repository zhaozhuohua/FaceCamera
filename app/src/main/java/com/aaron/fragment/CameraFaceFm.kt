package com.cvr.fswitcher.fragment

import android.content.Context
import android.graphics.Point
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.aaron.camera.CameraControllerHelper
import com.aaron.camera.CameraControllerListener
import com.aaron.fragment.BaseFragment
import com.aaron.utils.DrawFaceHelper
import com.aaron.utils.ToastUtils
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fm_camera_face.*



/**
 * Created by Aaron on 2019/8/22.
 * 主界面和人脸识别操作界面
 */
open class CameraFaceFm : BaseFragment(),
        CameraControllerListener {

    override fun onCameraOpened(camera: Camera?, cameraId: Int, displayOrientation: Int, isMirror: Boolean) {
    }

    override fun onPreview(data: ByteArray?, camera: Camera?) {

    }

    override fun onCameraClosed() {
    }

    override fun onCameraError(e: java.lang.Exception?) {
        Logger.i("BaseCameraFm onCameraError: " + e!!.message)
    }

    override fun onCameraConfigurationChanged(cameraID: Int, displayOrientation: Int) {

    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        //一体机没有拍照按钮
    }

    override fun getRootLayoutId(): Int {
        return com.aaron.facecamera.R.layout.fm_camera_face
    }

    var cameraId = Camera.CameraInfo.CAMERA_FACING_BACK

    lateinit var cameraHelper: CameraControllerHelper  //相机控制类
    private lateinit var handler:Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler()
    }

    override fun loadView(view: View) {
    }

    override fun loadData(savedInstanceState: Bundle?) {
        super.loadData(savedInstanceState)
        camera_surfaceview.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                camera_surfaceview.viewTreeObserver.removeOnGlobalLayoutListener(this)
                Logger.i(
                        "basecamerafm  camera onGlobalLayout---- w: " + camera_surfaceview.measuredWidth
                                + "， h: " + camera_surfaceview.measuredHeight
                )
                if (!this@CameraFaceFm::cameraHelper.isInitialized || cameraHelper == null) {
                    //进行相机初始化
                    cameraHelper = CameraControllerHelper.Builder()
                            .previewViewSize(Point(camera_surfaceview.measuredWidth, camera_surfaceview.measuredHeight))
                            .rotation(mBaseAc.windowManager.defaultDisplay.rotation)
                            .specificCameraId(cameraId)
                            .isMirror(isMirror())
                            .previewOn(camera_surfaceview)
                            .cameraListener(this@CameraFaceFm)
                            //设置人脸检测回调方法
                            .setFaceDetectionListener(object : Camera.FaceDetectionListener {
                                override fun onFaceDetection(p0: Array<out Camera.Face>?, p1: Camera?) {
                                    if (faceRectView != null) {
                                        faceRectView.clearFaceInfo()
                                        faceRectView.addFaceInfo(p0!!.toList())
//                                        showFaceView.setFaces(p0 as Array<Camera.Face>?)
                                    }
                                }
                            })
                            .build()
                    val p = getScreenPoint()
                    DrawFaceHelper.cameraWidth = p.x.toFloat()
                    DrawFaceHelper.cameraHeight = p.y.toFloat()
                    DrawFaceHelper.isBackCameraId = isBackCameraId()

//                    showFaceView.setIsBack(isBackCameraId())

                    cameraHelper.init()
                    cameraHelper.start()
                }
            }

        })

        switchId.setOnClickListener {
            startSwitchAdmin()
            switchId.isEnabled = false
            handler.postDelayed({
                switchId.isEnabled = true
            }, 2000)
            cameraId = if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Camera.CameraInfo.CAMERA_FACING_BACK
            } else {
                Camera.CameraInfo.CAMERA_FACING_FRONT
            }
            DrawFaceHelper.isBackCameraId = isBackCameraId()
            showFaceView.setIsBack(isBackCameraId())
            faceRectView.clearFaceInfo()
            cameraHelper.setSpecificCameraId(cameraId)
            cameraHelper.stop()
            cameraHelper.start()
        }
    }

    private fun isBackCameraId():Boolean {
        return  cameraId == Camera.CameraInfo.CAMERA_FACING_BACK
    }

    private fun startSwitchAdmin() {
        val rotateAnimation = RotateAnimation(0F, 360F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5F)
        rotateAnimation.fillAfter = false
        rotateAnimation.fillBefore = true
        rotateAnimation.isFillEnabled = true
        rotateAnimation.duration = 1000
        switchId.startAnimation(rotateAnimation)
    }

    override fun onPause() {
        super.onPause()
        if (::cameraHelper.isInitialized) {
            cameraHelper.stop()
        }
    }

    /**
     * 是否镜像像是
     */
    private fun isMirror(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        if (isPause && ::cameraHelper.isInitialized) {
            cameraHelper.init()
            cameraHelper.start()
        }
        isPause = false
    }

    override fun showToast(info: String?) {
        ToastUtils.showShortToast(info!!, mBaseAc)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::cameraHelper.isInitialized) {
            cameraHelper.stop()
        }
    }

    companion object {

        fun newInstance(): CameraFaceFm {
            return CameraFaceFm()
        }
    }

    fun getScreenPoint():Point {
        val outSize = Point()
        val wm = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(outSize)
        return outSize
    }
}
