package com.example.prm391_project.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prm391_project.api.PaymentService
import com.example.prm391_project.response.IResponse
import com.example.prm391_project.response.VnpayRedirectDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class CheckoutViewModel(
    private val paymentService: PaymentService,
    private val tokenManager: TokenManager
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val redirectUrl: String?, val code: Int, val message: String?) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun pay(cartId: Long, addressId: Long, paymentMethod: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val token = tokenManager.getToken().orEmpty()
                val resp = paymentService.createOrder(
                    token = "Bearer $token",
                    cartId = cartId,
                    addressId = addressId,
                    paymentMethod = paymentMethod
                )
                if (resp.code == 200 && resp.data != null) {
                    _uiState.value = UiState.Success(resp.data.redirectUrl, resp.code, resp.message)
                } else {
                    _uiState.value = UiState.Error(resp.message ?: "Lỗi thanh toán: Mã ${resp.code}")
                }
            } catch (e: HttpException) {
                _uiState.value = UiState.Error("Lỗi server: ${e.code()}")
            } catch (e: IOException) {
                _uiState.value = UiState.Error("Không thể kết nối server")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Lỗi không xác định")
            }
        }
    }
}