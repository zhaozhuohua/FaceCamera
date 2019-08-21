package com.cvr.fswitcher.fragment

import android.graphics.Point
import android.graphics.Rect
import android.hardware.Camera
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import com.aaron.camera.CameraControllerHelper
import com.aaron.camera.CameraControllerListener
import com.aaron.facecamera.R
import com.aaron.fragment.BaseFragment
import com.aaron.utils.CameraUtils
import com.aaron.utils.DrawFaceHelper
import com.aaron.utils.ToastUtils
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.fm_camera_face.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by Aaron on 2019/7/3.
 * 主界面和人脸识别操作界面
 */
open class CameraFaceFm : BaseFragment(),
        CameraControllerListener {

    override fun onCameraOpened(camera: Camera?, cameraId: Int, displayOrientation: Int, isMirror: Boolean) {
        val previewSize = camera?.parameters?.previewSize
        startScanFace()
        var size = CameraUtils.updateTextureViewSize(
                camera_surfaceview.width,
                camera_surfaceview.height, previewSize!!.width, previewSize!!.height
        )

        //固定相机预览界面大小，不经心重新设置
//            setLayoutParams(camera_surfaceview, size.width, size.height)
//            setLayoutParams(faceRectView, size.width, size.height)
//            setLayoutParams(hint_head_layout, size.width, size.height)

        //重新设置的预览界面和相机预览界面比例
        previewRatio = CameraUtils.updateTextureViewRatio(
                camera_surfaceview.width,
                camera_surfaceview.height, cameraHelper.previewSize.width,
                cameraHelper.previewSize.height
        )

        //设置相机预览界面中心点位置
        centerCoordinate.set(size.width / 2, size.height / 2)
    }

    private fun setLayoutParams(view: View, width: Int, height: Int) {
        val params = view.layoutParams
        params.width = width
        params.height = height
        view.layoutParams = params
    }

    override fun onPreview(data: ByteArray?, camera: Camera?) {

    }

    override fun onCameraClosed() {
        Logger.i("BaseCameraFm onCameraClosed")
        stopScanFace()
    }

    override fun onCameraError(e: java.lang.Exception?) {
        Logger.i("BaseCameraFm onCameraError: " + e!!.message)
    }

    override fun onCameraConfigurationChanged(cameraID: Int, displayOrientation: Int) {

    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        //一体机没有拍照按钮
    }

    /**
     * 获取根据界面转换过的Rect
     */
    private fun getFaceRect(faceInfo: Camera.Face): Rect {
        return Rect(
                (faceInfo.rect.left * previewRatio).toInt(), (faceInfo.rect.top * previewRatio).toInt(),
                (faceInfo.rect.right * previewRatio).toInt(), (faceInfo.rect.bottom * previewRatio).toInt()
        )
    }

    override fun getRootLayoutId(): Int {
        return R.layout.fm_camera_face
    }

    var previewRate = 1.33f

    private lateinit var centerCoordinate: Point  //相机预览界面中心点位置

    lateinit var cameraHelper: CameraControllerHelper  //相机控制类

    protected open var previewRatio = -1f  //TextureView与相机预览界面比例
    protected open var isStartFaceInfo = false  //是否开始人脸识别

    private var isStopScanFace = false  //停止扫描人脸
    private var mExecutor: ExecutorService? = null  //处理人脸识别检测线程池

    /**
     * 是否打开前置摄像头
     * @return
     */
    protected open val isFrontCamera: Boolean
        get() = isAdminLogin

    protected open val isAdminLogin: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        centerCoordinate = Point()
    }

    override fun loadView(view: View) {
        isStartFaceInfo = false

        initViewParams()
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
                            .specificCameraId(getCameraId())
                            .isMirror(isMirror())
                            .previewOn(camera_surfaceview)
                            .cameraListener(this@CameraFaceFm)
                            .setFaceDetectionListener(object : Camera.FaceDetectionListener {
                                override fun onFaceDetection(p0: Array<out Camera.Face>?, p1: Camera?) {
                                    showFaceView.setIsBack(true)
                                    showFaceView.setFaces(p0)
                                    //根据不同摄像头调整
                                    showFaceView.setRotateDegree(90)
                                    if (faceRectView != null) {
                                        faceRectView.clearFaceInfo()
                                        faceRectView.addFaceInfo(p0!!.toList())
                                    }
                                }
                            })
                            .build()
                    DrawFaceHelper.cameraWidth = camera_surfaceview.measuredWidth.toFloat()
                    DrawFaceHelper.cameraHeight = camera_surfaceview.measuredHeight.toFloat()
                    cameraHelper.init()
                    cameraHelper.start()
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        if (::cameraHelper.isInitialized) {
            cameraHelper.stop()
        }
    }

    /**
     * 开始识别人脸
     */
    private fun startScanFace() {
        isStopScanFace = false
        stopScanFace()
        mExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * 停止识别人脸
     */
    private fun stopScanFace() {
        isStopScanFace = true
        if (mExecutor != null) {
            mExecutor!!.shutdownNow()
            mExecutor = null
        }
    }

    /**
     * 是否镜像像是
     */
    private fun isMirror(): Boolean {
        return false
    }

    /**
     * 获取相机id
     */
    private fun getCameraId(): Int {
        return Camera.CameraInfo.CAMERA_FACING_BACK
    }

    override fun onResume() {
        super.onResume()
        if (isPause && ::cameraHelper.isInitialized) {
            cameraHelper.init()
            cameraHelper.start()
        }
        isPause = false
    }

    protected fun initViewParams() {
//        previewRate = MultiDeviceUtils.getFmScreenRate(
//            (mBaseAc as BaseRootAc).fmLayoutWidth.toFloat(),
//            (mBaseAc as BaseRootAc).fmLayoutHeight.toFloat(), !isFrontCamera
//        )
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

        fun newInstance(status: Int): CameraFaceFm {

            val args = Bundle()
            val fragment = CameraFaceFm()
            fragment.arguments = args
            return fragment
        }
    }
}
