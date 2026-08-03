package com.example.firstdemo.mvvm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * UI 层：Compose 界面。
 *
 * MVVM 的数据流向（单向）：
 *   用户点按钮 → viewModel.loadPost() → ViewModel 改 StateFlow
 *            → Compose 观察到新状态 → 自动重组(recompose)刷新界面
 *
 * 两个关键 API：
 *   viewModel()                  —— 拿到（并在重组间保持同一个）PostViewModel 实例
 *   collectAsStateWithLifecycle() —— 把 StateFlow 转成 Compose 的 State，
 *                                    且在界面不可见时自动暂停收集（比 collectAsState 更省）
 */
@Composable
fun PostMvvmScreen(
    modifier: Modifier = Modifier,
    viewModel: PostViewModel = viewModel(),
) {
    // 观察状态：uiState 一变，这里就重组
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("MVVM Demo：协程 + Retrofit + ViewModel + StateFlow", style = MaterialTheme.typography.titleMedium)

        Button(onClick = { viewModel.loadPost(1) }) {
            Text("加载 Post #1")
        }

        // 根据状态渲染不同 UI —— when 覆盖所有情况，编译器保证不漏
        when (val state = uiState) {
            is PostUiState.Idle -> Text("点上面的按钮开始加载")
            is PostUiState.Loading -> CircularProgressIndicator()
            is PostUiState.Success -> {
                val post = state.post
                Text("✅ 加载成功")
                Text("title: ${post.title}")
                Text("body: ${post.body}")
            }
            is PostUiState.Error -> Text("❌ 失败: ${state.message}")
        }
    }
}
