package com.example.prm391_project.config

import com.example.prm391_project.api.AuthService
import com.example.prm391_project.api.CartService
import com.example.prm391_project.api.ProductService
import com.example.prm391_project.api.OrderService // Thêm import
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_API_URL = "https://icot.onrender.com/api/"
    const val BASE_CARTS_URL = "https://icot.onrender.com/carts"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val apiRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BASE_API_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())

    val authService: AuthService by lazy {
        apiRetrofitBuilder
            .build()
            .create(AuthService::class.java)
    }

    val cartService: CartService by lazy {
        Retrofit.Builder()
            .baseUrl("https://icot.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CartService::class.java)
    }

    val productService: ProductService by lazy {
        Retrofit.Builder()
            .baseUrl("https://icot.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductService::class.java)
    }

    // ✅ Thêm instance cho OrderService
    val orderService: OrderService by lazy {
        Retrofit.Builder()
            .baseUrl("https://icot.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OrderService::class.java)
    }
}
