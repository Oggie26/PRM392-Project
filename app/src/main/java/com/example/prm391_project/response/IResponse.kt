// com.example.prm391_project.response/IResponse.kt
package com.example.prm391_project.response

import com.google.gson.annotations.SerializedName // CẦN THÊM IMPORT NÀY

data class IResponse<T>(
    var code: Int,
    @SerializedName("result") // <-- THÊM DÒNG NÀY: Ánh xạ trường JSON "result" vào thuộc tính "data" của Kotlin
    val data: T?,
    val message: String,
    val redirectUrl: String?
)