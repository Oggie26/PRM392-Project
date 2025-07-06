package com.example.prm391_project.api

import com.example.prm391_project.request.LoginRequest
import com.example.prm391_project.request.RegisterRequest
import com.example.prm391_project.request.UserProfileRequest
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.LoginResponse
import com.example.prm391_project.response.RegisterResponse
import com.example.prm391_project.response.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthService  {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): IResponse<LoginResponse>

    @POST("/api/auth/register/mobile")
    suspend fun register(@Body request: RegisterRequest): IResponse<RegisterResponse>

    @GET("users/profile")
    // <-- THÊM PHƯƠNG THỨC NÀY
    suspend fun getUserProfile(@Header("Authorization") token: String): IResponse<UserProfileResponse>

    @PUT("users/{userId}")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body request: UserProfileRequest
    ): IResponse<UserProfileResponse>
}