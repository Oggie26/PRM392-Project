package com.example.prm391_project.api

import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.ProductDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductService {
    @GET("api/products/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: String,
    ): IResponse<ProductDetailResponse>
}