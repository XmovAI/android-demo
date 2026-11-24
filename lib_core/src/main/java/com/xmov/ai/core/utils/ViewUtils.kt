package com.xmov.ai.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * View相关处理工具类
 */
object ViewUtils {

    /**
     * 设置View圆角
     */
    fun setClipViewCornerRadius(view: View?, radius: Int) {
        if (view == null) {
            return
        }
        if (radius > 0) {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, 0, view?.width ?: 0, view?.height ?: 0, radius.toFloat())
                }
            }
            view.clipToOutline = true
        } else {
            view.clipToOutline = false
        }
    }

    /**
     * 设置View顶部圆角
     */
    fun setClipViewCornerTopRadius(view: View?, radius: Int) {
        if (view == null) {
            return
        }
        if (radius > 0) {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, 0, view?.width ?: 0, view?.height?.plus(radius) ?: 0, radius.toFloat())
                }
            }
            view.clipToOutline = true
        } else {
            view.clipToOutline = false
        }
    }

    /**
     * 设置View底部圆角
     */
    fun setClipViewCornerBottomRadius(view: View?, radius: Int) {
        if (view == null) {
            return
        }
        if (radius > 0) {
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, -radius, view?.width ?: 0, view?.height ?: 0, radius.toFloat())
                }
            }
            view.clipToOutline = true
        } else {
            view.clipToOutline = false
        }
    }

    /**
     * 设置透明沉浸式状态栏
     * @param activity 页面
     * @param isDarkText 是否状态栏文字显示深色
     */
    fun transparentBar(activity: Activity, isDarkText: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity?.window
            window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.statusBarColor = Color.TRANSPARENT
            var systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            if (isDarkText && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            window?.decorView?.systemUiVisibility = systemUiVisibility
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = activity?.window
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
    }

    @SuppressLint("ServiceCast")
    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        view?.let {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    /**
     * 动态设置View的宽高
     * @param view 需要设置的View
     * @param width 宽度，单位px
     * @param height 高度，单位px
     */
    fun setViewSize(view: ViewGroup, width: Int, height: Int) {
        val params = view.layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.width = width
        params.height = height
        view.layoutParams = params

        // 强制刷新布局
        view.requestLayout()
    }

    /**
     * 允许View超出父容器边界
     * @param parent 父容器
     */
    fun allowViewOverflow(parent: ViewGroup) {
        parent.clipChildren = false
        parent.clipToPadding = false
        // 如果有多层父容器，可能需要递归设置
        (parent.parent as? ViewGroup)?.let { allowViewOverflow(it) }
    }

    fun getScreenDimensions(context: Context): IntArray {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.getDefaultDisplay().getRealMetrics(metrics)
        return intArrayOf(
            metrics.widthPixels,
            metrics.heightPixels - getBottomNavBarHeight(context)
        )
    }

    /**
     * 获取底部导航区域高度（虚拟按键/手势提示线，px）
     * 适配Android 10+（API 29+）和低版本
     */
    private fun getBottomNavBarHeight(context: Context): Int {
        // 低版本（Android 9及以下）：通过尺寸差计算
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            // 总高度（含导航栏）
            val realSize = Point()
            display.getRealSize(realSize)
            // 可见高度（不含导航栏）
            val visibleSize = Point()
            display.getSize(visibleSize)
            // 导航栏高度 = 总高度 - 可见高度
            realSize.y - visibleSize.y
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }


}