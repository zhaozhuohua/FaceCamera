package com.aaron.utils

import android.content.Context
import android.view.View
import android.widget.Toast

/**
 * Created by Aaron on 2019/08/21.
 *
 * 弹出toast提示工具类
 */
object ToastUtils {

    private var mToast: Toast? = null  //toast样式
    private val mMsg: String? = null  //上一次弹出的内容
    private var mToastGravity: Int = -1  //位置

    /**
     * 弹出提示
     * @param msg  提示信息
     * @param time  显示时间
     */
    fun showToast(msg: String?, time: Int, context: Context?) {
        if (mToast == null || mMsg != null && msg != mMsg) {
            mToast = Toast.makeText(context, msg, time)

            mToast!!.setText(msg)
        } else {
            mToast!!.setText(msg)
            mToast!!.duration = time
        }
        if (mToastGravity != -1) {
            mToast!!.setGravity(mToastGravity, 0, 0)
        }

        //不设置的话，最高显示到状态栏下面
        mToast!!.view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        mToast!!.show()
    }

    /**
     * 弹出提示信息
     * @param msgId  提示信息id
     * @param time  显示时间
     */
    fun showToast(msgId: Int, time: Int, context: Context?) {
        showToast(context?.getString(msgId), time, context)
    }

    /**
     * 弹出短时间提示
     * @param msg  提示信息
     */
    fun showShortToast(msg: String, context: Context?) {
        showToast(msg, Toast.LENGTH_SHORT, context)
    }

    fun showShortToast(msgId: Int, context: Context?) {
        showToast(msgId, Toast.LENGTH_SHORT, context)
    }

    /**
     * 弹出长时间提示
     * @param msg  提示信息
     */
    fun showLongToast(msg: String, context: Context?) {
        showToast(msg, Toast.LENGTH_LONG, context)
    }

    /**
     * 关闭当前Toast
     */
    fun cancelCurrentToast() {
        if (mToast != null) {
            mToast!!.cancel()
        }
    }

    fun setToastGravity(gravity: Int) {
        mToastGravity = gravity
    }

    /**
     * 重置toast 信息
     */
    fun resetToast() {
        mToastGravity = -1
        mToast = null
    }
}
