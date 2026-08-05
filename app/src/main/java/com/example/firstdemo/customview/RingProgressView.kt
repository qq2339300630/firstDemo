package com.example.firstdemo.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.min

/**
 * 自绘环形进度条 —— 经典自定义 View 实战。
 *
 * 把布局流程的三步里的两步跑通:
 *   onMeasure：处理测量,尤其让 wrap_content 生效(否则等于 match_parent)
 *   onDraw   ：用 Canvas/Paint 画底环 + 进度弧 + 居中百分比文字
 * (单个自绘 View 没有子 View,不用重写 onLayout。)
 *
 * 复习点:drawArc 描边、Paint.Cap.ROUND 圆头、FontMetrics 纵向居中、
 * onDraw 里不 new 对象(Paint/RectF 都提前建好)。
 */
class RingProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val strokeW = 14f * density
    private val defaultSize = (160 * density).toInt()   // wrap_content 时的默认边长

    /** 进度 0f~1f。改它就 invalidate 重绘(内容变、大小不变 → 只走绘制那一步)。 */
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    // ★ Paint / RectF 都在这里建好,onDraw 里绝不 new(onDraw 每帧可能跑很多次)
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeW
        color = Color.parseColor("#E0E0E0")
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeW
        strokeCap = Paint.Cap.ROUND       // 进度头圆角
        color = Color.parseColor("#2196F3")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        textAlign = Paint.Align.CENTER    // 水平居中交给 Paint
        textSize = 40f * density
    }
    private val arcRect = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // resolveSize 帮你处理三种模式:EXACTLY 用父给的、AT_MOST(wrap_content)取默认但不超上限
        setMeasuredDimension(
            resolveSize(defaultSize, widthMeasureSpec),
            resolveSize(defaultSize, heightMeasureSpec),
        )
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        // 半径要减掉一半描边宽,否则粗线会画出边界被裁
        val radius = (min(width, height) - strokeW) / 2f - 4f * density
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // 底环(整圈)
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)
        // 进度弧:从 12 点方向(-90°)开始,顺时针扫 360*progress 度
        canvas.drawArc(arcRect, -90f, 360f * progress, false, progressPaint)

        // 居中百分比文字:水平靠 Paint.Align.CENTER,纵向用 FontMetrics 居中
        val fm = textPaint.fontMetrics
        val baseline = cy - (fm.ascent + fm.descent) / 2f
        canvas.drawText("${(progress * 100).toInt()}%", cx, baseline, textPaint)
    }
}

@Preview(showBackground = true)
@Composable
fun RingProgressViewPreview() {
    AndroidView(
        factory = { context ->
            RingProgressView(context).apply {
                progress = 0.5f
            }
        },
        modifier = Modifier.size(200.dp)
    )
}
