//package com.example.prm391_project.repository
//
//import com.example.prm391_project.common.CartItem
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.tasks.await
//
//class CartRepository {
//    private val db: FirebaseFirestore = Firebase.firestore
//    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
//    val cartItems: Flow<List<CartItem>> = _cartItems.asStateFlow()
//
//    // Lấy tất cả items trong cart của user
//    suspend fun getCartItems(userId: String): Result<List<CartItem>> {
//        return try {
//            val snapshot = db.collection("users")
//                .document(userId)
//                .collection("cart")
//                .get()
//                .await()
//
//            val items = snapshot.documents.mapNotNull { doc ->
//                val data = doc.data
//                if (data != null) {
//                    CartItem.fromMap(data)
//                } else null
//            }
//
//            _cartItems.value = items
//            Result.success(items)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Thêm item vào cart
//    suspend fun addToCart(userId: String, item: CartItem): Result<Unit> {
//        return try {
//            // Kiểm tra xem item đã tồn tại chưa
//            val existingItem = getExistingItem(userId, item.id)
//
//            if (existingItem != null) {
//                // Nếu đã tồn tại, tăng quantity
//                val newQuantity = existingItem.quantity + item.quantity
//                updateItemQuantity(userId, item.id, newQuantity)
//            } else {
//                // Nếu chưa tồn tại, thêm mới
//                db.collection("users")
//                    .document(userId)
//                    .collection("cart")
//                    .document(item.id.toString())
//                    .set(item.toMap())
//                    .await()
//            }
//
//            // Refresh cart items
//            getCartItems(userId)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Cập nhật quantity của item
//    suspend fun updateItemQuantity(userId: String, itemId: Int, quantity: Int): Result<Unit> {
//        return try {
//            if (quantity <= 0) {
//                // Nếu quantity <= 0, xóa item
//                removeFromCart(userId, itemId)
//            } else {
//                db.collection("users")
//                    .document(userId)
//                    .collection("cart")
//                    .document(itemId.toString())
//                    .update("quantity", quantity)
//                    .await()
//
//                // Refresh cart items
//                getCartItems(userId)
//            }
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Xóa item khỏi cart
//    suspend fun removeFromCart(userId: String, itemId: Int): Result<Unit> {
//        return try {
//            db.collection("users")
//                .document(userId)
//                .collection("cart")
//                .document(itemId.toString())
//                .delete()
//                .await()
//
//            // Refresh cart items
//            getCartItems(userId)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Xóa tất cả items trong cart
//    suspend fun clearCart(userId: String): Result<Unit> {
//        return try {
//            val snapshot = db.collection("users")
//                .document(userId)
//                .collection("cart")
//                .get()
//                .await()
//
//            val batch = db.batch()
//            snapshot.documents.forEach { doc ->
//                batch.delete(doc.reference)
//            }
//            batch.commit().await()
//
//            _cartItems.value = emptyList()
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    // Lấy item hiện tại để kiểm tra
//    private suspend fun getExistingItem(userId: String, itemId: Int): CartItem? {
//        return try {
//            val doc = db.collection("users")
//                .document(userId)
//                .collection("cart")
//                .document(itemId.toString())
//                .get()
//                .await()
//
//            if (doc.exists()) {
//                doc.data?.let { CartItem.fromMap(it) }
//            } else null
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    // Lắng nghe thay đổi real-time
//    fun listenToCartChanges(userId: String) {
//        db.collection("users")
//            .document(userId)
//            .collection("cart")
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null) {
//                    val items = snapshot.documents.mapNotNull { doc ->
//                        val data = doc.data
//                        if (data != null) {
//                            CartItem.fromMap(data)
//                        } else null
//                    }
//                    _cartItems.value = items
//                }
//            }
//    }
//}