package com.example.prm391_project.request

import java.time.LocalDate

data class UserProfileRequest(
    val avatar: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String? = null, // Vẫn cảnh báo về việc gửi mật khẩu
    val birthday: String? = null, // Giữ là String cho TextField
    val phone: String? = null,
    val point: Int? = null,
    val address: String? = null,
    val gender: String? = null,
    val role: String? = null,
    val status: String? = null
)