package com.example.firstdemo.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room 数据库定义。
 *
 * entities：这个库包含哪些表(这里只有 PostEntity)。
 * version ：schema 版本号。以后改了表结构要 +1,并提供 Migration(否则崩)。
 *
 * abstract fun postDao()：声明能拿到哪些 DAO,实现同样由 Room 生成。
 */
@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
