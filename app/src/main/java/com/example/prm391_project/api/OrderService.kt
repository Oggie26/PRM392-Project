package com.example.prm391_project.api

import com.example.prm391_project.response.OrderDetailResponse
import com.example.prm391_project.response.OrderHistoryResponse
import com.example.prm391_project.response.IResponse
import retrofit2.Response
import retrofit2.http.*

interface OrderService {

    // Lấy danh sách lịch sử đơn hàng với phân trang
    @GET("orders/history-order")
    suspend fun getOrderHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null // Filter by status if needed
    ): Response<OrderHistoryResponse>

    // Lấy chi tiết đơn hàng theo ID
    @GET("orders/{id}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Path("id") orderId: Long
    ): Response<OrderDetailResponse>

    // Cập nhật trạng thái đơn hàng (uncommented and improved)
//    @PATCH("orders/{id}")
//    suspend fun updateOrderStatus(
//        @Header("Authorization") token: String,
//        @Path("id") id: Long,
//        @Body body: Map<String, String>
//    ): Response<IResponse>


}