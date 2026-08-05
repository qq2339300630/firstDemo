package com.example.firstdemo.customview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 用 AndroidView 把经典自定义 View(RingProgressView)嵌进 Compose。
 *
 * AndroidView:
 *   factory —— 只创建一次 View(相当于 new)
 *   update  —— 每次相关状态变化时回调,这里把 progress 同步给 View
 *              → 触发 View 的 setter → invalidate() → 只重绘(布局流程第③步)
 *
 * 拖 Slider 改 progress,就能看到经典 View 的 invalidate 重绘在工作。
 */
@Composable
fun CustomViewDemoScreen(modifier: Modifier = Modifier) {
    var progress by remember { mutableFloatStateOf(0.35f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        Text("自定义 View · 环形进度条", style = MaterialTheme.typography.titleMedium)
        Text(
            "经典 View(onMeasure + onDraw),用 AndroidView 嵌进 Compose",
            style = MaterialTheme.typography.bodySmall,
        )

        AndroidView(
            factory = { context -> RingProgressView(context) },  // 创建一次
            update = { view -> view.progress = progress },        // progress 变 → invalidate 重绘
            modifier = Modifier.size(200.dp),
        )

        Text("拖动改变进度:${(progress * 100).toInt()}%")
        Slider(value = progress, onValueChange = { progress = it })
    }
}
