package com.example.prm391_project.api

import com.example.prm391_project.response.CartResult
import com.example.prm391_project.response.IResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface CartService {
    @GET("carts") // <-- THÊM PHƯƠNG THỨC NÀY CHO GIỎ HÀNG
    suspend fun getCart(@Header("Authorization") token: String): IResponse<CartResult>
}