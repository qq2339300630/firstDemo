package com.example.firstdemo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 数据库里的一行 = 一个 PostEntity。
 *
 * 为什么不直接存 retrofitstudy.Post,要单独建个 Entity？
 * 分层解耦:Post 是"网络返回的 DTO",PostEntity 是"数据库表结构",两者职责不同、
 * 可能各自演化(比如表想加个 cachedAt 时间戳,但网络 DTO 不该有)。Repository 负责在
 * 两者间转换(见 PostMappers.kt)。这是 Now in Android 等项目的标准做法。
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
)
