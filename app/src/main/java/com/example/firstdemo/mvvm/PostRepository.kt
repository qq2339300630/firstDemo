package com.example.firstdemo.mvvm

import com.example.firstdemo.network.ApiResult
import com.example.firstdemo.network.HttpService
import com.example.firstdemo.network.apiCall
import com.example.firstdemo.network.decode
import com.example.firstdemo.retrofitstudy.Comment
import com.example.firstdemo.retrofitstudy.Post
import com.example.firstdemo.retrofitstudy.RetrofitClient

/**
 * 业务层（数据层）。这是它"正确的姿势"：
 *
 *   - 依赖【通用】的 HttpService（只有 get/post），不依赖一堆类型化端点
 *   - 每个方法在这里定义"业务上要什么 + 是什么类型"，用 http.get(url).decode() 拿到
 *   - 加新接口【只改这一个文件】，HttpService 永远不动
 *
 * 方法分两类：
 *   1) 简单接口：裸拿一份数据（getPost / getComments）—— 一行搞定
 *   2) 组合接口：把多个接口拼成业务概念（getPostDetail）—— 这才是 Repository 的核心价值，
 *      ViewModel 不该关心"要发几次请求、怎么拼"，它只管要一个 PostDetail
 *
 * decode() 的目标类型由方法的返回类型推导：
 *   http.get(...).decode() 里的 T 会被 Kotlin 从 ApiResult<Post> 反推成 Post。
 */
class PostRepository(
    private val http: HttpService = RetrofitClient.http,
) {
    /** 简单接口：单篇帖子。 */
    suspend fun getPost(id: Int): ApiResult<Post> =
        apiCall { http.get("posts/$id").decode() }

    /** 简单接口：某用户的帖子列表。查询参数直接写进 url。 */
    suspend fun getPostsByUser(userId: Int): ApiResult<List<Post>> =
        apiCall { http.get("posts?userId=$userId").decode() }

    /** 简单接口：某帖子的评论。 */
    suspend fun getComments(postId: Int): ApiResult<List<Comment>> =
        apiCall { http.get("posts/$postId/comments").decode() }

    /**
     * ★ 组合业务接口：一次拿到"帖子 + 它的评论"，拼成 PostDetail。
     *
     * 两个请求都包在【同一个】apiCall 里：任一个失败（断网/非2xx），
     * 整个 getPostDetail 就返回对应的 Error/Exception，语义清晰。
     *
     * 这里是顺序请求（先帖子后评论）。因为两者互不依赖，其实可以用
     * coroutineScope { async{} async{} } 并发拉、更快 —— 等你熟了可以改成并发版练手。
     */
    suspend fun getPostDetail(id: Int): ApiResult<PostDetail> = apiCall {
        val post: Post = http.get("posts/$id").decode()
        val comments: List<Comment> = http.get("posts/$id/comments").decode()
        PostDetail(post, comments)
    }
}
