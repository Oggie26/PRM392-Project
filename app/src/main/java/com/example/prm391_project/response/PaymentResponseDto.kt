package com.example.prm391_project.response

data class PaymentResponseDto(
    val orderId: Long,
    val totalAmount: Long,
    val status: String,
    val username: String,
    val orderDate: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val address: AddressDto,
    val orderResponseItemList: List<OrderItemDto>,
    val imageOrderSuccess: String
)

data class OrderItemDto(
    val id: Long,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Long,
    val totalPrice: Long,
    val thumbnailProduct: String,
    val size: String
)
