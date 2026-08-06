package com.example.firstdemo.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import androidx.core.graphics.toColorInt

/**
 * 综合手势 View:双向拖动 + 双指缩放 + 双击放大 + 惯性甩动。
 *
 * 内容 = 用 Canvas 画的 1200×1200「内容坐标系」网格;
 * onDraw 里 translate(平移) + scale(缩放)把内容坐标映射到屏幕。
 *
 * 手势来源:
 *   GestureDetector      → onScroll(拖) / onFling(甩) / onDoubleTap(双击)
 *   ScaleGestureDetector → onScale(双指缩放)
 *   OverScroller         → fling 的减速动画(computeScroll 每帧推进)
 */
class PanZoomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val content = 1200f          // 内容边长(内容坐标系)
    private val minScale = 0.5f
    private val maxScale = 4f

    // 当前状态:缩放 + 平移偏移
    private var scale = 1f
    private var transX = 0f
    private var transY = 0f

    private val scroller = OverScroller(context)

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = "#455A64".toColorInt()
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 44f
    }
    private val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#333333".toColorInt()
        textSize = 36f
    }
    private val colors = intArrayOf(
        "#EF9A9A".toColorInt(), "#90CAF9".toColorInt(),
        "#A5D6A7".toColorInt(), "#FFCC80".toColorInt(),
        "#CE93D8".toColorInt(), "#80CBC4".toColorInt(),
    )

    // ── 缩放手势 ──
    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(d: ScaleGestureDetector): Boolean {
                val newScale = (scale * d.scaleFactor).coerceIn(minScale, maxScale)
                // 以双指焦点为中心缩放:让焦点在屏幕上的位置保持不动
                transX = d.focusX - (d.focusX - transX) * (newScale / scale)
                transY = d.focusY - (d.focusY - transY) * (newScale / scale)
                scale = newScale
                clamp()
                invalidate()
                return true
            }
        },
    )

    // ── 拖动 / 甩动 / 双击 ──
    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                scroller.forceFinished(true)   // 手指按下先停掉惯性
                return true                    // ★ 必须 true,否则收不到后续手势
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dX: Float, dY: Float): Boolean {
                if (scaleDetector.isInProgress) return false  // 缩放中不平移
                transX -= dX;
                transY -= dY     // 直接跟手(translate 方案下 -distance)
                clamp();
                invalidate()
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (scaleDetector.isInProgress) return false
                val sw = (content * scale).toInt()
                val sh = (content * scale).toInt()
                val minX = if (sw <= width) transX.toInt() else width - sw
                val maxX = if (sw <= width) transX.toInt() else 0
                val minY = if (sh <= height) transY.toInt() else height - sh
                val maxY = if (sh <= height) transY.toInt() else 0
                scroller.fling(transX.toInt(), transY.toInt(), vX.toInt(), vY.toInt(), minX, maxX, minY, maxY)
                postInvalidateOnAnimation()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val target = if (scale < 1.5f) 2f else 1f   // 双击在 1x / 2x 间切换
                val newScale = target.coerceIn(minScale, maxScale)
                transX = e.x - (e.x - transX) * (newScale / scale)
                transY = e.y - (e.y - transY) * (newScale / scale)
                scale = newScale
                clamp(); invalidate()
                return true
            }
        },
    )

    override fun onTouchEvent(e: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(e)     // 两个检测器都喂
        gestureDetector.onTouchEvent(e)
        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {   // 推进 fling 动画
            transX = scroller.currX.toFloat()
            transY = scroller.currY.toFloat()
            postInvalidateOnAnimation()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        clamp()   // 尺寸确定后校正偏移(内容比视图小则居中)
    }

    /** 把平移限制在合理范围:内容比视图大→不露白边;比视图小→居中。 */
    private fun clamp() {
        val sw = content * scale
        val sh = content * scale
        transX = if (sw <= width) {
            (width - sw) / 2f
        } else {
            transX.coerceIn(width - sw, 0f)
        }
        transY = if (sh <= height) (height - sh) / 2f else transY.coerceIn(height - sh, 0f)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(transX, transY)   // 先平移
        canvas.scale(scale, scale)         // 再缩放,之后按内容坐标画
        drawContent(canvas)
        canvas.restore()

        // HUD:当前缩放倍数(画在屏幕固定位置,不受变换影响)
        canvas.drawText("缩放 ×%.1f  ·  拖动/双指缩放/双击/甩动".format(scale), 24f, 48f, hudPaint)
    }

    private fun drawContent(canvas: Canvas) {
        val cell = 200f
        val n = (content / cell).toInt()   // 6
        val fm = textPaint.fontMetrics
        for (row in 0 until n) {
            for (col in 0 until n) {
                val left = col * cell
                val top = row * cell
                cellPaint.color = colors[(row + col) % colors.size]
                canvas.drawRect(left, top, left + cell, top + cell, cellPaint)
                val cx = left + cell / 2
                val cy = top + cell / 2 - (fm.ascent + fm.descent) / 2f
                canvas.drawText("${row * n + col}", cx, cy, textPaint)
            }
        }
        canvas.drawRect(0f, 0f, content, content, borderPaint)
    }
}
