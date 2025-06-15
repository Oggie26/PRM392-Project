package com.example.prm391_project.response

data class IResponse<T>(
    var code: Int,
    val data: T?,
    val message: String
)
