package com.example.firstdemo.data

import android.content.Context
import androidx.room.Room

/**
 * 数据库单例持有者。
 *
 * Room 建库需要 Context,而 Repository/ViewModel 手头没有 Context。
 * 所以在 Application.onCreate()（App.kt）里调一次 init(context) 把库建好并存在这里,
 * 之后任何地方用 DatabaseProvider.postDao 直接拿。
 * (真实项目里这种"给谁提供依赖"的活通常交给 Hilt 之类的 DI 框架,这里手动做够用。)
 */
object DatabaseProvider {

    private lateinit var database: AppDatabase

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "firstdemo.db",
        ).build()
    }

    val postDao: PostDao get() = database.postDao()
}
