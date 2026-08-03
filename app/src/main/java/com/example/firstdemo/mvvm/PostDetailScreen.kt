package com.example.firstdemo.mvvm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstdemo.retrofitstudy.Comment

/**
 * 帖子详情页:标题/正文 + 评论列表。
 * 数据来自【组合业务接口】getPostDetail —— UI 拿到的是一个拼好的 PostDetail,
 * 不用自己发两次请求、也不用自己拼。
 */
@Composable
fun PostDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is PostDetailUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PostDetailUiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("❌ ${state.message}")
            }
        }

        is PostDetailUiState.Success -> {
            val detail = state.detail
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 帖子本身
                item {
                    Text(detail.post.title, style = MaterialTheme.typography.titleMedium)
                    Text(detail.post.body, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
                    Text(
                        "评论（${detail.comments.size}）",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                // 评论列表
                items(detail.comments, key = { it.id }) { comment ->
                    CommentRow(comment)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: Comment) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(comment.name, style = MaterialTheme.typography.titleSmall)
        Text(comment.email, style = MaterialTheme.typography.labelSmall)
        Text(comment.body, style = MaterialTheme.typography.bodySmall)
    }
}
