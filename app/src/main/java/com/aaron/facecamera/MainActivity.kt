package com.aaron.facecamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cvr.fswitcher.fragment.CameraFaceFm

class MainActivity : AppCompatActivity() {

    private lateinit var mBaseHandler:Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)

        mBaseHandler = Handler()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val checkWriteStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            //如果没有被授予
            if (checkWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE)
                Toast.makeText(this, "没有相机权限!", Toast.LENGTH_SHORT).show()
                return
            } else {
                addCameraFm()
            }
        } else {
            addCameraFm()
        }
    }

    private fun addCameraFm() {
        val mFragmentManager = supportFragmentManager
        //事务是不能共享的，每次用到都要重新开启一个事务，之后提交
        val fragmentTransaction = mFragmentManager.beginTransaction()
        //参数：1.父容器   2.要替换的fragment。
        fragmentTransaction.replace(R.id.fragment_layout, CameraFaceFm.newInstance())
        //提交事务
        fragmentTransaction.commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addCameraFm()
            } else {
                Toast.makeText(this, "获取权限失败!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        private val REQUEST_CODE = 123
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            mBaseHandler.removeCallbacks(mHideSystemUi)
            //dialog、软键盘弹出有可能造成导航栏重新显示，延迟隐藏
            mBaseHandler.postDelayed(mHideSystemUi, 300)
        } else {
            mBaseHandler.removeCallbacks(mHideSystemUi)
        }
        hideSystemBar()
    }

    private val mHideSystemUi = Runnable {
        hideSystemBar()
    }

    /**
     * 全屏显示
     */
    private fun hideSystemBar() {
        val decorView = window.decorView
        val uiOptions =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        decorView.systemUiVisibility = uiOptions
    }
}
