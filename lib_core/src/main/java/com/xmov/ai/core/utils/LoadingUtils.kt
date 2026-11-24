package com.xmov.ai.core.utils

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.xmov.ai.core.R
import com.xmov.ai.core.loading.CenterLoadingView

/**
 * 等待提示框
 */
class LoadingUtils(private val mContext: Context) {
    private var loadView: CenterLoadingView? = null

    /**
     * 统一耗时操作Dialog
     */
    fun showLoading(txt: String?) {
        if (loadView == null) {
            loadView = CenterLoadingView(mContext, R.style.dialog)
            // loadView.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        if (loadView?.isShowing == true) {
            loadView?.dismiss()
        }
        if (!TextUtils.isEmpty(txt)) {
            loadView?.setTitle(txt as CharSequence)
        }
        if (mContext is Activity && mContext.isFinishing) {
            return
        }
        loadView?.show()
    }

    fun setTitle(txt: String?) {
        if (loadView?.isShowing == true) {
            if (!TextUtils.isEmpty(txt)) {
                loadView?.setTitle(txt as CharSequence)
            }
        }
    }

    /**
     * 关闭Dialog
     */
    fun dismissLoading() {
        if (mContext is Activity && mContext.isFinishing) {
            return
        }

        loadView?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }
}
