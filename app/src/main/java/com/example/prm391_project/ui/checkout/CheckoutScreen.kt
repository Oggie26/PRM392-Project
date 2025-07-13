package com.example.prm391_project.ui.checkout

import TokenManager
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val scope = rememberCoroutineScope()
    val tokenMgr = remember { TokenManager(context.applicationContext) }

    // State
    var cartItems by remember { mutableStateOf(emptyList<CartItem>()) }
    var total by remember { mutableStateOf(0) }
    var cartId by remember { mutableStateOf(0L) }
    var addresses by remember { mutableStateOf(emptyList<AddressForm>()) }
    var selectedAddress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedPayment by remember { mutableStateOf("Thanh toán khi nhận hàng") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editAddress by remember { mutableStateOf<AddressForm?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentUrl by remember { mutableStateOf<String?>(null) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Load data function
    fun loadAll() {
        scope.launch {
            isLoading = true
            errorMsg = null
            val raw = tokenMgr.getToken().orEmpty()
            if (raw.isBlank()) {
                errorMsg = "Bạn chưa đăng nhập."
                isLoading = false
                return@launch
            }
            val auth = "Bearer $raw"
            try {
                // Load cart
                val cartResponse = ApiClient.cartService.getCart(auth)
                if (cartResponse.code == 200 && cartResponse.data != null) {
                    cartId = cartResponse.data.cartId?.toLong() ?: 0L
                    cartItems = cartResponse.data.items.orEmpty()
                        .mapNotNull { (it as? CartItemDto)?.toCartItem() }
                    total = cartResponse.data.totalPrice?.toInt() ?: 0
                } else {
                    errorMsg = cartResponse.message
                }

                // Load addresses
                val addressResponse = ApiClient.addressService.getAddresses(auth)
                if (addressResponse.code == 200 && addressResponse.data != null) {
                    addresses = addressResponse.data.map(AddressDto::toForm)
                    addressResponse.data.firstOrNull { it.isDefault == true }?.id?.let {
                        selectedAddress = it
                    }
                } else {
                    errorMsg = addressResponse.message
                }
            } catch (e: HttpException) {
                errorMsg = "Lỗi server: ${e.code()}"
            } catch (e: IOException) {
                errorMsg = "Không thể kết nối server."
            } catch (e: Exception) {
                errorMsg = "Lỗi không xác định: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Create order function
    fun createOrder() {
        scope.launch {
            isProcessingPayment = true
            errorMsg = null

            val token = tokenMgr.getToken().orEmpty()
            if (token.isBlank()) {
                errorMsg = "Token không hợp lệ"
                isProcessingPayment = false
                return@launch
            }

            try {
                val resp = ApiClient.paymentService.createOrder(
                    token = "Bearer $token",
                    cartId = cartId,
                    addressId = selectedAddress.toLong(),
                    paymentMethod = if (selectedPayment.startsWith("Thanh toán khi nhận hàng")) "COD" else "VNPAY"
                )

                Log.d("CheckoutScreen", "API Response: code=${resp.code}, message=${resp.message}")

                if (resp.code == 200) {
                    showSuccessDialog = true
                } else {
                    errorMsg = resp.message ?: "Lỗi tạo đơn hàng"
                }
            } catch (e: HttpException) {
                errorMsg = "Lỗi server: ${e.code()}"
            } catch (e: IOException) {
                errorMsg = "Không thể kết nối server"
            } catch (e: Exception) {
                errorMsg = e.localizedMessage ?: "Lỗi không xác định"
            } finally {
                isProcessingPayment = false
            }
        }
    }

    // Initiate VNPay payment
    fun initiateVNPayPayment() {
        scope.launch {
            isProcessingPayment = true
            errorMsg = null

            val token = tokenMgr.getToken().orEmpty()
            if (token.isBlank()) {
                errorMsg = "Token không hợp lệ"
                isProcessingPayment = false
                return@launch
            }

            try {
                val resp = ApiClient.paymentService.createOrder(
                    token = "Bearer $token",
                    cartId = cartId,
                    addressId = selectedAddress.toLong(),
                    paymentMethod = "VNPAY"
                )

                Log.d("CheckoutScreen", "API Response: code=${resp.code}, message=${resp.message}")

                if (resp.code == 200 && !resp.redirectUrl.isNullOrEmpty()) {
                    paymentUrl = resp.redirectUrl
                    showPaymentDialog = true
                } else {
                    errorMsg = resp.message ?: "Lỗi khởi tạo thanh toán VNPay"
                }
            } catch (e: HttpException) {
                errorMsg = "Lỗi server: ${e.code()}"
            } catch (e: IOException) {
                errorMsg = "Không thể kết nối server"
            } catch (e: Exception) {
                errorMsg = e.localizedMessage ?: "Lỗi không xác định"
            } finally {
                isProcessingPayment = false
            }
        }
    }

    // Load data on first composition
    LaunchedEffect(Unit) { loadAll() }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingScreen()
            errorMsg != null -> ErrorScreen(
                message = errorMsg!!,
                onRetry = { loadAll() }
            )
            else -> CheckoutContent(
                cartItems = cartItems,
                addresses = addresses,
                total = total,
                selectedAddress = selectedAddress,
                onSelectAddress = { selectedAddress = it },
                onAddAddress = { showAddDialog = true },
                onEditAddress = { editAddress = it; showEditDialog = true },
                onDeleteAddress = { id ->
                    scope.launch {
                        try {
                            val auth = "Bearer ${tokenMgr.getToken().orEmpty()}"
                            ApiClient.addressService.deleteAddress(auth, id)
                            loadAll()
                        } catch (ex: Exception) {
                            errorMsg = "Xóa địa chỉ thất bại: ${ex.message}"
                        }
                    }
                },
                selectedPayment = selectedPayment,
                onSelectPayment = { selectedPayment = it },
                isProcessing = isProcessingPayment,
                error = errorMsg,
                onPlaceOrder = {
                    if (selectedAddress == 0) {
                        errorMsg = "Vui lòng chọn địa chỉ giao hàng"
                        return@CheckoutContent
                    }
                    if (selectedPayment.startsWith("Thanh toán khi nhận hàng")) {
                        createOrder()
                    } else {
                        initiateVNPayPayment()
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Add Address Dialog
        if (showAddDialog) {
            AddAddressDialog(
                onDismiss = { showAddDialog = false },
                onAddAddress = { form ->
                    scope.launch {
                        try {
                            val auth = "Bearer ${tokenMgr.getToken().orEmpty()}"
                            ApiClient.addressService.addAddress(auth, form.toDto())
                            showAddDialog = false
                            loadAll()
                        } catch (ex: Exception) {
                            errorMsg = "Thêm địa chỉ thất bại: ${ex.message}"
                        }
                    }
                }
            )
        }

        // Edit Address Dialog
        if (showEditDialog && editAddress != null) {
            AddAddressDialog(
                initialAddress = editAddress,
                onDismiss = {
                    showEditDialog = false
                    editAddress = null
                },
                onAddAddress = { form ->
                    scope.launch {
                        try {
                            val auth = "Bearer ${tokenMgr.getToken().orEmpty()}"
                            ApiClient.addressService.updateAddress(auth, form.id, form.toDto())
                            showEditDialog = false
                            editAddress = null
                            loadAll()
                        } catch (ex: Exception) {
                            errorMsg = "Cập nhật địa chỉ thất bại: ${ex.message}"
                        }
                    }
                }
            )
        }

        // Success Dialog
        if (showSuccessDialog) {
            OrderSuccessDialog {
                showSuccessDialog = false
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }

        // Payment Dialog (Alternative to WebView)
        if (showPaymentDialog && paymentUrl != null) {
            VNPayPaymentDialog(
                url = paymentUrl!!,
                onDismiss = {
                    showPaymentDialog = false
                    paymentUrl = null
                },
                onPaymentResult = { success ->
                    showPaymentDialog = false
                    paymentUrl = null
                    if (success) {
                        createOrder() // Only create order after successful payment
                    } else {
                        errorMsg = "Thanh toán thất bại. Vui lòng thử lại."
                    }
                }
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đang tải...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Thử lại")
        }
    }
}

@Composable
fun VNPayPaymentDialog(
    url: String,
    onDismiss: () -> Unit,
    onPaymentResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var isProcessing by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thanh toán VNPay",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { if (!isProcessing) onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showInstructions) {
                    Text(
                        text = "Bạn sẽ được chuyển đến trang thanh toán VNPay",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hủy")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                isProcessing = true
                                showInstructions = false
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    // In a real app, payment status should be checked via API
                                    // For now, we rely on user confirmation
                                } catch (e: Exception) {
                                    Log.e("VNPayDialog", "Error opening browser: ${e.message}")
                                    onPaymentResult(false)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Thanh toán")
                        }
                    }
                } else {
                    Text(
                        text = "Đang xử lý thanh toán...",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Vui lòng hoàn tất thanh toán trong trình duyệt",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { onPaymentResult(false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hủy")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { onPaymentResult(true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hoàn tất")
                        }
                    }
                }
            }
        }
    }
}

// Extension function to convert AddressForm to AddressDto
private fun AddressForm.toDto() = AddressDto(
    id = if (id == 0) null else id,
    name = name,
    phone = phone,
    city = city,
    district = district,
    ward = ward,
    street = street,
    addressLine = addressLine,
    isDefault = isDefault
)

// Additional utility functions for better error handling
private fun handleApiError(error: Throwable): String {
    return when (error) {
        is HttpException -> {
            when (error.code()) {
                401 -> "Phiên đăng nhập đã hết hạn"
                403 -> "Không có quyền truy cập"
                404 -> "Không tìm thấy dữ liệu"
                500 -> "Lỗi server nội bộ"
                else -> "Lỗi server: ${error.code()}"
            }
        }
        is IOException -> "Không thể kết nối server. Vui lòng kiểm tra kết nối mạng."
        else -> error.localizedMessage ?: "Lỗi không xác định"
    }
}