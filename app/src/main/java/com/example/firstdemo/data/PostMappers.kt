package com.example.firstdemo.data

import com.example.firstdemo.retrofitstudy.Post

/**
 * DTO <-> Entity 的转换。
 * Repository 用它在"网络模型 Post"和"数据库模型 PostEntity"之间来回转,
 * 让两层各自独立、互不牵连。
 */
fun PostEntity.toPost(): Post = Post(
    userId = userId,
    id = id,
    title = title,
    body = body,
)

fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    userId = userId,
    title = title,
    body = body,
)
