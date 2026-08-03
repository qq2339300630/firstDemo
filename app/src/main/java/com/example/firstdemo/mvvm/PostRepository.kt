package com.example.firstdemo.mvvm

import com.example.firstdemo.network.ApiResult
import com.example.firstdemo.network.apiCall
import com.example.firstdemo.retrofitstudy.ApiService
import com.example.firstdemo.retrofitstudy.Post
import com.example.firstdemo.retrofitstudy.RetrofitClient

/**
 * Repository（数据层）：ViewModel 和数据来源（网络/数据库）之间的中间人。
 *
 * 为什么要这一层？——让 ViewModel 不直接依赖 Retrofit。
 * 以后想加缓存、换数据源、写测试，都只改 Repository，ViewModel 不动。
 *
 * ★ 引入网络框架后的变化：
 * 方法返回类型从裸的 Post 变成 ApiResult<Post>，body 用 apiCall { } 包一层。
 * 好处：请求的异常在这一层就被"翻译"成了确定的结果类型，
 * ViewModel 拿到的永远是 Success/Error/Exception 三选一，不会再抛异常上去。
 * 每个方法也退化成了一行 —— 这就是封装带来的收敛。
 */
class PostRepository(
    private val api: ApiService = RetrofitClient.api,
) {
    suspend fun getPost(id: Int): ApiResult<Post> =
        apiCall { api.getPost(id) }

    suspend fun getPostsByUser(userId: Int): ApiResult<List<Post>> =
        apiCall { api.getPostsByUser(userId) }
}
