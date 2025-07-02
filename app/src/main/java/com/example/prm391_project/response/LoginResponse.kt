// com.example.prm391_project.response/LoginResponse.kt
package com.example.prm391_project.response

// import kotlinx.serialization.Serializable // Nếu bạn dùng kotlinx.serialization, giữ lại.
// Nếu bạn chỉ dùng Gson, có thể bỏ qua

data class LoginResponse(
    // Xóa code và message ở đây, vì chúng đã được xử lý ở IResponse
    // val code: Int,
    // val message: String,

    // Thay đổi từ 'data: LoginData?' thành các thuộc tính trực tiếp từ JSON "result"
    val token: String,
    val fullName: String, // Thêm trường này nếu nó luôn có trong JSON
    val role: String // Thêm trường này nếu nó luôn có trong JSON
)

// Xóa lớp LoginData vì nó không còn cần thiết.
// data class LoginData(
//     val token: String
// )