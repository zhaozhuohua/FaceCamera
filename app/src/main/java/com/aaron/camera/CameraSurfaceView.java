package com.aaron.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

public class CameraSurfaceView extends AutoFitTextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback{

    private final static String TAG = "CameraSurfaceView";

    Context mContext;
    SurfaceTexture mSurfaceHolder;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setSurfaceTextureListener(this);
    }

    public SurfaceTexture getSurfaceHolder(){
        return mSurfaceHolder;
    }

    public void setSurfaceHolder(SurfaceTexture surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable...");
        mSurfaceHolder = surface;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged...");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed...");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureUpdated...");
    }

    /***
     * 想在这里做一个监听处理 收五侦 传输出去
     * @param bytes
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
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
}
