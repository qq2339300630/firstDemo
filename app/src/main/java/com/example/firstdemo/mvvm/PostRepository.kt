package com.example.firstdemo.mvvm

import com.example.firstdemo.retrofitstudy.ApiService
import com.example.firstdemo.retrofitstudy.Post
import com.example.firstdemo.retrofitstudy.RetrofitClient

/**
 * Repository（数据层）：ViewModel 和数据来源（网络/数据库）之间的中间人。
 *
 * 为什么要这一层？——让 ViewModel 不直接依赖 Retrofit。
 * 以后想加缓存、换数据源、写测试，都只改 Repository，ViewModel 不动。
 *
 * 这里的方法都是 suspend：它们本身不切线程，
 * 「在哪个线程执行」由调用方（ViewModel）用 Dispatcher 决定。
 * 不过 Retrofit 的 suspend 请求内部已经切到 IO 了，所以这里直接调即可。
 */
class PostRepository(
    private val api: ApiService = RetrofitClient.api,
) {
    suspend fun getPost(id: Int): Post = api.getPost(id)

    suspend fun getPostsByUser(userId: Int): List<Post> = api.getPostsByUser(userId)
}
