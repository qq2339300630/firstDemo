package com.example.firstdemo.retrofitstudy

/**
 * 评论数据模型，对应 jsonplaceholder 的 /posts/{id}/comments 返回项。
 * 字段：postId(属于哪篇帖子)、id、name、email、body。
 */
data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val email: String,
    val body: String,
)
