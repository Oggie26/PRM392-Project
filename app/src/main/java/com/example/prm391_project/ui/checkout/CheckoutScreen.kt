package com.example.prm391_project.ui.checkout

import TokenManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prm391_project.R
import com.example.prm391_project.Screen
import com.example.prm391_project.api.ApiClient
import com.example.prm391_project.response.*
import com.example.prm391_project.screen.user.CartItem
import com.example.prm391_project.screen.user.toCartItem
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun CheckoutScreen(navController: NavController) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val tokenMgr = remember { TokenManager(context.applicationContext) }

    val viewModel = remember {
        CheckoutViewModel(
            paymentService = ApiClient.paymentService,
            tokenManager   = tokenMgr
        )
    }
    val paymentState by viewModel.uiState.collectAsState()

    // State
    var cartItems       by remember { mutableStateOf(emptyList<CartItem>()) }
    var total           by remember { mutableStateOf(0) }
    var cartId          by remember { mutableStateOf(0L) }
    var addresses       by remember { mutableStateOf(emptyList<AddressForm>()) }
    var selectedAddress by remember { mutableStateOf(0) }
    var isLoading       by remember { mutableStateOf(true) }
    var errorMsg        by remember { mutableStateOf<String?>(null) }
    var selectedPayment by remember { mutableStateOf("Thanh toán khi nhận hàng") }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showAddDialog     by remember { mutableStateOf(false) }
    var showEditDialog    by remember { mutableStateOf(false) }
    var editAddress       by remember { mutableStateOf<AddressForm?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Load data
    fun loadAll() {
        scope.launch {
            isLoading = true; errorMsg = null
            val raw = tokenMgr.getToken().orEmpty()
            if (raw.isBlank()) {
                errorMsg = "Bạn chưa đăng nhập."; isLoading = false; return@launch
            }
            val auth = "Bearer $raw"
            try {
                ApiClient.cartService.getCart(auth).let { resp ->
                    if (resp.code == 200 && resp.data != null) {
                        cartId    = resp.data.cartId?.toLong() ?: 0L
                        cartItems = resp.data.items.orEmpty().mapNotNull { (it as CartItemDto).toCartItem() }
                        total     = resp.data.totalPrice?.toInt() ?: 0
                    } else errorMsg = resp.message
                }
                ApiClient.addressService.getAddresses(auth).let { resp ->
                    if (resp.code == 200 && resp.data != null) {
                        addresses = resp.data.map(AddressDto::toForm)
                        resp.data.firstOrNull { it.isDefault == true }?.id?.let { selectedAddress = it }
                    } else errorMsg = resp.message
                }
            } catch (e: HttpException) {
                errorMsg = "Server lỗi: ${e.code()}"
            } catch (e: IOException) {
                errorMsg = "Không thể kết nối server."
            } finally {
                isLoading = false
            }
        }
    }
    LaunchedEffect(Unit) { loadAll() }

    // Handle payment result and redirect for VNPAY
    // Use LocalUriHandler to open external URLs from Compose
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is CheckoutViewModel.UiState.Success -> {
                val data = (paymentState as CheckoutViewModel.UiState.Success).data
                if (selectedPayment == "Ví điện tử" && !data.redirectUrl.isNullOrEmpty()) {
                    // Mở trình duyệt tới URL VNPAY
                    uriHandler.openUri(data.redirectUrl)
                } else {
                    // Modal thành công cho COD hoặc trường hợp ví không có URL
                    showSuccessDialog = true
                }
            }
            is CheckoutViewModel.UiState.Error -> {
                errorMsg = (paymentState as CheckoutViewModel.UiState.Error).message
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading ->  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.trackloading)
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(200.dp)
                    )

                }
            }
            errorMsg != null -> Text(
                text = errorMsg!!,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error
            )
            else -> CheckoutContent(
                cartItems       = cartItems,
                addresses       = addresses,
                total           = total,
                selectedAddress = selectedAddress,
                onSelectAddress = { selectedAddress = it },
                onAddAddress    = { showAddDialog = true },
                onEditAddress   = { editAddress = it; showEditDialog = true },
                onDeleteAddress = { id ->
                    scope.launch {
                        val auth = "Bearer ${tokenMgr.getToken().orEmpty()}"
                        try {
                            ApiClient.addressService.deleteAddress(auth, id)
                            loadAll()
                        } catch (ex: Exception) {
                            errorMsg = "Xóa địa chỉ thất bại: ${ex.message}"
                        }
                    }
                },
                selectedPayment = selectedPayment,
                onSelectPayment = { selectedPayment = it },
                isProcessing    = paymentState is CheckoutViewModel.UiState.Loading,
                error           = errorMsg,
                onPlaceOrder    = {
                    if (selectedPayment == "Ví điện tử") showWalletDialog = true
                    else {
                        val method = if (selectedPayment.startsWith("Thanh toán khi nhận hàng")) "COD" else selectedPayment
                        viewModel.pay(cartId, selectedAddress.toLong(), method)
                    }
                },
                onNavigateBack  = { navController.popBackStack() }
            )
        }

        if (showAddDialog) AddAddressDialog(
            onDismiss    = { showAddDialog = false },
            onAddAddress = { form ->
                scope.launch {
                    val auth = "Bearer ${tokenMgr.getToken().orEmpty()}"
                    try {
                        ApiClient.addressService.addAddress(auth, form.toDto())
                        showAddDialog = false; loadAll()
                    } catch (ex: Exception) {
                        errorMsg = "Thêm địa chỉ thất bại: ${ex.message}"
                    }
                }
            }
        )

        if (showEditDialog && editAddress != null) AddAddressDialog(
            initialAddress = editAddress,
            onDismiss      = { showEditDialog = false; editAddress = null },
            onAddAddress   = { form ->
                scope.launch {
                    val auth = "Bearer ${tokenMgr.getToken().orEmpty()}"
                    try {
                        ApiClient.addressService.updateAddress(auth, form.id, form.toDto())
                        showEditDialog = false; editAddress = null; loadAll()
                    } catch (ex: Exception) {
                        errorMsg = "Cập nhật địa chỉ thất bại: ${ex.message}"
                    }
                }
            }
        )

        if (showWalletDialog) DigitalWalletDialog(
            amount = total,
            onDismiss = { showWalletDialog = false },
            onPaymentSuccess = {
                showWalletDialog = false
                viewModel.pay(cartId, selectedAddress.toLong(), "VNPAY")
            }
        )

        if (showSuccessDialog) OrderSuccessDialog {
            showSuccessDialog = false
            navController.navigate(Screen.Home.route) { popUpTo("checkout") { inclusive = true } }
        }
    }
}

private fun AddressForm.toDto() = AddressDto(
    id          = if (id == 0) null else id,
    name        = name,
    phone       = phone,
    city        = city,
    district    = district,
    ward        = ward,
    street      = street,
    addressLine = addressLine,
    isDefault   = isDefault
)
