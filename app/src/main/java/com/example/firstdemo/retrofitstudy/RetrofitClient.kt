package com.example.firstdemo.retrofitstudy

import com.example.firstdemo.network.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 负责组装 Retrofit 实例。
 *
 * 跟源码时，第一个断点就下在下面的 Retrofit.Builder().build() 上，
 * step into 可以看到 Retrofit 把 baseUrl / callFactory(OkHttpClient) /
 * converterFactories / callAdapterFactories 全部收集进一个 Retrofit 对象。
 *
 * 注意：build() 阶段【不会】解析任何 interface，也不会发请求。
 * 真正的注解解析发生在 create() 之后「第一次调用接口方法」时。
 */
object RetrofitClient {

    // 免费公共测试 API，返回假的 posts 数据
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // OkHttp 日志拦截器：把每次真实请求/响应打到 Logcat，
    // 方便你对照断点确认「Retrofit 最终确实把活交给了 OkHttp」
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 注意添加顺序 = 执行顺序:
    // 把 AuthInterceptor 放在 logging【之前】,请求先经过 Auth 加好 header,
    // 再流到 logging,这样日志里就能看到 Authorization / Accept 头(否则打印不到)。
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())   // ① 先加公共头/token
        .addInterceptor(logging)             // ② 再打日志(能看到①加的头)
        .build()

    val api: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)                       // callFactory：真正发请求的人
            .addConverterFactory(GsonConverterFactory.create()) // Converter：JSON <-> Post
            .build()

        // ★ 核心断点位置 1：step into create() 看动态代理是怎么生成的
        retrofit.create(ApiService::class.java)
    }
}
