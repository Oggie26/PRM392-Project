package com.example.prm391_project.response

data class OrderItem(
    val id: Int,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Int,
    val totalPrice: Int,
    val thumbnailProduct: String,
    val size: String
)

data class Address(
    val id: Int,
    val name: String,
    val phone: String,
    val city: String,
    val district: String,
    val ward: String,
    val street: String,
    val addressLine: String,
    val isDefault: Boolean
)

data class OrderResult(
    val orderId: Int,
    val totalAmount: Int,
    val status: String,
    val username: String,
    val orderDate: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val address: Address,
    val orderResponseItemList: List<OrderItem>,
    val imageOrderSuccess: String
)
