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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 混合模式（BlendMode）Demo：三个原色圆叠加，看"加法变亮 / 乘法变暗"。
 *
 * ★ 关键点（和 Xfermode 同一个坑）：
 * blendMode 是把当前要画的东西，和【当前图层里已有的像素】做混合。
 * 所以必须先画一个【不透明基底】(drawRect)，给混合一个确定的 Dst：
 *   - MULTIPLY（相乘变暗）基底用【白】：白 × 颜色 = 颜色本身，重叠处相乘趋近黑
 *   - SCREEN / PLUS（叠加变亮）基底用【黑】：黑 + 颜色 = 颜色本身，重叠处相加趋近白
 * 基底不透明，混合就只发生在这个 Canvas 内，不会串到背景/别的控件。
 */
@Composable
fun BlendDemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "BlendMode：叠加变亮 vs 相乘变暗",
            style = MaterialTheme.typography.titleMedium,
        )

        // 相乘变暗 → 阴影/压暗：基底白
        BlendCell("MULTIPLY · 相乘变暗", "重叠趋黑 → 阴影 / 压暗", base = Color.White, blend = BlendMode.Multiply)
        // 滤色变亮 → 发光：基底黑
        BlendCell("SCREEN · 滤色变亮", "重叠趋白 → 发光", base = Color.Black, blend = BlendMode.Screen)
        // 加法叠加 → 高光（比 SCREEN 更亮、更快封顶到白）：基底黑
        BlendCell("ADD (Plus) · 加法叠加", "additive → 高光更强", base = Color.Black, blend = BlendMode.Plus)
    }
}

@Composable
private fun BlendCell(
    title: String,
    desc: String,
    base: Color,
    blend: BlendMode,
) {
    // 三原色（绿色调暗一点，避免纯 00FF00 太刺眼）
    val red = Color(0xFFFF0000)
    val green = Color(0xFF00E000)
    val blue = Color(0xFF0000FF)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Canvas(modifier = Modifier.size(110.dp)) {
            drawRect(base)   // ① 不透明基底 = 混合的 Dst

            val r = size.minDimension * 0.26f
            val c = center
            // ② 三个原色圆按 blendMode 叠加（呈三角形交叠）
            drawCircle(red, r, Offset(c.x, c.y - r * 0.62f), blendMode = blend)
            drawCircle(green, r, Offset(c.x - r * 0.55f, c.y + r * 0.42f), blendMode = blend)
            drawCircle(blue, r, Offset(c.x + r * 0.55f, c.y + r * 0.42f), blendMode = blend)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
    }
}
