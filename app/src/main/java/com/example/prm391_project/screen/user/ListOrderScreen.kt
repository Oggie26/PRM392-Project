package com.example.prm391_project.screen.user

import TokenManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.prm391_project.R
import com.example.prm391_project.Screen
import com.example.prm391_project.common.getStatusColorAndIcon
import com.example.prm391_project.config.RetrofitClient
import com.example.prm391_project.response.OrderResult
import kotlinx.coroutines.delay
import java.net.SocketTimeoutException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOrderScreen(navController: NavHostController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var orders by remember { mutableStateOf<List<OrderResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }

    // Function to fetch orders with retry logic
    suspend fun fetchOrders() {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                errorMessage = "Vui lòng đăng nhập để xem lịch sử đơn hàng"
                isLoading = false
                return
            }

            Log.d("ListOrderScreen", "Fetching orders with token: $token")

            val response = RetrofitClient.orderService.getOrderHistory(
                token = "Bearer $token",
                page = 1,
                limit = 50
            )

            Log.d("ListOrderScreen", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val orderHistory = response.body()
                orders = orderHistory?.result ?: emptyList()
                errorMessage = if (orders.isEmpty()) {
                    "Bạn chưa có đơn hàng nào"
                } else {
                    null
                }
                Log.d("ListOrderScreen", "Successfully loaded ${orders.size} orders")
            } else {
                errorMessage = when (response.code()) {
                    401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại"
                    403 -> "Không có quyền truy cập"
                    404 -> "Không tìm thấy dữ liệu"
                    500 -> "Lỗi máy chủ. Vui lòng thử lại sau"
                    else -> "Không thể tải danh sách đơn hàng (${response.code()})"
                }
                Log.e("ListOrderScreen", "API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: SocketTimeoutException) {
            errorMessage = "Kết nối quá chậm. Vui lòng kiểm tra mạng và thử lại"
            Log.e("ListOrderScreen", "Timeout Exception: ${e.message}")
        } catch (e: Exception) {
            errorMessage = "Lỗi kết nối: ${e.message}"
            Log.e("ListOrderScreen", "Exception: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Retry function
    fun retryFetch() {
        retryCount++
        isLoading = true
        errorMessage = null
    }

    // Launch effect with retry logic
    LaunchedEffect(retryCount) {
        if (retryCount > 0) {
            delay(1000) // Wait 1 second before retry
        }
        fetchOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử Đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Add refresh button
                    IconButton(
                        onClick = { retryFetch() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = if (isLoading) Color.Gray else Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 0.dp),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        when {
                            isLoading -> {
                                LoadingContent()
                            }

                            errorMessage != null -> {
                                ErrorContent(
                                    errorMessage = errorMessage!!,
                                    onRetry = { retryFetch() },
                                    showRetryButton = true
                                )
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(orders) { order ->
                                        ModernOrderCard(order = order) {
                                            navController.navigate(Screen.OrderDetail.createRoute(order.orderId))
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.trackloading)
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    showRetryButton: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Black.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                errorMessage,
                textAlign = TextAlign.Center,
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            if (showRetryButton) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thử lại")
                }
            }
        }
    }
}

@Composable
fun ModernOrderCard(order: OrderResult, onClick: () -> Unit) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
    val outputDateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val formattedDate = try {
        order.orderDate?.let { dateString ->
            val date = isoDateFormatter.parse(dateString)
            date?.let { outputDateFormatter.format(it) } ?: "Không xác định"
        } ?: "Không xác định"
    } catch (e: Exception) {
        "Không xác định"
    }
    val (statusColor, statusIcon) = getStatusColorAndIcon(order.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Đơn hàng #${order.orderId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 12.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }

                ModernStatusChip(
                    status = order.status ?: "Không xác định",
                    color = statusColor,
                    icon = statusIcon
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Order details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tổng tiền",
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatter.format(order.totalAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Sản phẩm",
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${order.orderResponseItemList.size} món",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Xem chi tiết",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ModernStatusChip(status: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}