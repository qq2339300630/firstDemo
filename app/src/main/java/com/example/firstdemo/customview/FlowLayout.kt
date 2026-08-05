package com.example.firstdemo.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone

/**
 * 流式布局 FlowLayout —— 经典自定义 ViewGroup 实战。
 *
 * 效果:子 View 从左往右排,一行放不下就自动换到下一行(标签云那种)。
 * 它跑通布局流程里【和子 View 打交道】的两步:
 *   onMeasure：先测量每个子 View,再按换行规则累加出自己需要的总高
 *   onLayout ：用【完全相同】的换行规则,把每个子 View 摆到具体位置
 *
 * 关键:onMeasure 和 onLayout 的换行判断必须一致,否则"量出来的高"和"实际摆放"对不上。
 */
class FlowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    private val gap = (8 * resources.displayMetrics.density).toInt()  // 子 View 之间的间距

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        // ① 先让每个子 View 测量自己(按各自内容确定大小)
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        val maxWidth = widthSize - paddingLeft - paddingRight   // 一行可用宽度
        var lineWidth = 0     // 当前行已用宽(含尾随 gap)
        var lineHeight = 0    // 当前行最高子 View 的高
        var totalHeight = 0   // 累计总高
        var maxLineWidth = 0  // 最宽的一行(wrap_content 求自身宽用)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            val cw = child.measuredWidth
            val ch = child.measuredHeight

            // 放不下且当前行已有内容 → 换行
            if (lineWidth + cw > maxWidth && lineWidth > 0) {
                totalHeight += lineHeight + gap
                maxLineWidth = maxOf(maxLineWidth, lineWidth - gap)
                lineWidth = 0
                lineHeight = 0
            }
            lineWidth += cw + gap
            lineHeight = maxOf(lineHeight, ch)
        }
        // 别忘了最后一行
        totalHeight += lineHeight
        maxLineWidth = maxOf(maxLineWidth, lineWidth - gap)

        // 宽:EXACTLY 用父给的;否则用最宽那行(wrap_content)
        val finalWidth = if (widthMode == MeasureSpec.EXACTLY) widthSize
        else maxLineWidth + paddingLeft + paddingRight
        val finalHeight = totalHeight + paddingTop + paddingBottom

        setMeasuredDimension(finalWidth, resolveSize(finalHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val maxWidth = r - l - paddingLeft - paddingRight
        var x = paddingLeft
        var y = paddingTop
        var lineHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.isGone) continue
            val cw = child.measuredWidth
            val ch = child.measuredHeight

            // 换行条件和 onMeasure 保持一致
            if (x - paddingLeft + cw > maxWidth && x > paddingLeft) {
                x = paddingLeft
                y += lineHeight + gap
                lineHeight = 0
            }
            child.layout(x, y, x + cw, y + ch)   // ★ 摆到具体位置
            x += cw + gap
            lineHeight = maxOf(lineHeight, ch)
        }
    }
}
