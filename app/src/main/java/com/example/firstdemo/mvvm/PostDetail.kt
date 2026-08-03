package com.example.firstdemo.mvvm

import com.example.firstdemo.retrofitstudy.Comment
import com.example.firstdemo.retrofitstudy.Post

/**
 * 业务模型：一篇帖子 + 它的评论。
 *
 * 注意这【不是】任何一个后端接口直接返回的东西 —— 它是把 /posts/{id} 和
 * /posts/{id}/comments 两个接口的结果【组合】出来的。这种"组合出的业务概念"正是
 * Repository 该干的活，也是它区别于"裸接口"的价值所在。
 */
data class PostDetail(
    val post: Post,
    val comments: List<Comment>,
)
