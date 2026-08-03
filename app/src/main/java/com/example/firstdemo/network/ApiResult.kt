package com.example.firstdemo.network

/**
 * 网络请求结果的统一封装。
 *
 * 为什么需要它？
 * ------------------------------------------------------------
 * Retrofit 的 suspend 接口只有两种结局：正常返回 T，或者抛异常。
 * 但"抛异常"其实混了两类完全不同的情况：
 *   1) 服务器有响应，只是状态码非 2xx（404、500…）—— HttpException，能拿到错误码/错误体
 *   2) 压根没连上服务器（断网、超时、DNS 失败）—— IOException，什么响应都没有
 * 如果上层只写 catch(e: Exception)，这两类就被拍平成一句 e.message，
 * 界面无法区分"服务器说资源不存在"和"你没网"，提示自然也做不细。
 *
 * 所以我们把结局显式建模成【互斥的三种】，用密封接口穷举：
 *   - Success   ：成功，带数据
 *   - Error     ：服务器返回了非 2xx（有 code、有 message）
 *   - Exception ：请求根本没成功（断网/超时/意外崩溃）
 *
 * 密封接口的好处：上层用 when 处理时，编译器会强制你覆盖所有分支，
 * 少写一种情况就编译不过 —— 漏处理错误这种低级 bug 直接被挡在编译期。
 *
 * out T：协变。让 ApiResult<Post> 能赋值给 ApiResult<Any>，
 *        同时也让不带数据的 Error/Exception 可以用 Nothing 作为类型参数（见下）。
 */
sealed interface ApiResult<out T> {

    /** 成功：拿到了业务数据。 */
    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * 业务/服务器错误：连上了服务器，但状态码非 2xx。
     * code    —— HTTP 状态码（404、500…），界面可据此做不同处理
     * message —— 给用户看的错误描述
     *
     * 用 ApiResult<Nothing> 是因为错误分支不携带任何成功数据。
     * Nothing 是所有类型的子类型，配合 out T，Error 能当成任意 ApiResult<T> 使用，
     * 不用为每种数据类型各写一个 Error。
     */
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>

    /**
     * 异常：请求没能完成（IOException 断网/超时，或其它意外 Throwable）。
     * 和 Error 的本质区别：Error 是"服务器拒绝了你"，Exception 是"话都没说上"。
     */
    data class Exception(val throwable: Throwable) : ApiResult<Nothing>
}
