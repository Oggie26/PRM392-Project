package com.example.prm391_project.config

import com.example.prm391_project.api.AuthService
import com.example.prm391_project.api.CartService
import com.example.prm391_project.api.ProductService
import com.example.prm391_project.api.OrderService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_API_URL = "https://icot.onrender.com/api/"
    const val BASE_CARTS_URL = "https://icot.onrender.com/carts"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ✅ Updated OkHttpClient with increased timeout values
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)    // Connection timeout
        .readTimeout(60, TimeUnit.SECONDS)       // Read timeout
        .writeTimeout(60, TimeUnit.SECONDS)      // Write timeout
        .callTimeout(90, TimeUnit.SECONDS)       // Overall call timeout
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

    val orderService: OrderService by lazy {
        Retrofit.Builder()
            .baseUrl("https://icot.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OrderService::class.java)
    }
}