package com.example.prm391_project.ui.checkout

import TokenManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

    // ViewModel
    val viewModel = remember {
        CheckoutViewModel(
            paymentService = ApiClient.paymentService,
            tokenManager   = tokenMgr
        )
    }
    val paymentState by viewModel.uiState.collectAsState()

    // State cho cart + address + payment
    var cartItems       by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var total           by remember { mutableStateOf(0) }
    var cartId          by remember { mutableStateOf(0L) }
    var addresses       by remember { mutableStateOf<List<AddressForm>>(emptyList()) }
    var selectedAddress by remember { mutableStateOf(0) }
    var isLoading       by remember { mutableStateOf(true) }
    var errorMsg        by remember { mutableStateOf<String?>(null) }
    var selectedPayment by remember { mutableStateOf("Trả tiền mặt") }  // ← thêm state

    // Controls
    var showAddDialog     by remember { mutableStateOf(false) }
    var showEditDialog    by remember { mutableStateOf(false) }
    var editAddress       by remember { mutableStateOf<AddressForm?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Load dữ liệu
    fun loadAll() {
        scope.launch {
            isLoading = true; errorMsg = null
            val rawToken = tokenMgr.getToken().orEmpty()
            if (rawToken.isEmpty()) {
                errorMsg = "Bạn chưa đăng nhập."; isLoading = false; return@launch
            }
            val auth = "Bearer $rawToken"
            try {
                ApiClient.cartService.getCart(auth).let { resp: IResponse<CartResult> ->
                    if (resp.code == 200 && resp.data != null) {
                        cartId    = resp.data.cartId?.toLong() ?: 0L
                        cartItems = resp.data.items.orEmpty()
                            .mapNotNull { (it as CartItemDto).toCartItem() }
                        total    = resp.data.totalPrice?.toInt() ?: 0
                    } else errorMsg = resp.message
                }
                ApiClient.addressService.getAddresses(auth).let { resp: IResponse<List<AddressDto>> ->
                    if (resp.code == 200 && resp.data != null) {
                        addresses = resp.data.map(AddressDto::toForm)
                        resp.data.firstOrNull { it.isDefault == true }?.id?.let {
                            selectedAddress = it
                        }
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

    // Khi paymentState thay đổi
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is CheckoutViewModel.UiState.Success -> {
                errorMsg           = null
                showSuccessDialog  = true
            }
            is CheckoutViewModel.UiState.Error -> {
                errorMsg = (paymentState as CheckoutViewModel.UiState.Error).message
            }
            else -> {}
        }
    }

    // UI
    Box(Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            errorMsg != null ->
                Text(
                    text = errorMsg!!,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            else -> {
                CheckoutContent(
                    cartItems       = cartItems,
                    addresses       = addresses,
                    total           = total,
                    selectedAddress = selectedAddress,
                    onSelectAddress = { selectedAddress = it },
                    onAddAddress    = { showAddDialog = true },
                    onEditAddress   = { editAddress = it; showEditDialog = true },
                    onDeleteAddress = { addrId ->
                        scope.launch {
                            val rawToken = tokenMgr.getToken().orEmpty()
                            try {
                                ApiClient.addressService.deleteAddress("Bearer $rawToken", addrId)
                                loadAll()
                            } catch (e: Exception) {
                                errorMsg = "Lỗi khi xóa địa chỉ: ${e.message}"
                            }
                        }
                    },
                    selectedPayment = selectedPayment,              // ← truyền state
                    onSelectPayment = { selectedPayment = it },     // ← cập nhật state
                    isProcessing    = paymentState is CheckoutViewModel.UiState.Loading,
                    error           = null,
                    onPlaceOrder    = {
                        // map "Trả tiền mặt" → "COD"
                        val method = if (selectedPayment == "Trả tiền mặt") "COD" else selectedPayment
                        viewModel.pay(cartId, selectedAddress.toLong(), method)
                    },
                    onNavigateBack  = { navController.popBackStack() }
                )
            }
        }

        // Dialogs thêm/sửa
        if (showAddDialog) {
            AddAddressDialog(
                onDismiss    = { showAddDialog = false },
                onAddAddress = { form ->
                    scope.launch {
                        val rawToken = tokenMgr.getToken().orEmpty()
                        try {
                            ApiClient.addressService.addAddress("Bearer $rawToken", form.toDto())
                            showAddDialog = false
                            loadAll()
                        } catch (e: Exception) {
                            errorMsg = "Lỗi khi thêm địa chỉ: ${e.message}"
                        }
                    }
                }
            )
        }
        if (showEditDialog && editAddress != null) {
            AddAddressDialog(
                initialAddress = editAddress,
                onDismiss      = { showEditDialog = false; editAddress = null },
                onAddAddress   = { form ->
                    scope.launch {
                        val rawToken = tokenMgr.getToken().orEmpty()
                        try {
                            ApiClient.addressService.updateAddress("Bearer $rawToken", form.id, form.toDto())
                            showEditDialog = false; editAddress = null
                            loadAll()
                        } catch (e: Exception) {
                            errorMsg = "Lỗi khi cập nhật địa chỉ: ${e.message}"
                        }
                    }
                }
            )
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        OrderSuccessDialog {
            showSuccessDialog = false
            navController.navigate("home") { popUpTo("checkout") { inclusive = true } }
        }
    }
}

// Extension: AddressForm → AddressDto
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
