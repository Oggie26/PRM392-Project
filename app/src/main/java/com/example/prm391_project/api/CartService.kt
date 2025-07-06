package com.example.prm391_project.api

import com.example.prm391_project.response.CartResult
import com.example.prm391_project.response.IResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query // <-- Import Query

interface CartService {
    @GET("carts")
    suspend fun getCart(@Header("Authorization") token: String): IResponse<CartResult>

    @PATCH("carts/update-quantity")
    suspend fun updateQuantity(
        @Header("Authorization") token: String,
        @Query("productId") productId: String, // <-- Thêm productId
        @Query("quantity") quantity: Int      // <-- Thêm quantity
    ): IResponse<CartResult>
}