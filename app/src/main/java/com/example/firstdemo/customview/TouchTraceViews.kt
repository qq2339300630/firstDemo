package com.example.firstdemo.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/** 把 MotionEvent 的动作转成短名字。 */
private fun actionName(ev: MotionEvent): String = when (ev.actionMasked) {
    MotionEvent.ACTION_DOWN -> "DOWN"
    MotionEvent.ACTION_MOVE -> "MOVE"
    MotionEvent.ACTION_UP -> "UP"
    MotionEvent.ACTION_CANCEL -> "CANCEL"
    else -> "OTHER"
}

/**
 * 外层容器:重写三个方法并打日志。
 * interceptMove=true 时,在 MOVE 阶段拦截 → 子 View 会收到 CANCEL,之后事件归自己。
 */
class TouchTraceGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    var logger: ((String) -> Unit)? = null
    var interceptMove = false

    // 每个 prefix 每次手势只记一条 MOVE,避免刷屏
    private val movedPrefixes = HashSet<String>()
    private fun log(prefix: String, ev: MotionEvent, extra: String = "") {
        val a = ev.actionMasked
        if (a == MotionEvent.ACTION_DOWN) movedPrefixes.clear()
        if (a == MotionEvent.ACTION_MOVE && !movedPrefixes.add(prefix)) return
        logger?.invoke("$prefix ${actionName(ev)}$extra")
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        log("Group.dispatch", ev)
        return super.dispatchTouchEvent(ev)   // 内部会调 onInterceptTouchEvent / 子View / onTouchEvent
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercept = interceptMove && ev.actionMasked == MotionEvent.ACTION_MOVE
        log("Group.intercept", ev, " → $intercept")
        return intercept
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        log("Group.onTouch", ev)
        return true   // 拦下来后必须消费,否则后续事件收不到
    }
}

/**
 * 内层子 View:clickable 所以 onTouchEvent 默认消费。重写打日志。
 */
class TouchTraceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var logger: ((String) -> Unit)? = null

    init {
        isClickable = true   // 可点击 → onTouchEvent 默认返回 true 消费事件
    }

    private val movedPrefixes = HashSet<String>()
    private fun log(prefix: String, ev: MotionEvent) {
        val a = ev.actionMasked
        if (a == MotionEvent.ACTION_DOWN) movedPrefixes.clear()
        if (a == MotionEvent.ACTION_MOVE && !movedPrefixes.add(prefix)) return
        logger?.invoke("$prefix ${actionName(ev)}")
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        log("  View.dispatch", ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        log("  View.onTouch", ev)
        return super.onTouchEvent(ev)   // clickable → 消费
    }
}
