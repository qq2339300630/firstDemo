package com.example.firstdemo.retrofitstudy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.firstdemo.coroutinestudy.CoroutineBasics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 一个最小的 Demo 界面：两个按钮分别演示 Retrofit 的两种调用方式。
 *
 * ── 想跟源码时，推荐的断点顺序 ──
 * 1) RetrofitClient.kt  retrofit.create(...)        看动态代理生成
 * 2) 下面 fetchWithCall() 里 api.getPostCall(1)      触发 InvocationHandler.invoke
 *    step into 会进入 Retrofit$1.invoke → loadServiceMethod → HttpServiceMethod.invoke
 * 3) call.enqueue(...) / call.execute()             进入 OkHttpCall，看它调用 OkHttp
 */
@Composable
fun RetrofitDemoScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("点上面的按钮发起请求") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 方式一：Call<T> —— 最原始、最适合第一次单步跟踪
        Button(onClick = {
//            result = "请求中… (Call 方式)"
//            fetchWithCall(
//                onResult = { result = it },
//            )

            GlobalScope.launch {
                try {
                    RetrofitClient.api.getPost(8)
                } catch (e: Exception)  {

            }

            }

        }) {
            Text("方式一：Call.enqueue()")
        }

        // 方式二：suspend —— 由 CallAdapter 适配成挂起函数
        Button(onClick = {
            result = "请求中… (suspend 方式)"
            scope.launch {
                result = try {
                    // ★ 断点：step into 会看到 suspend 版最终也走 OkHttpCall
                    val post = RetrofitClient.api.getPost(1)
                    "suspend 成功:\n$post"
                } catch (e: Exception) {
                    "失败: ${e.message}"
                }
            }
        }) {
            Text("方式二：suspend fun")
        }

        // 方式三：纯 OkHttp 同步（不经过 Retrofit），放子线程避免主线程异常
        Button(onClick = {
            result = "请求中… (OkHttp 同步)"
            scope.launch {
                // ★ 断点：step into OkHttpDemo.sync() → newCall → execute → 拦截器链
                val r = withContext(Dispatchers.IO) { OkHttpDemo.sync() }
                result = r
            }
        }) {
            Text("方式三：OkHttp 同步 execute()")
        }

        // 方式四：纯 OkHttp 异步，交给 Dispatcher 线程池
        Button(onClick = {
            result = "请求中… (OkHttp 异步)"
            OkHttpDemo.async { result = it }
        }) {
            Text("方式四：OkHttp 异步 enqueue()")
        }

        // 协程基础：跑 Lesson 1~4，结果看 Logcat（tag = CoroutineStudy）
        Button(onClick = {
            result = "协程 Demo 运行中… 打开 Logcat 过滤 tag=CoroutineStudy 看输出"
            scope.launch {
                // ★ 断点：step into runAll() 逐个 lesson 跟「挂起 / 恢复」
                CoroutineBasics.runAll()
                result = "协程 Lesson 1~4 跑完，详见 Logcat（tag=CoroutineStudy）"
            }
        }) {
            Text("协程基础：Lesson 1~4（看 Logcat）")
        }

        Text(result)
    }
}

/**
 * Call<T> 的异步用法：enqueue 交给后台线程池，回调在主线程。
 * 这是最能看清 Retrofit 调用链的写法，不涉及协程包装。
 */
private fun fetchWithCall(onResult: (String) -> Unit) {
    val call: Call<Post> = RetrofitClient.api.getPostCall(1)
    call.enqueue(object : Callback<Post> {
        override fun onResponse(call: Call<Post>, response: Response<Post>) {
            onResult(
                if (response.isSuccessful) "Call 成功:\n${response.body()}"
                else "HTTP ${response.code()}"
            )
        }

        override fun onFailure(call: Call<Post>, t: Throwable) {
            onResult("失败: ${t.message}")
        }
    })
}
