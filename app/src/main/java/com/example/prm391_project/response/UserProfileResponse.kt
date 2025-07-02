package com.example.prm391_project.response

import com.google.gson.annotations.SerializedName // Cần nếu dùng Gson và tên thuộc tính Kotlin khác tên JSON key

data class UserProfileResponse(
    // Các trường này nằm trực tiếp trong "result" của JSON
    val id: String?, // Có thể là null
    val avatar: String?, // Có thể là null
    val fullName: String?, // Có thể là null
    val email: String?, // Có thể là null
    val username: String?, // Có thể là null
    val birthday: String?, // Có thể là null (hoặc LocalDate/Date nếu parse)
    val phone: String?, // Có thể là null
    val point: Int?, // Có thể là null
    val address: String?, // Có thể là null
    val gender: String?, // Có thể là null
    val role: String?, // Có thể là null
    val status: String? // Có thể là null
)