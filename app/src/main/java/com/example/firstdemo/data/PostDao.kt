package com.example.firstdemo.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * DAO：定义对 posts 表的操作。Room 会在编译期(KSP)自动生成实现类。
 *
 * ★ SWR 的关键就在 observePosts() 返回 Flow<List<PostEntity>>：
 * 这是一个"活的"查询 —— 只要 posts 表数据变了(比如网络刷新后 upsert 了新数据),
 * Room 会【自动重新查询并往 Flow 里发射新值】。UI 订阅它,就能自动刷新。
 * 这就是"单一数据源"：界面永远只认数据库,不直接认网络。
 */
@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY id")
    fun observePosts(): Flow<List<PostEntity>>

    /**
     * Upsert = 有则更新、无则插入(按主键 id)。
     * 网络刷新后用它把最新数据写进表,顺带覆盖旧的。
     */
    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Query("DELETE FROM posts")
    suspend fun clear()
}
