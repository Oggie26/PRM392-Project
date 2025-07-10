package com.example.prm391_project.response

data class ProductDetailResponse(
    val id: String?,
    val productName: String,
    val price: Double,
    val description: String,
    val imageThumbnail: String,
    val sizes: List<ProductSize>,
    val status: String,
    val category: Category?,
    val fabric: Fabric?,
    val typePrint: TypePrint?,
    val feedbacks: List<Feedback>,
    val images: List<ProductImage>,
    val createdAt: String?,
    val updatedAt: String?
)

data class ProductSize(
    val id: Int,
    val size: String
)

data class Category(
    val id: Int,
    val categoryName: String,
    val description: String,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?,
    val isDeleted: Boolean
)

data class Fabric(
    val id: Int,
    val fabricName: String,
    val price: Double,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?,
    val isDeleted: Boolean
)

data class TypePrint(
    val id: Int,
    val printName: String,
    val price: Double,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?,
    val isDeleted: Boolean
)

data class Feedback(
    val id: Int,
    val description: String,
    val rating: Int
)

data class ProductImage(
    val id: String,
    val image: String,
    val isDeleted: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)