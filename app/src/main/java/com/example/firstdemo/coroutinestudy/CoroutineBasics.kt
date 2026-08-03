package com.example.firstdemo.coroutinestudy

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 协程基础 Demo（阶段 1~4）。
 *
 * 所有输出用 Log.d(TAG, ...) 打到 Logcat，过滤 tag = "CoroutineStudy" 看结果。
 * 每个 lesson 都是 suspend 函数，方便在里面下断点单步跟「挂起 / 恢复」。
 *
 * 用法：在协程作用域里调用，比如
 *   scope.launch { CoroutineBasics.runAll() }
 */
object CoroutineBasics {

    const val TAG = "CoroutineStudy"

    suspend fun runAll() {
        lesson1_suspendVsBlock()
        lesson2_launchVsAsync()
        lesson3_dispatchers()
        lesson4_structuredConcurrency()
        Log.d(TAG, "===== 全部 lesson 跑完 =====")
    }

    // ─────────────────────────────────────────────────────────────
    // Lesson 1：挂起 vs 阻塞
    //   delay 是挂起函数：暂停协程但【不阻塞线程】，线程可以去干别的
    //   Thread.sleep 是阻塞：把整个线程卡住
    // ─────────────────────────────────────────────────────────────
    private suspend fun lesson1_suspendVsBlock() {
        Log.d(TAG, "L1 开始，线程=${threadName()}")
        // ★ 断点这一行，单步：会发现 delay 之后可能换了线程/时间跳了 1 秒，但线程没被卡死
        delay(1000)
        Log.d(TAG, "L1 delay 1 秒后恢复，线程=${threadName()}")
    }

    // ─────────────────────────────────────────────────────────────
    // Lesson 2：launch（发射后不管） vs async（要返回值）
    //   两个网络请求「并发」跑：串行要 2 秒，并发只要约 1 秒
    // ─────────────────────────────────────────────────────────────
    private suspend fun lesson2_launchVsAsync() = coroutineScope {
        // launch：不关心返回值，返回 Job，可以 join/cancel
        val job = launch {
            delay(500)
            Log.d(TAG, "L2 launch 的活干完了（不返回值）")
        }

        // async：关心返回值，返回 Deferred，用 await() 拿结果
        val d1 = async { fakeApi("A", 1000) }   // 两个 async 同时开跑
        val d2 = async { fakeApi("B", 1000) }
        // ★ 断点 await：两个请求是并发的，总耗时 ≈ 1 秒而不是 2 秒
        val result = d1.await() + " + " + d2.await()
        Log.d(TAG, "L2 async 并发结果 = $result")

        job.join()  // 等 launch 那个也结束
    }

    // ─────────────────────────────────────────────────────────────
    // Lesson 3：Dispatchers —— 协程跑在哪个线程
    //   withContext 切线程：IO 干耗时活，切回来更新结果
    // ─────────────────────────────────────────────────────────────
    private suspend fun lesson3_dispatchers() {
        Log.d(TAG, "L3 当前线程=${threadName()}")

        val data = withContext(Dispatchers.IO) {
            // 模拟在 IO 线程读网络/数据库
            Log.d(TAG, "L3 IO 线程干活=${threadName()}")
            delay(300)
            "从 IO 拿到的数据"
        }
        // withContext 结束后自动切回原来的线程/调度器
        Log.d(TAG, "L3 切回来=${threadName()}，data=$data")

        val sum = withContext(Dispatchers.Default) {
            // Default 适合 CPU 密集计算
            Log.d(TAG, "L3 Default 线程算数=${threadName()}")
            (1..1_000_000).sum()
        }
        Log.d(TAG, "L3 计算结果=$sum")
    }

    // ─────────────────────────────────────────────────────────────
    // Lesson 4：结构化并发
    //   coroutineScope 会等它内部所有子协程都完成后才返回
    //   —— 这就是「父等子」的结构化并发，避免协程泄漏
    // ─────────────────────────────────────────────────────────────
    private suspend fun lesson4_structuredConcurrency() {
        Log.d(TAG, "L4 进入 coroutineScope 前")
        coroutineScope {
            launch { delay(400); Log.d(TAG, "L4 子协程1 完成") }
            launch { delay(200); Log.d(TAG, "L4 子协程2 完成") }
            Log.d(TAG, "L4 scope 内：已启动两个子协程，但 scope 不会提前结束")
        }
        // ★ 只有上面两个子协程都干完，才会执行到这里
        Log.d(TAG, "L4 coroutineScope 结束（所有子协程都完成了）")
    }

    // 模拟一个耗时网络请求，delayMs 毫秒后返回
    private suspend fun fakeApi(tag: String, delayMs: Long): String {
        delay(delayMs)
        return "resp-$tag"
    }

    private fun threadName(): String = Thread.currentThread().name
}
