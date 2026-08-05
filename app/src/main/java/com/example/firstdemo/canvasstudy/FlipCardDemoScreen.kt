package com.example.firstdemo.canvasstudy

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * 翻牌卡片 —— Compose 版 3D 翻转。
 *
 * Compose 不用 android.graphics.Camera,3D 旋转内建在 Modifier.graphicsLayer 里:
 *   rotationY       绕 Y 轴翻转(左右翻/翻牌),0→180 度
 *   cameraDistance  景深,防高分屏透视过强(用 density 缩放)
 *   轴心默认就是中心(transformOrigin = 0.5,0.5),不用像经典 Camera 那样手动 pre/post translate
 *
 * 一个必处理的坑:当 rotationY 过了 90°,整张卡被镜像了。
 * 所以过 90° 后要①切换到背面内容 ②把背面再 rotationY 180° 转回来,否则背面文字是反的。
 */
@Composable
fun FlipCardDemoScreen(modifier: Modifier = Modifier) {
    var flipped by remember { mutableStateOf(false) }

    // 点击驱动 0↔180 的旋转角,animateFloatAsState 自动补间动画
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "flip",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        Text("翻牌卡片 · graphicsLayer 3D 翻转", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier
                .size(width = 220.dp, height = 300.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density   // ★ 景深:乘 density 防高分屏透视过猛
                }
                .clickable { flipped = !flipped },
            contentAlignment = Alignment.Center,
        ) {
            if (rotation <= 90f) {
                CardFace(Color(0xFF1E88E5), "正 面", "点我翻转 →")
            } else {
                // ★ 背面再翻 180°,修正镜像,否则文字是反的
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                ) {
                    CardFace(Color(0xFFE53935), "背 面", "← 再点翻回")
                }
            }
        }

        Text(
            "点击卡片翻转:过 90° 时切换正反面,并把背面再翻 180° 修正镜像。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun CardFace(color: Color, title: String, hint: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text(hint, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
