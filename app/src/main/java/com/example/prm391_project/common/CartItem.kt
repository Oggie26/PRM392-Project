//package com.example.prm391_project.common
//
//data class CartItem(
//    val id: Int = 0,
//    val name: String = "",
//    val price: Double = 0.0,
//    val imageUrl: String = "",
//    var quantity: Int = 0
//) {
//    // Constructor rỗng cho Firestore
//    constructor() : this(0, "", 0.0, "", 0)
//
//    fun toMap(): Map<String, Any> {
//        return hashMapOf(
//            "id" to id,
//            "name" to name,
//            "price" to price,
//            "imageUrl" to imageUrl,
//            "quantity" to quantity
//        )
//    }
//
//    companion object {
//        fun fromMap(map: Map<String, Any>): CartItem {
//            return CartItem(
//                id = (map["id"] as? Number)?.toInt() ?: 0,
//                name = map["name"] as? String ?: "",
//                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
//                imageUrl = map["imageUrl"] as? String ?: "",
//                quantity = (map["quantity"] as? Number)?.toInt() ?: 0
//            )
//        }
//    }
//}