package com.example.firstdemo.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstdemo.network.ApiResult
import com.example.firstdemo.network.apiCall
import com.example.firstdemo.retrofitstudy.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 列表页 ViewModel。演示两个和上一个 Demo 不同的点：
 *   1) init{} 里自动首屏加载 —— ViewModel 创建即拉数据
 *   2) 用 _uiState.update{ } 基于旧状态改新状态（并发安全，比直接 .value= 更稳）
 */
class PostListViewModel : ViewModel() {

    // 同样去掉 Repository,直接 apiCall{ RetrofitClient.api.xxx() }

    private val _uiState = MutableStateFlow(PostListUiState())
    val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    init {
        // ViewModel 一创建就首屏加载（只会执行一次，重组不会重复触发）
        load()
    }

    /** 首屏加载：列表为空，显示整页转圈 */
    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetch(isRefresh = false)
    }

    /** 下拉刷新：保留旧列表，只在顶部转圈 */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        fetch(isRefresh = true)
    }

    // 首屏和刷新共用的请求逻辑
    private fun fetch(isRefresh: Boolean) {
        // ★ 断点：协程发起处
        viewModelScope.launch {
            // 同样用 when 分流 ApiResult。列表页的错误统一塞进 uiState.error 字段。
            when (val result = apiCall { RetrofitClient.api.getPostsByUser(userId = 1) }) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        posts = result.data,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                is ApiResult.Exception -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = "网络异常，请检查网络连接")
                }
            }
        }
    }
}
