package com.example.prm391_project.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.prm391_project.screen.user.CartItem



@Composable
fun CheckoutContent(
    cartItems: List<CartItem>,
    addresses: List<AddressForm>,
    total: Int,
    selectedAddress: Int,
    onSelectAddress: (Int) -> Unit,
    onAddAddress: () -> Unit,
    onEditAddress: (AddressForm) -> Unit,
    onDeleteAddress: (Int) -> Unit,
    selectedPayment: String,
    onSelectPayment: (String) -> Unit,
    isProcessing: Boolean,
    error: String?,
    onPlaceOrder: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showVoucherDialog by remember { mutableStateOf(false) }
    var appliedVoucher   by remember { mutableStateOf<Voucher?>(null) }

    // Tính lại tổng nếu có voucher
    val finalTotal = appliedVoucher
        ?.let { (total * (1 - it.discountPercent / 100f)).toInt() }
        ?: total

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- Tiêu đề ---
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Color.Black,
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                    Text("Thanh toán", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Tài khoản", tint = Color.White)
                    }
                }
            }
        }

        // --- Thanh tiến trình ---
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckoutStep(Icons.Default.Payment, "Thanh toán", isCompleted = false, isActive = true)
                    StepConnector(isCompleted = false)
                    CheckoutStep(Icons.Default.CheckCircle, "Hoàn tất", isCompleted = false, isActive = false)
                }
            }
        }

        // --- Mã giảm giá & Khuyến mãi ---
        item {
            SectionCard("Mã giảm giá & Khuyến mãi", Icons.Default.LocalOffer) {
                if (appliedVoucher != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${appliedVoucher!!.code} — giảm ${appliedVoucher!!.discountPercent}%",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { appliedVoucher = null }) {
                            Text("Xóa")
                        }
                    }
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                            .clickable { showVoucherDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Chọn mã giảm giá", fontSize = 16.sp)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }

        // --- Thông tin giao hàng ---
        item {
            SectionCard("Thông tin giao hàng", Icons.Default.LocationOn) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    addresses.forEach { addr ->
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                AddressCard(addr, addr.id == selectedAddress) {
                                    onSelectAddress(addr.id)
                                }
                            }

                            // Nút sửa luôn hiển thị
                            IconButton(onClick = { onEditAddress(addr) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa địa chỉ")
                            }

                            // Chỉ hiển thị nút xóa khi không phải default
                            if (!addr.isDefault) {
                                IconButton(onClick = { onDeleteAddress(addr.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa địa chỉ")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onAddAddress,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Thêm địa chỉ mới")
                    }
                }
            }
        }

        // --- Phương thức thanh toán ---
        item {
            SectionCard("Phương thức thanh toán", Icons.Default.CreditCard) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
//                        "Thẻ tín dụng" to Icons.Default.CreditCard,
                        "Thanh toán khi nhận hàng" to Icons.Default.LocalShipping,
                        "Ví điện tử" to Icons.Default.AccountBalanceWallet
                    ).forEach { (method, icon) ->
                        PaymentMethodCard(
                            method = method,
                            subtitle = when (method) {
//                                "Thẻ tín dụng"              -> "•••• •••• •••• 3218"
                                "Thanh toán khi nhận hàng" -> "Trả khi nhận hàng"
                                else                          -> "VNPAY, Momo, ZaloPay"
                            },
                            icon = icon,
                            isSelected = method == selectedPayment,
                            onSelect = { onSelectPayment(method) }
                        )
                    }
                }
            }
        }

        // --- Tóm tắt đơn hàng ---
        item {
            SectionCard("Tóm tắt đơn hàng", Icons.Default.Receipt) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    cartItems.forEach { ci ->
                        OrderItem(ci.name, ci.quantity, (ci.price * ci.quantity).toInt())
                    }
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    SummaryRow("Tạm tính", total)
                    appliedVoucher?.let {
                        SummaryRow("Giảm giá", total - finalTotal)
                    }
                    SummaryRow("Thuế", 0)
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tổng cộng", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("₫${finalTotal}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Thông báo lỗi ---
        if (error != null) {
            item {
                Text(error, color = Color.Red, modifier = Modifier.padding(16.dp))
            }
        }

        // --- Nút đặt hàng ---
        item {
            Button(
                onClick = onPlaceOrder,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                if (isProcessing)
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                else
                    Text("Đặt hàng - ₫${finalTotal}", color = Color.White)
            }
        }
    }

    // --- Hộp thoại chọn mã giảm giá ---
    if (showVoucherDialog) {
        Dialog(onDismissRequest = { showVoucherDialog = false }) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chọn mã giảm giá", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showVoucherDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val vouchers = listOf(
                        Voucher("SAVE10","Giảm 10%",10,"HSD đến 31 Dec"),
                        Voucher("SAVE15","Giảm 15%",15,"HSD đến 31 Dec"),
                        Voucher("SAVE20","Giảm 20%",20,"HSD đến 31 Dec"),
                        Voucher("NEWUSER","Giảm 25% cho khách mới",25,"HSD đến 31 Dec")
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(vouchers) { v ->
                            VoucherCard(v) {
                                appliedVoucher = v
                                showVoucherDialog = false
                            }
                        }
                    }
                }
            }
        }
    }
}
