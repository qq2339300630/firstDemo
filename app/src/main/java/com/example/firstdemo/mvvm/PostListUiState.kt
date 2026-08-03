package com.example.firstdemo.mvvm

import com.example.firstdemo.retrofitstudy.Post

/**
 * 列表页的 UI 状态。
 *
 * 这次用 data class + 标志位，而不是 sealed interface。原因：
 * 列表场景里几种状态经常【并存】——比如「已有一屏旧数据」的同时「正在下拉刷新转圈」。
 * 密封接口的互斥状态表达不了这种组合，data class 用几个字段就很自然。
 *
 * isLoading   ：首次加载（列表还是空的，显示整页转圈）
 * isRefreshing：下拉刷新（列表已有数据，只在顶部转圈）
 */
data class PostListUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
