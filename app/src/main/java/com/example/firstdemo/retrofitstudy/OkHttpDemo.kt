package com.example.firstdemo.retrofitstudy

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 纯 OkHttp 调用（不经过 Retrofit），用来跟 OkHttp 自己的源码。
 *
 * OkHttp 三大核心对象：
 *   OkHttpClient  —— 全局配置 + 连接池 + 线程池(Dispatcher)，应当单例复用
 *   Request       —— 一次请求的描述（url / method / headers / body）
 *   Call          —— 一次「可执行」的请求，实现类是 RealCall
 *
 * ── 跟源码推荐断点顺序 ──
 * 1) client.newCall(request)          → 进 OkHttpClient.newCall → new RealCall
 * 2) call.execute() / call.enqueue()  → 进 RealCall
 * 3) RealCall.getResponseWithInterceptorChain()   ★ 全场核心：拦截器责任链
 *      这里能看到 5 个内置拦截器按顺序组装：
 *      retryAndFollowUp → Bridge → Cache → Connect → CallServer
 * 4) RealInterceptorChain.proceed()   → 看链条是怎么一层层 proceed 下去的
 */
object OkHttpDemo {

    // 复用同一个 client（含连接池/线程池）。跟源码时也方便观察连接复用。
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // 自定义 Application 拦截器：最外层，proceed 前后能包裹整个请求
            // ★ 断点：看 chain.request()，再 step over 到 proceed 看返回的 response
            val request = chain.request()
            val response = chain.proceed(request)
            response
        }
        .build()

    private const val URL = "https://jsonplaceholder.typicode.com/posts/1"

    /**
     * 同步调用：当前线程阻塞直到响应返回。
     * 注意：不能在 Android 主线程调，会抛 NetworkOnMainThreadException，
     * 所以下面 Demo 界面里会放到子线程执行。
     */
    fun sync(): String {
        val request = Request.Builder()
            .url(URL)
            .build()

        // ★ 断点 1：step into 看 newCall 造出 RealCall
        val call: Call = client.newCall(request)

        // ★ 断点 2：step into execute → getResponseWithInterceptorChain（拦截器链核心）
        call.execute().use { response ->
            return if (response.isSuccessful) {
                "同步成功: HTTP ${response.code}\n${response.body?.string()}"
            } else {
                "HTTP ${response.code}"
            }
        }
    }

    /**
     * 异步调用：交给 Dispatcher 的线程池执行，回调在子线程。
     * ★ 断点：step into enqueue → Dispatcher.enqueue → 看 runningAsyncCalls/readyAsyncCalls 队列
     */
    fun async(onResult: (String) -> Unit) {
        val request = Request.Builder()
            .url(URL)
            .addHeader("X-Demo", "okhttp-study")   // 加个头，方便在 Bridge 拦截器里观察
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    onResult(
                        if (it.isSuccessful) "异步成功: HTTP ${it.code}\n${it.body?.string()}"
                        else "HTTP ${it.code}"
                    )
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onResult("失败: ${e.message}")
            }
        })
    }
}
