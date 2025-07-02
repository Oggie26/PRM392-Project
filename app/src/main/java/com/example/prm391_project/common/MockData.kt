package com.example.prm391_project.common
data class CartItem(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String,
    var quantity: Int
)

object MockData {
    val cartItems = mutableListOf(
        CartItem(
            id = 1,
            name = "Áo Thun Nam",
            price = 250000.0,
            imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=300&h=300&fit=crop",
            quantity = 1
        ),
        CartItem(
            id = 2,
            name = "Quần Jean Nữ",
            price = 450000.0,
            imageUrl = "https://images.unsplash.com/photo-1542272604-787c3835535d?w=300&h=300&fit=crop",
            quantity = 2
        ),
        CartItem(
            id = 3,
            name = "Áo Sơ Mi",
            price = 320000.0,
            imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=300&h=300&fit=crop",
            quantity = 1
        ),
        CartItem(
            id = 4,
            name = "Áo Sơ Mi",
            price = 320000.0,
            imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=300&h=300&fit=crop",
            quantity = 1
        ),
        CartItem(
            id = 5,
            name = "Áo Sơ Mi",
            price = 320000.0,
            imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=300&h=300&fit=crop",
            quantity = 1
        ),
        CartItem(
            id = 6,
            name = "Áo Sơ Mi",
            price = 320000.0,
            imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=300&h=300&fit=crop",
            quantity = 1
        ),
        CartItem(
            id = 7,
            name = "Áo Sơ Mi",
            price = 320000.0,
            imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=300&h=300&fit=crop",
            quantity = 1
        )
    )
}