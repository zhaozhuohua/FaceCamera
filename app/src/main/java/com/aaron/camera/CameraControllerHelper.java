package com.aaron.camera;

import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 相机辅助类
 */
public class CameraControllerHelper implements Camera.PreviewCallback {
    private static final String TAG = "CameraHelper";
    private Camera mCamera;
    private int mCameraId;
    private Point previewViewSize;
    private TextureView previewDisplayView;
    private Camera.Size previewSize;
    private Camera.Size pictureSize;
    private Point specificPreviewSize;
    private int displayOrientation = 0;  //设置预览方向
    private int rotation;  //屏幕方向
    private int additionalRotation;  //额外的旋转角度（用于适配一些定制设备）
    private boolean isMirror = false;
    private Camera.AutoFocusCallback autoFocusCallback;
    private Camera.FaceDetectionListener mFaceDetectionListener;

    private Integer specificCameraId = null;
    private CameraControllerListener cameraListener;

    private CameraControllerHelper(CameraControllerHelper.Builder builder) {
        previewDisplayView = builder.previewDisplayView;
        specificCameraId = builder.specificCameraId;
        cameraListener = builder.cameraListener;
        rotation = builder.rotation;
        additionalRotation = builder.additionalRotation;
        previewViewSize = builder.previewViewSize;
        specificPreviewSize = builder.previewSize;
        autoFocusCallback = builder.autoFocusCallback;
        mFaceDetectionListener = builder.faceDetectionListener;
        if (builder.previewDisplayView instanceof TextureView) {
            isMirror = builder.isMirror;
        } else if (isMirror) {
            throw new RuntimeException("mirror is effective only when the preview is on a textureView");
        }
    }

    public void setSpecificCameraId(int id) {
        specificCameraId = id;
    }

    public void init() {
        this.previewDisplayView.setSurfaceTextureListener(textureListener);

        if (isMirror) {
            previewDisplayView.setScaleX(-1);
        }
    }

