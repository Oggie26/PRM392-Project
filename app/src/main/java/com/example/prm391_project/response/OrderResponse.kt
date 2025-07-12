package com.example.prm391_project.response

data class OrderHistoryResponse(
    val code: Int,
    val message: String,
    val result: List<OrderResult>,
    val redirectUrl: String
)

data class OrderDetailResponse(
    val code: Int,
    val message: String,
    val result: OrderResult,
    val redirectUrl: String
)
