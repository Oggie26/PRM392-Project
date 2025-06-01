package com.example.prm391_project.response

import kotlinx.serialization.Serializable

data class LoginResponse(
    val code: Int,
    val data: LoginData?,
    val message: String
)

data class LoginData(
    val token: String
)