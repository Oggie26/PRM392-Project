package com.example.prm391_project.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://icot.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối
        .readTimeout(30, TimeUnit.SECONDS)   // Thời gian chờ đọc dữ liệu
        .writeTimeout(30, TimeUnit.SECONDS)  // Thời gian chờ ghi dữ liệu
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val cartService:    CartService    = retrofit.create(CartService::class.java)
    val addressService: AddressService = retrofit.create(AddressService::class.java)
    val paymentService: PaymentService = retrofit.create(PaymentService::class.java)

}

