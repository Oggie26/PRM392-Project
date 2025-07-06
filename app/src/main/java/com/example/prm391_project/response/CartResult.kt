package com.example.prm391_project.response

import com.google.gson.annotations.SerializedName

data class CartResult(
    @SerializedName("cartId") val cartId: Int?,
    @SerializedName("username") val username: String?,
    @SerializedName("items") val items: List<CartItemDto>?, // Sử dụng CartItemDto ở đây
    @SerializedName("totalPrice") val totalPrice: Double?
)