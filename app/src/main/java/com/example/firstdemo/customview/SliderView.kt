package com.example.firstdemo.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

/**
 * 自定义滑块 —— 严格照"四问拆解"来写。
 *
 * ① 零件：灰轨道 + 蓝色已选 + 把手(白实心圆 + 蓝圈)
 * ② 状态：只有一个 progress(0f~1f)。★ 把手 x、蓝色长度都从它算,不单独存像素坐标。
 * ③ 输入→状态：DOWN 记偏移;MOVE 把 手指x 换算成 progress;invalidate
 * ④ 按状态画：onDraw 只负责按当前 progress 画,绝不改 progress
 */
class SliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val knobRadius = 16f * density
    private val trackH = 6f * density
    private val defaultHeight = (48 * density).toInt()

    /** ② 唯一状态。setter 里夹边界 + 回调 + 重绘,外部无法把它设成非法值。 */
    var progress: Float = 0f
        set(value) {
            val v = value.coerceIn(0f, 1f)
            if (v != field) {
                field = v
                onProgressChanged?.invoke(v)
                invalidate()
            }
        }
    var onProgressChanged: ((Float) -> Unit)? = null

    // 轨道两端留出把手半径,免得把手画到边界外被裁
    private val trackLeft get() = paddingLeft + knobRadius
    private val trackRight get() = width - paddingRight - knobRadius
    private val trackWidth get() = (trackRight - trackLeft).coerceAtLeast(1f)
    private val centerY get() = height / 2f

    // ③④ 共用的换算:进度 → 把手 x(就是 screen = trans + scale×content 的同款映射)
    private fun knobX() = trackLeft + progress * trackWidth

    private var dragOffset = 0f   // 按下时手指与把手中心的偏移,让拖动"跟手不跳"

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = trackH
        color = Color.parseColor("#B0BEC5")
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = trackH
        color = Color.parseColor("#2196F3")
    }
    private val knobFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val knobStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f * density
        color = Color.parseColor("#2196F3")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),   // 宽用父给的
            resolveSize(defaultHeight, heightMeasureSpec),  // 高 wrap 用默认
        )
    }

    override fun onDraw(canvas: Canvas) {
        val y = centerY
        val kx = knobX()
        // 灰色全长轨道
        canvas.drawLine(trackLeft, y, trackRight, y, trackPaint)
        // 蓝色已选(从左端画到把手)
        canvas.drawLine(trackLeft, y, kx, y, fillPaint)
        // 把手:白实心 + 蓝描边
        canvas.drawCircle(kx, y, knobRadius, knobFill)
        canvas.drawCircle(kx, y, knobRadius, knobStroke)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 拖动期间别让外层容器(滚动)拦走事件 —— 呼应事件分发那节
                parent?.requestDisallowInterceptTouchEvent(true)
                val kx = knobX()
                // 按在把手附近 → 记偏移跟手;否则(点空轨道)→ 直接跳到手指处
                dragOffset = if (abs(e.x - kx) <= knobRadius * 1.6f) e.x - kx else 0f
                updateProgress(e.x)
            }
            MotionEvent.ACTION_MOVE -> updateProgress(e.x)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                parent?.requestDisallowInterceptTouchEvent(false)
        }
        return true
    }

    /** ③ 手指 x → progress(反算),赋值给 progress(setter 自动夹边界 + 重绘)。 */
    private fun updateProgress(touchX: Float) {
        progress = (touchX - dragOffset - trackLeft) / trackWidth
    }
}
