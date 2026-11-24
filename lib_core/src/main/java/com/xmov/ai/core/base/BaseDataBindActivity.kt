package com.xmov.ai.core.base

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.xmov.ai.core.ext.saveAs
import com.xmov.ai.core.ext.saveAsUnChecked
import java.lang.reflect.ParameterizedType

/**
 * @author fugang
 * @date   2025/2/20 12:34
 * @desc   dataBinding Activity基类
 */
abstract class BaseDataBindActivity<DB : ViewBinding> : BaseActivity() {
    lateinit var mBinding: DB

    override fun setContentLayout() {
//      mBinding = DataBindingUtil.setContentView(this, getLayoutResId())
        val type = javaClass.genericSuperclass
        val vbClass: Class<DB> = type!!.saveAs<ParameterizedType>().actualTypeArguments[0].saveAs()
        val method = vbClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        mBinding = method.invoke(this, layoutInflater)!!.saveAsUnChecked()
        setContentView(mBinding.root)
    }

    override fun getLayoutResId(): Int = 0
}