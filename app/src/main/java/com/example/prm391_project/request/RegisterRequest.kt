package com.example.prm391_project.request

import java.time.LocalDate

data class RegisterRequest(
    val username: String,
    val password: String,
    val birthday: LocalDate,
    val phone: String,
    val email: String,
    val gender: String,
    val fullName: String
)
