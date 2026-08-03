package com.example.firstdemo.mvvm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstdemo.retrofitstudy.Post

/**
 * 列表页 UI：下拉刷新 + 列表。
 *
 * 关注两点：
 *   - PullToRefreshBox：Material3 官方下拉刷新容器，isRefreshing 由 StateFlow 驱动
 *   - LazyColumn：只渲染可见项的高性能列表（类似 RecyclerView）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    modifier: Modifier = Modifier,
    viewModel: PostListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },   // 下拉手势触发
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            // 首屏加载：列表还空着，显示整页转圈
            uiState.isLoading && uiState.posts.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // 出错且没有任何数据可显示
            uiState.error != null && uiState.posts.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("❌ ${uiState.error}")
                }
            }
            // 有数据：渲染列表（即使正在刷新，旧数据也照常显示）
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        PostRow(post)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PostRow(post: Post) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("#${post.id}  ${post.title}", style = MaterialTheme.typography.titleSmall)
        Text(post.body, style = MaterialTheme.typography.bodySmall)
    }
}
