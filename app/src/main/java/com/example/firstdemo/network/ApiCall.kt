package com.example.firstdemo.network

import retrofit2.HttpException
import java.io.IOException

/**
 * 统一网络请求包裹器 —— 整个框架的核心。
 *
 * 作用：把"发一次请求 + try/catch 分类异常"这段重复逻辑收敛到这一处，
 * 让每个 Repository 方法只需一行 apiCall { api.xxx() }，
 * 再也不用在各处手写 try/catch，也不会有人漏 catch。
 *
 * 用法：
 *   suspend fun getPost(id: Int): ApiResult<Post> = apiCall { api.getPost(id) }
 *
 * @param block 真正发请求的挂起 lambda（通常就是调一个 Retrofit suspend 方法）
 * @return 永远返回 ApiResult，绝不抛异常 —— 上层拿到的一定是三选一的确定结果
 */
suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        // block() 正常返回 = 请求成功（Retrofit 对 2xx 才会正常返回）
        ApiResult.Success(block())
    } catch (e: HttpException) {
        // 非 2xx：Retrofit 的 suspend 方法会抛 HttpException。
        // 这里能拿到 HTTP 状态码，是"服务器拒绝了你"这一类。
        ApiResult.Error(
            code = e.code(),
            message = mapHttpError(e.code()),
        )
    } catch (e: IOException) {
        // 断网、超时、DNS 失败等：连服务器都没连上，属于"话没说上"这一类。
        // 注意：一定要放在 Throwable 之前单独 catch，否则会被下面的兜底吞掉、丢失语义。
        ApiResult.Exception(e)
    } catch (e: Throwable) {
        // 兜底：任何没预料到的异常（JSON 解析失败、空指针…）也不让它崩到上层。
        // 但要放行协程取消异常 —— 取消是正常的协作机制，不能被当成错误吞掉。
        if (e is kotlinx.coroutines.CancellationException) throw e
        ApiResult.Exception(e)
    }
}

/**
 * 把 HTTP 状态码翻译成给用户看的文案。
 * 真实项目里这里通常还会解析响应体里的业务错误信息，这里先按状态码给个友好提示。
 */
private fun mapHttpError(code: Int): String = when (code) {
    401 -> "登录已过期，请重新登录"
    403 -> "没有权限访问"
    404 -> "请求的资源不存在"
    in 500..599 -> "服务器开小差了（$code）"
    else -> "请求失败（$code）"
}
