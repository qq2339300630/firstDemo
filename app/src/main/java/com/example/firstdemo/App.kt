package com.example.firstdemo

import android.app.Application
import com.example.firstdemo.data.DatabaseProvider

/**
 * 自定义 Application：进程启动时(早于任何 Activity/ViewModel)执行一次初始化。
 * 这里用来建好 Room 数据库,让后面 Repository 能直接拿到 DAO。
 *
 * 记得在 AndroidManifest.xml 的 <application android:name=".App" ... /> 注册,
 * 否则系统用默认 Application,这里的 onCreate 不会跑,DatabaseProvider 就没初始化。
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseProvider.init(this)
    }
}
