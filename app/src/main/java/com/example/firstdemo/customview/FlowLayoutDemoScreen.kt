package com.example.firstdemo.customview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 用 AndroidView 把自定义 ViewGroup(FlowLayout)嵌进 Compose。
 * factory 里创建 FlowLayout 并塞进一堆标签 TextView,FlowLayout 会自动把它们换行排列。
 */
@Composable
fun FlowLayoutDemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("自定义 ViewGroup · 流式标签布局", style = MaterialTheme.typography.titleMedium)
        Text(
            "FlowLayout(onMeasure 量子View + onLayout 摆子View),放不下自动换行。",
            style = MaterialTheme.typography.bodySmall,
        )

        AndroidView(
            factory = { context ->
                FlowLayout(context).apply {
                    val p = (4 * resources.displayMetrics.density).toInt()
                    setPadding(p, p, p, p)
                    tagTexts.forEach { addView(makeTag(context, it)) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val tagTexts = listOf(
    "Kotlin", "Jetpack Compose", "自定义 View", "onMeasure", "onLayout",
    "流式布局", "Canvas", "Paint", "FontMetrics", "协程", "StateFlow",
    "Room", "Retrofit", "OkHttp", "MVVM", "SWR 缓存", "BlendMode", "Path.Op",
)

/** 造一个圆角标签 TextView(用 GradientDrawable 做圆角底色)。 */
private fun makeTag(context: Context, text: String): TextView {
    val d = context.resources.displayMetrics.density
    return TextView(context).apply {
        this.text = text
        setTextColor(Color.WHITE)
        textSize = 14f
        val padH = (12 * d).toInt()
        val padV = (6 * d).toInt()
        setPadding(padH, padV, padH, padV)
        background = GradientDrawable().apply {
            cornerRadius = 16 * d
            setColor(Color.parseColor("#2196F3"))
        }
    }
}
