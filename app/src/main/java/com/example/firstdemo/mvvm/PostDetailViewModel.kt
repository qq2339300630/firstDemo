package com.example.firstdemo.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstdemo.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 详情页 ViewModel。重点:它调的是 repository.getPostDetail() 这个【组合业务接口】,
 * 完全不知道背后其实发了两次请求(帖子 + 评论)。
 * "发几次请求、怎么拼"被 Repository 封装掉了 —— 这就是业务层的价值。
 */
class PostDetailViewModel : ViewModel() {

    private val repository = PostRepository()

    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        load(1)   // 创建即加载 1 号帖子详情
    }

    fun load(id: Int) {
        _uiState.value = PostDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getPostDetail(id)) {
                is ApiResult.Success -> PostDetailUiState.Success(result.data)
                is ApiResult.Error -> PostDetailUiState.Error(result.message)
                is ApiResult.Exception -> PostDetailUiState.Error("网络异常，请检查网络连接")
            }
        }
    }
}
