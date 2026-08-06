package com.example.firstdemo.customview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 用 AndroidView 承载综合手势 View(PanZoomView)。
 * 拖动 = 双向平移;双指 = 缩放;双击 = 1x/2x 切换;快滑松手 = 惯性甩动。
 */
@Composable
fun PanZoomDemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("综合手势 · 双向拖动 + 缩放", style = MaterialTheme.typography.titleMedium)
        Text(
            "拖动平移 / 双指缩放 / 双击放大 / 快滑甩动(GestureDetector + ScaleGestureDetector + OverScroller)",
            style = MaterialTheme.typography.bodySmall,
        )
        AndroidView(
            factory = { ctx -> PanZoomView(ctx) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}
