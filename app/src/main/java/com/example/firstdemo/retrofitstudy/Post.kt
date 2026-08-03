package com.example.firstdemo.retrofitstudy

/**
 * 响应体对应的数据模型。
 *
 * Gson Converter 会把 JSON（ResponseBody）反序列化成这个对象。
 * 断点想看「响应怎么变成对象」时，可以在这个类被赋值的地方，
 * 或在 GsonResponseBodyConverter.convert() 里下断点。
 */
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String,
)
