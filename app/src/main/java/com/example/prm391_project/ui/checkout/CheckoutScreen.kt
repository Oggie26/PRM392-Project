package com.example.prm391_project.ui.checkout

// Các import giữ nguyên như mã trước đó
import TokenManager
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.viewinterop.AndroidView
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
    var showWalletDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editAddress by remember { mutableStateOf<AddressForm?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showWebView by remember { mutableStateOf(false) }
    var webViewUrl by remember { mutableStateOf<String?>(null) }

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
                        cartId = resp.data.cartId?.toLong() ?: 0L
                        cartItems = resp.data.items.orEmpty().mapNotNull { (it as CartItemDto).toCartItem() }
                        total = resp.data.totalPrice?.toInt() ?: 0
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

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            errorMsg != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { loadAll() },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Thử lại")
                }
            }
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
                isProcessing = isLoading,
                error = errorMsg,
                onPlaceOrder = {
                    scope.launch {
                        isLoading = true
                        errorMsg = null
                        val token = tokenMgr.getToken().orEmpty()
                        if (token.isBlank()) {
                            errorMsg = "Token không hợp lệ"
                            isLoading = false
                            return@launch
                        }
                        val method = if (selectedPayment.startsWith("Thanh toán khi nhận hàng")) "COD" else "VNPAY"
                        try {
                            val resp = ApiClient.paymentService.createOrder(
                                token = "Bearer $token",
                                cartId = cartId,
                                addressId = selectedAddress.toLong(),
                                paymentMethod = method
                            )
                            Log.d("CheckoutScreen", "Phản hồi API: code=${resp.code}, message=${resp.message}, redirectUrl=${resp.redirectUrl}")
                            if (resp.code == 200 && !resp.redirectUrl.isNullOrEmpty()) {
                                if (method == "VNPAY" && (resp.redirectUrl!!.startsWith("https://sandbox.vnpayment.vn") || resp.redirectUrl!!.startsWith("https://vnpayment.vn"))) {
                                    Log.d("CheckoutScreen", "Mở WebView với URL: ${resp.redirectUrl}")
                                    webViewUrl = resp.redirectUrl
                                    showWebView = true
                                } else {
                                    Log.d("CheckoutScreen", "Hiển thị dialog thành công cho COD")
                                    showSuccessDialog = true
                                }
                            } else {
                                errorMsg = resp.message ?: "Lỗi thanh toán: Không có URL chuyển hướng"
                                Log.e("CheckoutScreen", "Lỗi thanh toán: $errorMsg")
                            }
                        } catch (e: HttpException) {
                            errorMsg = "Lỗi server: ${e.code()}"
                            Log.e("CheckoutScreen", "Lỗi HTTP: ${e.message()}")
                        } catch (e: IOException) {
                            errorMsg = "Không thể kết nối server"
                            Log.e("CheckoutScreen", "Lỗi mạng: ${e.message}")
                        } catch (e: Exception) {
                            errorMsg = e.localizedMessage ?: "Lỗi không xác định"
                            Log.e("CheckoutScreen", "Lỗi khác: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        if (showAddDialog) AddAddressDialog(
            onDismiss = { showAddDialog = false },
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
            onDismiss = { showEditDialog = false; editAddress = null },
            onAddAddress = { form ->
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
                scope.launch {
                    isLoading = true
                    errorMsg = null
                    val token = tokenMgr.getToken().orEmpty()
                    if (token.isBlank()) {
                        errorMsg = "Token không hợp lệ"
                        isLoading = false
                        return@launch
                    }
                    try {
                        val resp = ApiClient.paymentService.createOrder(
                            token = "Bearer $token",
                            cartId = cartId,
                            addressId = selectedAddress.toLong(),
                            paymentMethod = "VNPAY"
                        )
                        Log.d("CheckoutScreen", "Phản hồi API: code=${resp.code}, message=${resp.message}, redirectUrl=${resp.redirectUrl}")
                        if (resp.code == 200 && !resp.redirectUrl.isNullOrEmpty()) {
                            if (resp.redirectUrl!!.startsWith("https://sandbox.vnpayment.vn") || resp.redirectUrl!!.startsWith("https://vnpayment.vn")) {
                                Log.d("CheckoutScreen", "Mở WebView với URL: ${resp.redirectUrl}")
                                webViewUrl = resp.redirectUrl
                                showWebView = true
                            } else {
                                errorMsg = "URL chuyển hướng không hợp lệ: ${resp.redirectUrl}"
                                Log.e("CheckoutScreen", "URL không hợp lệ: ${resp.redirectUrl}")
                            }
                        } else {
                            errorMsg = resp.message ?: "Lỗi thanh toán: Không có URL chuyển hướng"
                            Log.e("CheckoutScreen", "Lỗi thanh toán: $errorMsg")
                        }
                    } catch (e: HttpException) {
                        errorMsg = "Lỗi server: ${e.code()}"
                        Log.e("CheckoutScreen", "Lỗi HTTP: ${e.message()}")
                    } catch (e: IOException) {
                        errorMsg = "Không thể kết nối server"
                        Log.e("CheckoutScreen", "Lỗi mạng: ${e.message}")
                    } catch (e: Exception) {
                        errorMsg = e.localizedMessage ?: "Lỗi không xác định"
                        Log.e("CheckoutScreen", "Lỗi khác: ${e.message}")
                    } finally {
                        isLoading = false
                    }
                }
            }
        )

        if (showSuccessDialog) {
            OrderSuccessDialog {
                showSuccessDialog = false
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }

        if (showWebView && webViewUrl != null) {
            Log.d("CheckoutScreen", "Hiển thị WebViewDialog với URL: $webViewUrl")
            WebViewDialog(
                url = webViewUrl!!,
                onDismiss = {
                    showWebView = false
                    webViewUrl = null
                },
                onPaymentResult = { success ->
                    showWebView = false
                    webViewUrl = null
                    if (success) {
                        showSuccessDialog = true
                    } else {
                        errorMsg = "Thanh toán thất bại. Vui lòng thử lại."
                    }
                }
            )
        }
    }
}

// WebViewDialog và AddressForm.toDto giữ nguyên như mã trước đó
@Composable
fun WebViewDialog(
    url: String,
    onDismiss: () -> Unit,
    onPaymentResult: (Boolean) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thanh toán VNPay", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (!isLoading) onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.javaScriptCanOpenWindowsAutomatically = true
                                settings.setSupportMultipleWindows(true)
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                        isLoading = true
                                        error = null
                                        Log.d("WebViewDialog", "Bắt đầu tải: $url")
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        Log.d("WebViewDialog", "Hoàn tất tải: $url")
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        isLoading = false
                                    }
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        url: String
                                    ): Boolean {
                                        if (url.contains("vnp_TransactionStatus")) {
                                            val uri = Uri.parse(url)
                                            val transactionStatus = uri.getQueryParameter("vnp_TransactionStatus")
                                            Log.d("WebViewDialog", "Trạng thái giao dịch: $transactionStatus")
                                            when (transactionStatus) {
                                                "00" -> onPaymentResult(true)
                                                else -> onPaymentResult(false)
                                            }
                                            return true
                                        }
                                        if (url.startsWith("vnpay://")) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                                return true
                                            } catch (e: Exception) {
                                                error = "Không thể mở ứng dụng VNPay: ${e.message}"
                                                Log.e("WebViewDialog", "Lỗi deep link: ${e.message}")
                                            }
                                        }
                                        return false
                                    }
                                }
                                if (url.startsWith("https://sandbox.vnpayment.vn") || url.startsWith("https://vnpayment.vn")) {
                                    loadUrl(url)
                                } else {
                                    error = "URL không hợp lệ: $url"
                                    isLoading = false
                                    Log.e("WebViewDialog", "URL không hợp lệ: $url")
                                }
                            }
                        }
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Black
                        )
                    }
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

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