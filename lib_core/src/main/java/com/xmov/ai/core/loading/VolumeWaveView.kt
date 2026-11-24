package com.xmov.ai.core.loading

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.use
import com.xmov.ai.core.R
import kotlin.math.max
import android.util.Log


class VolumeWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = "VolumeWaveView"

    // 画笔配置
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = defaultWaveColor
    }

    // 圆角矩形路径
    private val rectF = RectF()

    // 动画相关
    private val animators = mutableListOf<ValueAnimator>()
    private val barHeights = mutableListOf<Float>()
    private val barDelays = mutableListOf<Long>()

    // 波纹配置属性
    var waveColor: Int
        get() = paint.color
        set(value) {
            paint.color = value
            invalidate()
        }

    var barCount: Int = 5
        set(value) {
            field = max(1, value)
            resetBars()
            invalidate()
        }

    var barWidth: Float = 4.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var barSpacing: Float = 3.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var cornerRadius: Float = 2.dpToPx()
        set(value) {
            field = value
            invalidate()
        }

    var animationDuration: Int = 800
        set(value) {
            field = max(100, value)
            restartAnimation()
        }

    var maxAmplitude: Float = 0.9f
        set(value) {
            field = value.coerceIn(0.1f, 1.0f)
            invalidate()
        }

    var minAmplitude: Float = 0.1f
        set(value) {
            field = value.coerceIn(0.05f, maxAmplitude)
            invalidate()
        }

    private var isAnimating = false

    companion object {
        private val defaultWaveColor = Color.parseColor("#4CAF50")
    }

    init {
        Log.d(TAG, "初始化视图")
        // 读取自定义属性
        context.obtainStyledAttributes(attrs, R.styleable.VolumeWaveView).use {
            waveColor = it.getColor(R.styleable.VolumeWaveView_waveColor, defaultWaveColor)
            barCount = it.getInt(R.styleable.VolumeWaveView_barCount, barCount)
            barWidth = it.getDimension(R.styleable.VolumeWaveView_barWidth, barWidth)
            barSpacing = it.getDimension(R.styleable.VolumeWaveView_barSpacing, barSpacing)
            cornerRadius = it.getDimension(R.styleable.VolumeWaveView_cornerRadius, cornerRadius)
            animationDuration = it.getInt(R.styleable.VolumeWaveView_animationDuration, animationDuration)
            maxAmplitude = it.getFloat(R.styleable.VolumeWaveView_maxAmplitude, maxAmplitude)
            minAmplitude = it.getFloat(R.styleable.VolumeWaveView_minAmplitude, minAmplitude)
        }

        resetBars()
    }

    private fun resetBars() {
        Log.d(TAG, "重置波纹柱，数量: $barCount")
        barHeights.clear()
        barDelays.clear()

        repeat(barCount) { index ->
            barHeights.add(0f)
            // 随机延迟，避免同步
            barDelays.add((index * 100).toLong()) // 更有规律的延迟，便于调试
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 计算所需宽度
        val desiredWidth = (barCount * barWidth + (barCount - 1) * barSpacing).toInt()
        // 固定高度为48dp，确保可见
        val desiredHeight = 48.dpToPx().toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        Log.d(TAG, "测量尺寸: 宽=$width, 高=$height")
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "尺寸变化: 新宽=$w, 新高=$h")
        // 确保尺寸有效时才启动动画
        if (w > 0 && h > 0 && !isAnimating) {
            startAnimation()
        }
    }

    /**
     * 启动波纹动画（增加更多调试信息）
     */
    fun startAnimation() {
        if (isAnimating) {
            Log.d(TAG, "动画已在运行，无需重复启动")
            return
        }

        if (width <= 0 || height <= 0) {
            Log.w(TAG, "尺寸无效，无法启动动画")
            return
        }

        stopAnimation()

        val maxHeight = height * maxAmplitude
        val minHeight = height * minAmplitude
        Log.d(TAG, "启动动画: 最大高度=$maxHeight, 最小高度=$minHeight")

        barHeights.forEachIndexed { index, _ ->
            val animator = ValueAnimator.ofFloat(minHeight, maxHeight).apply {
                duration = animationDuration.toLong()
                startDelay = barDelays[index]
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE

                addUpdateListener { animation ->
                    barHeights[index] = animation.animatedValue as Float
                    invalidate()
                }
            }

            animators.add(animator)
            animator.start()
        }

        isAnimating = true
        Log.d(TAG, "动画启动完成")
    }

    /**
     * 停止波纹动画
     */
    fun stopAnimation() {
        animators.forEach { it.cancel() }
        animators.clear()
        isAnimating = false
        Log.d(TAG, "动画已停止")
    }

    /**
     * 重新启动动画
     */
    fun restartAnimation() {
        Log.d(TAG, "重新启动动画")
        stopAnimation()
        startAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (barHeights.isEmpty()) {
            Log.w(TAG, "没有波纹柱可绘制")
            return
        }

        // 计算总宽度和起始位置
        val totalWidth = barCount * barWidth + (barCount - 1) * barSpacing
        val startX = (width - totalWidth) / 2f
        val centerY = height / 2f

        Log.d(TAG, "绘制波纹: 总宽度=$totalWidth, 起始X=$startX, 中心Y=$centerY")

        // 绘制每条波纹柱
        barHeights.forEachIndexed { index, height ->
            val left = startX + index * (barWidth + barSpacing)
            val top = centerY - height / 2
            val right = left + barWidth
            val bottom = centerY + height / 2

            // 确保矩形在可见范围内
            if (left < right && top < bottom) {
                rectF.set(left, top, right, bottom)
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            } else {
                Log.w(TAG, "无效的矩形坐标: 左=$left, 右=$right, 上=$top, 下=$bottom")
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Log.d(TAG, "可见性变化: $visibility")
        when (visibility) {
            VISIBLE -> startAnimation()
            GONE, INVISIBLE -> stopAnimation()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "视图已附加到窗口")
        if (visibility == VISIBLE) {
            startAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "视图已从窗口分离")
        stopAnimation()
    }

    /**
     * dp转px工具方法（增加容错）
     */
    private fun Int.dpToPx(): Float {
        return try {
            this * context.resources.displayMetrics.density
        } catch (e: Exception) {
            Log.e(TAG, "dp转px失败", e)
            this.toFloat()
        }
    }

    private fun Float.dpToPx(): Float {
        return try {
            this * context.resources.displayMetrics.density
        } catch (e: Exception) {
            Log.e(TAG, "dp转px失败", e)
            this
        }
    }
}
