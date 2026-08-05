package com.example.firstdemo.canvasstudy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 *             图文混排 + 自动换行(Compose 版)。
 *
 * 核心三件套：
 *   buildAnnotatedString  —— 拼一段带"占位标记"的富文本
 *   appendInlineContent   —— 在文字流里插一个占位(id + 备用文本)
 *   InlineTextContent     —— 用 id 映射到真正要渲染的 Composable(这里用 Canvas 画的图标)
 *
 * 关键：Text 把行内图标当成一个"字符"参与排版，所以它会跟着文字一起【自动换行】，
 * 你不用管断行、也不用量文字。图标这里用 Canvas 现画(接前面学的 Path)。
 */
@Composable
fun InlineTextDemoScreen(modifier: Modifier = Modifier) {
    val text = buildAnnotatedString {
        append("这是一段较长的文字，用来演示 Text 的自动换行。看这里点个 ")
        appendInlineContent("heart", "[心]")   // id=heart，"[心]"是无法渲染时的备用文本
        append(" 就在文字中间嵌了一张图；继续往下写，它会跟着文字一起换行。再来一颗 ")
        appendInlineContent("star", "[星]")
        append(" 星星，随便加多少都行——图标就像普通字符一样参与排版和折行，全自动。")
    }

    val inline = mapOf(
        "heart" to InlineTextContent(
            Placeholder(18.sp, 18.sp, PlaceholderVerticalAlign.Center), // 预留尺寸 + 与文字垂直居中
        ) {
            Canvas(Modifier.fillMaxSize()) { drawHeart(Color(0xFFE53935)) }
        },
        "star" to InlineTextContent(
            Placeholder(18.sp, 18.sp, PlaceholderVerticalAlign.Center),
        ) {
            Canvas(Modifier.fillMaxSize()) { drawStar(Color(0xFFFFB300)) }
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("图文混排 + 自动换行", style = MaterialTheme.typography.titleMedium)
        Text(
            text = text,
            inlineContent = inline,   // ★ 把占位 id 映射到实际图标
            fontSize = 18.sp,
            lineHeight = 30.sp,
        )
        Text(
            "试试把手机横过来 / 改窗口宽度：文字和图标会重新一起换行。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/** 用 Path 画一个五角星，填满画布。 */
private fun DrawScope.drawStar(color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outer = size.minDimension / 2f * 0.95f
    val inner = outer * 0.42f
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = Math.toRadians((-90 + i * 36).toDouble())
        val x = cx + r * cos(a).toFloat()
        val y = cy + r * sin(a).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

/** 用两段三次贝塞尔画一个爱心，填满画布。 */
private fun DrawScope.drawHeart(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.85f)
        cubicTo(w * 0.03f, h * 0.55f, w * 0.16f, h * 0.08f, w * 0.5f, h * 0.34f)
        cubicTo(w * 0.84f, h * 0.08f, w * 0.97f, h * 0.55f, w * 0.5f, h * 0.85f)
        close()
    }
    drawPath(path, color)
}
