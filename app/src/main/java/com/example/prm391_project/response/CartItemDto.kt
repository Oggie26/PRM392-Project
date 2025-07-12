package com.example.prm391_project.response

import com.google.gson.annotations.SerializedName

data class CartItemDto(
    @SerializedName("productId")      val productId: String?,
    @SerializedName("productName")    val productName: String?,
    @SerializedName("color")          val color: String?,
    @SerializedName("size")           val size: String?,
    @SerializedName("image")          val image: String?,
    @SerializedName("price")          val price: Double?,
    @SerializedName("quantity")       val quantity: Int?,
    @SerializedName("totalItemPrice") val totalItemPrice: Double?
)