    public void start() {
        synchronized (this) {
            if (mCamera != null) {
                return;
            }
            //若指定了相机ID且该相机存在，则打开指定的相机
            if (specificCameraId != null ) {
                mCameraId = specificCameraId;
            } else {
                //相机数量为2则打开1,1则打开0,相机ID 1为前置，0为后置
                mCameraId = Camera.getNumberOfCameras() - 1;
            }

            //没有相机
            if (mCameraId == -1) {
                if (cameraListener != null) {
                    cameraListener.onCameraError(new Exception("camera not found"));
                }
                return;
            }
            if (mCamera == null) {
                mCamera = Camera.open(mCameraId);
            }
            displayOrientation = getCameraOri(rotation);
            mCamera.setDisplayOrientation(displayOrientation);
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);

                //预览大小设置
                previewSize = parameters.getPreviewSize();
                List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
                if (supportedPreviewSizes != null && supportedPreviewSizes.size() > 0) {
                    previewSize = getCurrentPreviewSize();
                }

                parameters.setPreviewSize(previewSize.width, previewSize.height);

                pictureSize = parameters.getPictureSize();
                List<Camera.Size> supportedPicviewSizes = parameters.getSupportedPreviewSizes();
                if (supportedPicviewSizes != null && supportedPicviewSizes.size() > 0) {
                    pictureSize = getCurrentPictureSize();
                }
                parameters.setPictureSize(pictureSize.width, pictureSize.height);


                //对焦模式设置
                List<String> supportedFocusModes = parameters.getSupportedFocusModes();
                if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                    if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    }
                }
                setCameraParameters(parameters);
                mCamera.setPreviewTexture((previewDisplayView).getSurfaceTexture());
                mCamera.setPreviewCallback(this);
                mCamera.startPreview();
                if (mFaceDetectionListener != null) {
                    mCamera.setFaceDetectionListener(mFaceDetectionListener);
                    mCamera.startFaceDetection();
                }
                if (autoFocusCallback != null) {
                    mCamera.autoFocus(autoFocusCallback);
                }
                if (cameraListener != null) {
                    cameraListener.onCameraOpened(mCamera, mCameraId, displayOrientation, isMirror);
                }
            } catch (Exception e) {
                if (cameraListener != null) {
                    cameraListener.onCameraError(e);
                }
            }
        }
    }

    private Camera.Size getCurrentPictureSize() {
        List<Camera.Size> supportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
        if (supportedPictureSizes != null && supportedPictureSizes.size() > 0) {
            pictureSize = getBestSupportedSize(supportedPictureSizes, previewViewSize);
        }

        return pictureSize;
    }

    private Camera.Size getCurrentPreviewSize() {
        List<Camera.Size> supportedPretureSizes = mCamera.getParameters().getSupportedPreviewSizes();
        if (supportedPretureSizes != null && supportedPretureSizes.size() > 0) {
            previewSize = getBestSupportedSize(supportedPretureSizes, previewViewSize);
        }

        return previewSize;
    }

    private int getCameraOri(int rotation) {
        int degrees = rotation * 90;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }
        additionalRotation /= 90;
        additionalRotation *= 90;
        degrees += additionalRotation;
        int result;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public void stop() {
        synchronized (this) {
            if (mCamera == null) {
                return;
            }
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            if (cameraListener != null) {
                cameraListener.onCameraClosed();
            }
        }
    }

    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, Point previewViewSize) {
        if (sizes == null || sizes.size() == 0) {
            return mCamera.getParameters().getPreviewSize();
        }
        Camera.Size[] tempSizes = sizes.toArray(new Camera.Size[0]);
        Arrays.sort(tempSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                if (o1.width > o2.width) {
                    return -1;
                } else if (o1.width == o2.width) {
                    return o1.height > o2.height ? -1 : 1;
                } else {
                    return 1;
                }
            }
        });
        sizes = Arrays.asList(tempSizes);

        Camera.Size bestSize = sizes.get(0);
        float previewViewRatio;
        if (previewViewSize != null) {
            previewViewRatio = (float) previewViewSize.x / (float) previewViewSize.y;
        } else {
            previewViewRatio = (float) bestSize.width / (float) bestSize.height;
        }

        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio;
        }
        boolean isNormalRotate = (additionalRotation % 180 == 0);

        for (Camera.Size s : sizes) {
            if (specificPreviewSize != null && specificPreviewSize.x == s.width && specificPreviewSize.y == s.height) {
                return s;
            }
            //预览界面高宽大于相机界面就行，比例和相机界面相似
            if (isNormalRotate) {
                if ((Math.abs((s.height / (float) s.width) - previewViewRatio)
                        <= Math.abs(bestSize.height / (float) bestSize.width - previewViewRatio))
                        && (s.width >= previewViewSize.x && s.height >= previewViewSize.y)) {
                    bestSize = s;
                }
            } else {
                if ((Math.abs((s.width / (float) s.height) - previewViewRatio)
                        <= Math.abs(bestSize.width / (float) bestSize.height - previewViewRatio))
                        && (s.width >= previewViewSize.x && s.height >= previewViewSize.y)) {
                    bestSize = s;
                }
            }
        }
        return bestSize;
    }

    @Override
    public void onPreviewFrame(byte[] nv21, Camera camera) {
        if (cameraListener != null) {
            cameraListener.onPreview(nv21, camera);
        }
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setSurfaceTexture(surfaceTexture);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: " + width + "  " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            stop();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 方向改变
     *
     * @param rotation
     */
    public void changeDisplayOrientation(int rotation) {
        if (mCamera != null) {
            this.rotation = rotation;
            displayOrientation = getCameraOri(rotation);
            mCamera.setDisplayOrientation(displayOrientation);
            if (cameraListener != null) {
                cameraListener.onCameraConfigurationChanged(mCameraId, displayOrientation);
            }
        }
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    /**
     * 设置相机参数
     *
     * @param parameters
     */
    private boolean setCameraParameters(Camera.Parameters parameters) {
        try {
            mCamera.setParameters(parameters);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static final class Builder {

        /**
         * 预览显示的view，目前仅支持surfaceView和textureView
         */
        private TextureView previewDisplayView;

        /**
         * 是否镜像显示，只支持textureView
         */
        private boolean isMirror;
        /**
         * 指定的相机ID
         */
        private Integer specificCameraId;
        /**
         * 事件回调
         */
        private CameraControllerListener cameraListener;
        /**
         * 屏幕的长宽，在选择最佳相机比例时用到
         */
        private Point previewViewSize;
        /**
         * 传入getWindowManager().getDefaultDisplay().getRotation()的值即可
         */
        private int rotation;
        /**
         * 指定的预览宽高，若系统支持则会以这个预览宽高进行预览
         */
        private Point previewSize;

        /**
         * 额外的旋转角度（用于适配一些定制设备）
         */
        private int additionalRotation;
        /**
         * 自动对焦
         */
        private Camera.AutoFocusCallback autoFocusCallback;

        private Camera.FaceDetectionListener faceDetectionListener;

        public Builder() {
        }


        public Builder previewOn(TextureView val) {
            previewDisplayView = val;
            return this;
        }


        public Builder isMirror(boolean val) {
            isMirror = val;
            return this;
        }

        public Builder previewSize(Point val) {
            previewSize = val;
            return this;
        }

        public Builder previewViewSize(Point val) {
            previewViewSize = val;
            return this;
        }

        public Builder rotation(int val) {
            rotation = val;
            return this;
        }

        public Builder additionalRotation(int val) {
            additionalRotation = val;
            return this;
        }

        public Builder specificCameraId(Integer val) {
            specificCameraId = val;
            return this;
        }

        public Builder cameraListener(CameraControllerListener val) {
            cameraListener = val;
            return this;
        }

        public Builder setAutoFocusCallback(Camera.AutoFocusCallback focusCallback) {
            autoFocusCallback = focusCallback;
            return this;
        }

        public Builder setFaceDetectionListener(Camera.FaceDetectionListener faceDetectionListener) {
            this.faceDetectionListener = faceDetectionListener;
            return this;
        }

        public CameraControllerHelper build() {
            if (previewViewSize == null) {
                Log.e(TAG, "previewViewSize is null, now use default previewSize");
            }
            if (cameraListener == null) {
                Log.e(TAG, "cameraListener is null, callback will not be called");
            }
            if (previewDisplayView == null) {
                throw new RuntimeException("you must preview on a textureView or a surfaceView");
            }
            return new CameraControllerHelper(this);
        }
    }

}
