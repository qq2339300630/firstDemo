package com.example.firstdemo.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * token 的来源。真实项目里 token 通常在登录成功后写入、从本地存储(如 DataStore)读出。
 * 这里用一个可变字段模拟：默认给个假 token，你可以在登录后调 update() 替换。
 *
 * 单独抽出来的意义：拦截器只管"取当前 token 加到请求上"，
 * 至于 token 怎么来、存哪，是另一件事，解耦开更清晰。
 */
object TokenProvider {
    @Volatile
    private var token: String? = "demo-token-123"   // 模拟已登录状态

    fun current(): String? = token
    fun update(newToken: String?) { token = newToken }
}

/**
 * 统一给每个请求加公共请求头的【应用拦截器】。
 *
 * 回顾你学过的拦截器链：它通过 OkHttpClient.addInterceptor() 注册，属于最外层的
 * "应用拦截器"——每个 call 只走一次(不受内部重试/重定向影响)，最适合做这种
 * "所有请求都要加的东西"：token、公共 header、设备信息等。
 *
 * 这就是 Sandwich/Net 那些库"全局处理"的本质：把每个请求都要做的事收敛到一个
 * 拦截器里，业务代码再也不用每次手动加 header。
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // OkHttp 的 Request 是不可变的，要改只能用 newBuilder() 拷一份再改。
        val builder = chain.request().newBuilder()
            .header("Accept", "application/json")   // 公共头:声明想要 JSON

        // 有 token 才加 Authorization 头(没登录时就不带)
        TokenProvider.current()?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        // ★ 关键:改完后一定要 chain.proceed() 把请求交给链上的下一个拦截器，
        //   否则请求就断在这里、永远发不出去。返回它拿到的 Response 即可。
        return chain.proceed(builder.build())
    }
}
