package com.example.firstdemo.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstdemo.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 列表页 ViewModel —— SWR（stale-while-revalidate）版。
 *
 * 数据流向变了，关键理解【单一数据源】：
 *   - posts 这个字段，只由"观察 Room 的 Flow"来更新（见 init 的 ①）
 *   - 网络刷新（fetch）只负责把新数据【写进 Room】，绝不直接改 posts
 *   - Room 写入后自动触发 ① 的 Flow 发射新值 → posts 更新 → UI 刷新
 * 于是：一进页面先看到上次缓存（秒开 / 无网可看），网络回来后无缝换成新数据。
 */
class PostListViewModel : ViewModel() {

    private val repository = PostRepository()

    private val _uiState = MutableStateFlow(PostListUiState())
    val uiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    init {
        // ① 订阅本地缓存：一订阅就拿到库里现有数据（可能是旧的）立刻上屏，
        //    之后每次 Room 变化都会自动收到新值。这是 posts 唯一的更新来源。
        repository.observePosts()
            .onEach { posts -> _uiState.update { it.copy(posts = posts) } }
            .launchIn(viewModelScope)

        // ② 触发首次网络刷新
        load()
    }

    /** 首屏加载：列表可能还空，显示整页转圈 */
    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        fetch()
    }

    /** 下拉刷新：保留旧列表，只在顶部转圈 */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        fetch()
    }

    // 只负责"触发网络刷新 + 记录刷新成败"；数据本身由 ① 的 Flow 送达，这里不碰 posts。
    private fun fetch() {
        // ★ 断点：协程发起处
        viewModelScope.launch {
            when (val result = repository.refreshPosts(userId = 1)) {
                is ApiResult.Success -> _uiState.update {
                    // 不用手动塞 posts —— upsert 进 Room 已自动触发 ① 更新界面
                    it.copy(isLoading = false, isRefreshing = false, error = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
                is ApiResult.Exception -> _uiState.update {
                    // 没网：仍记个错误提示；但只要缓存里有数据，界面照样显示旧列表（无网可看）
                    it.copy(isLoading = false, isRefreshing = false, error = "网络异常，请检查网络连接")
                }
            }
        }
    }
}
