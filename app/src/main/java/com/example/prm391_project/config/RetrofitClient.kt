package com.example.prm391_project.config

import com.example.prm391_project.api.AuthService
import com.example.prm391_project.api.CartService
import com.example.prm391_project.api.ProductService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Base URL cho các API có tiền tố "/api/"
    private const val BASE_API_URL = "https://icot.onrender.com/api/"

    // Base URL riêng cho các API không có tiền tố "/api/" (như carts)
    const val BASE_CARTS_URL = "https://icot.onrender.com/carts" // <-- Đảm bảo đây là `const val`

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Retrofit Builder cho các API theo chuẩn BASE_API_URL
    private val apiRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BASE_API_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())

    // Instance cho AuthService
    val authService: AuthService by lazy {
        apiRetrofitBuilder
            .build()
            .create(AuthService::class.java)
    }

    // Instance cho CartService
    val cartService: CartService by lazy {
        // Sử dụng base URL tổng quát hơn vì CartService.getCart dùng @Url
        Retrofit.Builder()
            .baseUrl("https://icot.onrender.com/") // Base URL tổng quát để khởi tạo Retrofit cho CartService
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CartService::class.java)
    }

    //Instance cho ProductService
    val productService: ProductService by lazy {
        Retrofit.Builder()
            .baseUrl("https://icot.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductService::class.java)
    }

    // Bạn có thể thêm các service khác ở đây
}