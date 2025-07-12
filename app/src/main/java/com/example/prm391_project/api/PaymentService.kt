package com.example.prm391_project.api
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.PaymentResponseDto
import retrofit2.http.*

interface PaymentService {
    @POST("api/payment")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Query("cartId") cartId: Long,
        @Query("addressId") addressId: Long,
        @Query("paymentMethod") paymentMethod: String
    ): IResponse<PaymentResponseDto>
}
