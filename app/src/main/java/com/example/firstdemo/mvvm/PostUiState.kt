package com.example.firstdemo.mvvm

import com.example.firstdemo.retrofitstudy.Post

/**
 * UI 状态：用一个密封接口把「界面可能处于的几种状态」穷举出来。
 *
 * 这是 MVVM + 单向数据流（UDF）的核心思想：
 *   ViewModel 只对外暴露【一个】状态对象，UI 根据状态渲染，
 *   状态之间互斥（要么 Loading、要么 Success、要么 Error），不会出现
 *   「既在转圈又显示数据」这种矛盾状态。
 */
sealed interface PostUiState {
    data object Idle : PostUiState                    // 初始，还没加载
    data object Loading : PostUiState                 // 请求中
    data class Success(val post: Post) : PostUiState  // 成功，带数据
    data class Error(val message: String) : PostUiState // 失败，带错误信息
}
