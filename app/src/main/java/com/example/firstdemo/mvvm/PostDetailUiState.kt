package com.example.firstdemo.mvvm

/**
 * 帖子详情页的 UI 状态。和 PostUiState 一样用密封接口穷举互斥状态。
 * Success 携带的是【组合业务模型】PostDetail(帖子 + 评论),不是单个接口的返回。
 */
sealed interface PostDetailUiState {
    data object Loading : PostDetailUiState
    data class Success(val detail: PostDetail) : PostDetailUiState
    data class Error(val message: String) : PostDetailUiState
}
