package com.aaron.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import com.aaron.camera.AutoFitTextureView

class CameraSurfaceView(internal var mContext: Context, attrs: AttributeSet) : AutoFitTextureView(mContext, attrs), TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    lateinit var surfaceHolder: SurfaceTexture

    init {
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.i(TAG, "onSurfaceTextureAvailable...")
        surfaceHolder = surface
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.i(TAG, "onSurfaceTextureSizeChanged...")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.i(TAG, "onSurfaceTextureDestroyed...")
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        Log.i(TAG, "onSurfaceTextureUpdated...")
    }

    /***
     * 想在这里做一个监听处理 收五侦 传输出去
     * @param bytes
     * @param camera
     */
    override fun onPreviewFrame(bytes: ByteArray, camera: Camera) {
        //        if(number<FACENUM){
        //            //收集
        //            //判断监听器 开始
        //            if(onFaceCollectListener!=null){
        //                onFaceCollectListener.OnFaceCollectStart(true);
        //                //有byte数组转为bitmap
        //                bitmaps[number] = cameraByte2bitmap(bytes,camera);
        //                Log.d(TAG,"********收集了"+number+"个************");
        //                number = number+1;
        //                if(number==5){
        //                    Log.d(TAG,"********收集够5个************");
        //                    //提交
        //                    onFaceCollectListener.OnFaceCollected(bitmaps);
        //
        //                    //此处添加一个循环检测
        ////                    onFaceCollectListener.OnFaceCollectStart(true);
        ////                    number = 0;
        //                }
        //            }
        //        }else {
        //            //不做操作
        //            onFaceCollectListener.OnFaceCollectStart(false);
        //        }
    }

    companion object {

        private val TAG = "CameraSurfaceView"
    }
}
