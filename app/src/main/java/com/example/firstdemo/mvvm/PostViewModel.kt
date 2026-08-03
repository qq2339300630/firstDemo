package com.example.firstdemo.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstdemo.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel：持有并暴露 UI 状态，负责发起业务（这里是网络请求）。
 *
 * ★ 协程在 MVVM 里的关键就这一处：viewModelScope.launch { ... }
 *
 * viewModelScope 是 ViewModel 自带的协程作用域：
 *   - 绑定 ViewModel 生命周期，ViewModel 被清除（onCleared）时【自动取消】里面所有协程
 *   - 默认运行在 Dispatchers.Main.immediate（主线程），所以能直接更新 _uiState
 *   - 这就是为什么用它请求网络，页面销毁后不会内存泄漏、也不会崩「更新已销毁的界面」
 */
class PostViewModel : ViewModel() {

    // 注意：这里直接 new Repository，而不是写成构造参数。
    // 因为 Compose 的 viewModel() 用默认工厂靠【反射找无参构造】来创建 ViewModel，
    // Kotlin 的「默认参数」不会生成真正的无参构造，会导致运行时崩溃。
    // 真实项目里要做构造注入，得配 ViewModelProvider.Factory 或用 Hilt。
    private val repository = PostRepository()

    // 对内可变
    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    // 对外只读（UI 只能观察，不能改）——这是 StateFlow 的经典封装写法
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    /**
     * 加载指定 id 的 Post。
     * 注意整个过程没有回调、没有手动切线程，就是顺序代码 + try/catch。
     */
    fun loadPost(id: Int) {
        // 先切到 Loading，UI 立刻显示转圈
        _uiState.value = PostUiState.Loading

        // ★ 断点：step into launch，跟一遍「挂起→请求→恢复→更新状态」
        viewModelScope.launch {
            // repository 现在返回 ApiResult，不再抛异常。
            // 用 when 分流三种结果 —— 密封接口保证这里覆盖全、漏一个编译不过。
            // 注意 Error 和 Exception 给的是【不同】文案：
            //   Error     = 服务器有响应但非2xx（用 result.message，如"资源不存在"）
            //   Exception = 根本没连上（统一提示网络问题）
            _uiState.value = when (val result = repository.getPost(id)) {
                is ApiResult.Success -> PostUiState.Success(result.data)
                is ApiResult.Error -> PostUiState.Error(result.message)
                is ApiResult.Exception -> PostUiState.Error("网络异常，请检查网络连接")
            }
        }
    }
}
