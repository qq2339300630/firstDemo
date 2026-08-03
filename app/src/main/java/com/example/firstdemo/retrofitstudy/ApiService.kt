package com.example.firstdemo.retrofitstudy

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 这就是 Retrofit 的「主角」：一个纯 interface，没有任何实现。
 *
 * Retrofit.create(ApiService::class.java) 会用 JDK 动态代理为它生成一个代理对象。
 * 你调用下面任何方法时，实际执行的是 Retrofit 内部的 InvocationHandler.invoke()，
 * 它读取方法上的注解（@GET / @Path / @Query）来拼出真正的 HTTP 请求。
 *
 * 这里故意提供两种返回类型，方便对比 CallAdapter 的作用：
 *   - Call<T>       ：Retrofit 原生返回类型，需要手动 enqueue/execute
 *   - suspend fun   ：协程写法，由内置的 SuspendForBody CallAdapter 适配
 */
interface ApiService {

    // 返回 Call<T>：最原始的形式，最适合第一次跟源码
    @GET("posts/{id}")
    fun getPostCall(@Path("id") id: Int): Call<Post>

    // suspend 写法：等价于上面，但由 CallAdapter 把 Call 适配成挂起函数
    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post

    // 带 @Query 的例子：GET posts?userId=1
    @GET("posts")
    suspend fun getPostsByUser(@Query("userId") userId: Int): List<Post>
}
