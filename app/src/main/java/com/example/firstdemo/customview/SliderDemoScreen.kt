package com.example.firstdemo.customview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 承载自定义 SliderView。进度变化通过 onProgressChanged 回调同步到 Compose 显示百分比。
 */
@Composable
fun SliderDemoScreen(modifier: Modifier = Modifier) {
    var value by remember { mutableFloatStateOf(0.4f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("自定义滑块 · SliderView", style = MaterialTheme.typography.titleMedium)
        Text(
            "拖动把手改变进度。state 只存 progress(0~1),把手位置由它算出来。",
            style = MaterialTheme.typography.bodySmall,
        )

        AndroidView(
            factory = { ctx ->
                SliderView(ctx).apply {
                    progress = 0.4f
                    onProgressChanged = { value = it }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        )

        Text("当前进度:${(value * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
    }
}
