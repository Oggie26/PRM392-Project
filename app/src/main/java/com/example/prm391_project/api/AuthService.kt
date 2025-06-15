package com.example.prm391_project.api

import com.example.prm391_project.request.LoginRequest
import com.example.prm391_project.request.RegisterRequest
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.LoginResponse
import com.example.prm391_project.response.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService  {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): IResponse<LoginResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): IResponse<RegisterResponse>
}