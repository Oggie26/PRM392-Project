package com.example.prm391_project.api

import com.example.prm391_project.response.AddressDto
import com.example.prm391_project.response.IResponse
import retrofit2.http.*

interface AddressService {
    @GET("api/addresses")
    suspend fun getAddresses(@Header("Authorization") token: String): IResponse<List<AddressDto>>

    @POST("api/addresses")
    suspend fun addAddress(
        @Header("Authorization") token: String,
        @Body body: AddressDto
    ): IResponse<Unit>

    @PUT("api/addresses/{id}")
    suspend fun updateAddress(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: AddressDto
    ): IResponse<Unit>

    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): IResponse<Unit>
}
