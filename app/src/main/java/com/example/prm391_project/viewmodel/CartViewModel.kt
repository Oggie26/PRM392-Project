//package com.example.prm391_project.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.prm391_project.common.CartItem
//import com.example.prm391_project.repository.CartRepository
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//
//data class CartUiState(
//    val items: List<CartItem> = emptyList(),
//    val isLoading: Boolean = false,
//    val error: String? = null,
//    val totalItems: Int = 0,
//    val totalPrice: Double = 0.0
//)
//
//class CartViewModel(
//    private val cartRepository: CartRepository = CartRepository()
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(CartUiState())
//    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
//
//    init {
//        // Lắng nghe thay đổi từ repository
//        viewModelScope.launch {
//            cartRepository.cartItems.collect { items ->
//                updateUiState(items)
//            }
//        }
//    }
//
//    fun initCart(userId: String) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
//
//            // Bắt đầu lắng nghe thay đổi real-time
//            cartRepository.listenToCartChanges(userId)
//
//            // Load dữ liệu ban đầu
//            cartRepository.getCartItems(userId)
//                .onSuccess { items ->
//                    updateUiState(items)
//                }
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        error = "Không thể tải giỏ hàng: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    fun addToCart(userId: String, item: CartItem) {
//        viewModelScope.launch {
//            cartRepository.addToCart(userId, item)
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        error = "Không thể thêm sản phẩm: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    fun increaseQuantity(userId: String, itemId: Int) {
//        viewModelScope.launch {
//            val currentItem = _uiState.value.items.find { it.id == itemId }
//            if (currentItem != null) {
//                cartRepository.updateItemQuantity(userId, itemId, currentItem.quantity + 1)
//                    .onFailure { exception ->
//                        _uiState.value = _uiState.value.copy(
//                            error = "Không thể cập nhật số lượng: ${exception.message}"
//                        )
//                    }
//            }
//        }
//    }
//
//    fun decreaseQuantity(userId: String, itemId: Int) {
//        viewModelScope.launch {
//            val currentItem = _uiState.value.items.find { it.id == itemId }
//            if (currentItem != null) {
//                val newQuantity = (currentItem.quantity - 1).coerceAtLeast(0)
//                cartRepository.updateItemQuantity(userId, itemId, newQuantity)
//                    .onFailure { exception ->
//                        _uiState.value = _uiState.value.copy(
//                            error = "Không thể cập nhật số lượng: ${exception.message}"
//                        )
//                    }
//            }
//        }
//    }
//
//    fun removeFromCart(userId: String, itemId: Int) {
//        viewModelScope.launch {
//            cartRepository.removeFromCart(userId, itemId)
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        error = "Không thể xóa sản phẩm: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    fun clearCart(userId: String) {
//        viewModelScope.launch {
//            cartRepository.clearCart(userId)
//                .onFailure { exception ->
//                    _uiState.value = _uiState.value.copy(
//                        error = "Không thể xóa giỏ hàng: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    fun clearError() {
//        _uiState.value = _uiState.value.copy(error = null)
//    }
//
//    private fun updateUiState(items: List<CartItem>) {
//        val totalItems = items.sumOf { it.quantity }
//        val totalPrice = items.sumOf { it.price * it.quantity }
//
//        _uiState.value = _uiState.value.copy(
//            items = items,
//            isLoading = false,
//            totalItems = totalItems,
//            totalPrice = totalPrice,
//            error = null
//        )
//    }
//}