package com.aaron.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.aaron.utils.ToastUtils
import com.orhanobut.logger.Logger


/**
 * Created by Aaron on 2019/8/21.
 *
 * Fragment 基础类型
 */
open abstract class BaseFragment: Fragment() {
    open fun showToast(message: String?) {
        ToastUtils.showShortToast(message ?: "没有！！！！", context)
    }

    fun showErrorByCode(code: Float) {
        ToastUtils.showShortToast("$code", context)
    }

    fun showErrorById(resId: Int) {
        ToastUtils.showShortToast(resId, context)
    }

    fun showError(message: String) {
        ToastUtils.showShortToast(message, context)
    }

    fun showLongError(error: String) {
        ToastUtils.showShortToast(error, context)
    }

    fun showToast(message: String, duration: Int) {
        ToastUtils.showToast(message, duration, context)
    }

    protected var mFragmentManager: FragmentManager? = null
    protected lateinit var mBaseAc:Activity
    protected var isPause = false
    protected var isDestroyView = false  //是否销毁
    protected var fmRootView: View? = null
    var callbackFm:BaseFragment? = null  //回调的Fragment，拍照回调

    /**
     * 视图是否已经初初始化
     */
    protected open var isInit = false
    /**
     * 是否已经加载过数据
     */
    protected open var isLoad = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mBaseAc = activity!!
        Logger.i("onAttach. ${this.javaClass.canonicalName}")
    }

    override fun onDetach() {
        super.onDetach()
        Logger.i("onDetach. ${this.javaClass.canonicalName}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i("onDetonCreateach. ${this.javaClass.canonicalName}")

        mFragmentManager = childFragmentManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Logger.i("onCreateView. ${this.javaClass.canonicalName}")
        return createView(inflater, container, savedInstanceState)
    }

    protected open fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View? {
        Logger.i("createView. ${this.javaClass.canonicalName}")
        if (fmRootView == null) {
            getFmRootView()
        } else {
            //缓存的rootView需要判断是否已经被加过parent，
            //如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
            if (fmRootView!!.parent != null) {
                val parent = fmRootView!!.parent as ViewGroup
                parent?.removeView(fmRootView)
            }
        }

        return fmRootView
    }

    /**
     * 设置根节点view信息
     */
    protected open fun getFmRootView() {
        fmRootView = mBaseAc.layoutInflater.inflate(getRootLayoutId(), null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.i("onViewCreated. ${this.javaClass.canonicalName}")
        isDestroyView = false
        if (!isInit) {
            isInit = true
            //防止多层fragment 点击 穿透
            view.setOnTouchListener { v, event -> true }
            loadView(view)
            isCanLoadData(savedInstanceState)
        }
    }

    /**
     * 是否可以加载数据
     * 可以加载数据的条件：
     * 1.视图已经初始化
     * 2.视图对用户可见
     */
    private fun isCanLoadData(savedInstanceState: Bundle?) {
        if (!isInit) {
            return
        }

        if (userVisibleHint) {
            if (!isLoad) {
                loadData(savedInstanceState)
            }
            isLoad = true
        } else {
            if (isLoad) {
                stopLoad()
            }
        }
    }

    /**
     * 当视图已经对用户不可见并且加载过数据，如果需要在切换到其他页面时停止加载数据，可以调用此方法
     */
    protected fun stopLoad() {}

    /**
     * 获取布局id
     */
    open abstract fun getRootLayoutId():Int

    /**
     * 加载界面信息
     */
    open abstract fun loadView(view: View)

    /**
     * 当视图初始化并且对用户可见的时候去真正的加载数据
     */
    open fun loadData(savedInstanceState: Bundle?) {
    }

    override fun onPause() {
        super.onPause()
        isPause = true
        Logger.i("onPause. ${this.javaClass.canonicalName}")
    }

    override fun onResume() {
        super.onResume()
        Logger.i("onResume. ${this.javaClass.canonicalName}")
    }

    protected open fun finish() {
        //会产生白屏
//        val fragment = mBaseAc.supportFragmentManager.findFragmentByTag(fragmentTag)
//        if (fragment != null) {
//            mBaseAc.supportFragmentManager.beginTransaction().remove(fragment!!).commit()
//        }
        mBaseAc.onBackPressed()
    }

    override fun onDestroyView() {
        isInit = false
        isDestroyView = true
        super.onDestroyView()
        Logger.i("onDestroyView. ${this.javaClass.canonicalName}")

        //在这里移除view，会导致白屏
//        if (view != null) {
//            val parent = view!!.parent as ViewGroup
//            parent?.removeAllViews()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isLoad = false
        Logger.i("onDestroy. ${this.javaClass.canonicalName}")
    }

    fun isFmVisible():Boolean {
        return isAdded && isVisible && !isHidden
    }

    /**
     * Fragment 回调事件处理
     */
    open fun onFmResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    /**
     * Fragment 回调事件
     * @param requestCode 进行onActivityResult 第一个参数补全
     */
    open fun onCallbackFmResult(requestCode: Int, resultCode: Int, data: Intent?):Boolean {
        return if (callbackFm == null) {
            false
        } else {
            callbackFm?.onFmResult(requestCode, resultCode, data)
            true
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        showToast("isVisibleToUser: $isVisibleToUser, $this")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        showToast("hidden: $hidden, $this")
    }
}