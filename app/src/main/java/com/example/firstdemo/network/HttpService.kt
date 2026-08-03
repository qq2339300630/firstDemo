package com.example.firstdemo.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * 通用 HTTP 客户端接口 —— App 真正的网络层入口。
 *
 * 和 retrofitstudy.ApiService 的区别：
 *   - ApiService  ：为"学习 Retrofit 源码"而写，每个端点一个带类型的 @GET 方法
 *   - HttpService ：为"业务开发"而写，只有通用的 get/post，具体类型全交给 Repository
 * 这样以后加接口【只改 Repository 一处】，HttpService 永远不用动。
 *
 * ★ 为什么 get 返回 ResponseBody 而不是泛型 T？
 * Retrofit 靠"方法返回类型的反射"决定怎么反序列化，运行时泛型 T 会被类型擦除，
 * 它没法知道该把 JSON 转成 Post 还是 List<Post>。所以这里退回到最原始的 ResponseBody
 * （拿到的就是原始响应流），再由下面的 decode() 在【调用点】用 reified 拿到真实类型去转。
 * 代价：放弃了 Retrofit 的类型安全 Converter，改成手动 Gson 解析。这正是 RxHttp 那类
 * "一个 get 打天下"的库在底层做的事。
 *
 * @Url：让调用方传完整/相对 URL（相对 URL 会拼在 baseUrl 后面），
 *       连查询参数也能直接写进 url，如 "posts?userId=1"，省掉一堆 @Query 声明。
 */
interface HttpService {

    @GET
    suspend fun get(@Url url: String): ResponseBody

    /**
     * 发 POST 请求。
     *
     * ★ 约定：body 只传【对象或 Map】,不要传"已经是 JSON 的字符串"。
     *
     * 因为配了 GsonConverterFactory,Retrofit 会用 Gson 序列化这个 body:
     *   - 传对象/Map           → Gson 转成 JSON,正确
     *       post("posts", mapOf("title" to "hi"))   // body = {"title":"hi"}
     *   - 传裸 JSON 字符串      → Gson 把整个字符串当成一个 JSON 值再包一层引号转义,
     *                            双重编码,服务器会解析失败:
     *       post("posts", "{\"title\":\"hi\"}")      // body = "\"{\\\"title\\\"...\""  ❌
     *
     * 统一在【入口】约束:所有 body 都给对象,由 Gson 统一转,永远正确。
     * (万一真有第三方给的现成 JSON 串要原样发,那是另一回事——需要单独用
     *  RequestBody + application/json,不能走这个方法。)
     */
    @POST
    suspend fun post(@Url url: String, @Body body: Any): ResponseBody
}

/** 复用一个 Gson 实例（线程安全）。 */
val networkGson = Gson()

/**
 * 把原始 ResponseBody 反序列化成目标类型 T。
 *
 * inline + reified：让 T 的真实类型在【每个调用点】被保留下来，
 * 于是 object : TypeToken<T>(){} 能拿到完整泛型（连 List<Comment> 这种也能正确还原），
 * 这是不加 reified 时做不到的（会被擦除成 List<*>）。
 *
 * use { }：读完自动 close ResponseBody，避免连接不归还、资源泄漏
 *          （正好呼应之前讲的"忘了 close 响应体会泄漏"）。
 */
inline fun <reified T> ResponseBody.decode(): T = use { body ->
    val type = object : TypeToken<T>() {}.type
    networkGson.fromJson(body.charStream(), type)
}
