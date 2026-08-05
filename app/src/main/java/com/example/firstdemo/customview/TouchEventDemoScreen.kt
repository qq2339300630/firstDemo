package com.example.firstdemo.customview

import android.graphics.Color
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 触摸事件分发追踪器:在蓝色小块上拖动,实时打印 dispatch/intercept/onTouch 的调用链。
 * 打开"父容器拦截 MOVE"后,子 View 会在 MOVE 时收到 CANCEL —— 直观看到中途拦截。
 */
@Composable
fun TouchEventDemoScreen(modifier: Modifier = Modifier) {
    val logs = remember { mutableStateListOf<String>() }
    var intercept by remember { mutableStateOf(false) }

    val append: (String) -> Unit = { line ->
        logs.add(line)
        if (logs.size > 16) logs.removeAt(0)   // 只留最近 16 条
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("触摸事件分发 · 事件流追踪", style = MaterialTheme.typography.titleMedium)
        Text(
            "在蓝色小块上短距离慢拖,看下方日志的分发顺序(缩进的是子 View)。",
            style = MaterialTheme.typography.bodySmall,
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("父容器拦截 MOVE(打开后子 View 收到 CANCEL)", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = intercept, onCheckedChange = { intercept = it })
        }

        // 外层 TouchTraceGroup 里放一个居中的 TouchTraceView
        AndroidView(
            factory = { ctx ->
                TouchTraceGroup(ctx).apply {
                    logger = append
                    setBackgroundColor(Color.parseColor("#FFF3E0"))   // 外层浅橙
                    val d = resources.displayMetrics.density
                    addView(
                        TouchTraceView(ctx).apply {
                            logger = append
                            setBackgroundColor(Color.parseColor("#2196F3"))  // 内层蓝
                        },
                        FrameLayout.LayoutParams(
                            (220 * d).toInt(),
                            (90 * d).toInt(),
                        ).apply { gravity = Gravity.CENTER },
                    )
                }
            },
            update = { group -> group.interceptMove = intercept },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("事件日志", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { logs.clear() }) { Text("清空") }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            logs.forEach { line ->
                Text(line, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}
