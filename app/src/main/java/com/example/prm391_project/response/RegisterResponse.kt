package com.example.prm391_project.response

import java.time.LocalDate

data class RegisterResponse(
    val code: Int,
    val data: RegisterData?,
    val message: String
)

data class RegisterData(
    val userId: Int,
    val phone: String,
    val email: String,
    val fullName: String
)