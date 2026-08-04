package com.example.firstdemo.canvasstudy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Canvas + Path 绘制 Demo：把"方向 / FillType / Path.Op"三样规则画出来看效果。
 *
 * 关键 API：
 *   Canvas(Modifier) { ... }  —— lambda 的 receiver 是 DrawScope，可用 drawPath/drawArc
 *   Path().apply { addOval/addRect(...); fillType = ...; op(a, b, ...) }
 *   drawPath(path, color)     —— 把路径填色画出来
 */
@Composable
fun PathDemoScreen(modifier: Modifier = Modifier) {
    // 颜色要在 Composable 作用域里取好，再被下面的 draw lambda 捕获
    // （DrawScope 里访问不到 MaterialTheme）。
    val fill = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Canvas + Path：方向 / FillType / Path.Op",
            style = MaterialTheme.typography.titleMedium,
        )

        // ① 默认 NonZero + 内外同向 → 中间被填满，画不出洞（新手最常见的坑）
        ShapeCell("① NonZero · 同向", "内外圈同向 → 填满，没有洞") {
            val r = radius()
            val p = Path().apply {
                // fillType 默认就是 NonZero；addOval 默认方向也相同
                addOval(Rect(center, r))
                addOval(Rect(center, r * 0.55f))
            }
            drawPath(p, fill)
        }

        // ② NonZero + 内圈反向 → 环形（靠方向相反让中心环绕数抵消为 0）
        ShapeCell("② NonZero · 反向", "内圈改成反方向 → 环形") {
            val r = radius()
            val p = Path().apply {
                addOval(Rect(center, r), Path.Direction.CounterClockwise)
                addOval(Rect(center, r * 0.55f), Path.Direction.Clockwise) // ← 反向
            }
            drawPath(p, fill)
        }

        // ③ EvenOdd → 环形，且不用管方向（奇偶规则只看嵌套层数）
        ShapeCell("③ EvenOdd", "切成奇偶规则 → 环形，不管方向") {
            val r = radius()
            val p = Path().apply {
                fillType = PathFillType.EvenOdd
                addOval(Rect(center, r))
                addOval(Rect(center, r * 0.55f))
            }
            drawPath(p, fill)
        }

        // ④ Path.Op 差集：圆 − 方块 → 挖一个缺口（复杂拼合首选，不用纠结方向）
        ShapeCell("④ Path.Op · 差集", "圆 − 右下方块 → 缺口") {
            val r = radius()
            val circle = Path().apply { addOval(Rect(center, r)) }
            val square = Path().apply {
                addRect(Rect(center.x, center.y, center.x + r, center.y + r))
            }
            val result = Path().apply { op(circle, square, PathOperation.Difference) }
            drawPath(result, fill)
        }

        // ⑤ 进度环：drawArc + Stroke，实战最常用（不涉及 fillType，描边而非填充）
        ShapeCell("⑤ 进度环 70%", "drawArc + Stroke 描边画弧") {
            val r = radius()
            val topLeft = Offset(center.x - r, center.y - r)
            val arcSize = Size(2 * r, 2 * r)
            val stroke = Stroke(width = r * 0.22f, cap = StrokeCap.Round)
            drawArc(track, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)      // 底环
            drawArc(fill, -90f, 360f * 0.7f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke) // 进度 70%
        }
    }
}

/** 一行 = 左边一个固定大小的画布 + 右边标题/说明。 */
@Composable
private fun ShapeCell(
    title: String,
    desc: String,
    draw: DrawScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Canvas(modifier = Modifier.size(96.dp)) { draw() }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** 画布内可用的半径：取短边一半再留点边距。 */
private fun DrawScope.radius(): Float = size.minDimension / 2f * 0.82f
