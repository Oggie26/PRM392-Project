
package com.example.prm391_project.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Đối tượng đơn giản để giữ state số lượng sản phẩm trong giỏ hàng
object CartStateHolder {
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    fun updateCartItemCount(count: Int) {
        _cartItemCount.value = count
    }
}