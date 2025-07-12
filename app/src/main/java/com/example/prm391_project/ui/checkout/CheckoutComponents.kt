package com.example.prm391_project.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prm391_project.screen.user.CartItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// --- Progress Steps (Material3) ---
@Composable
fun CheckoutStep(
    icon: ImageVector,
    label: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(40.dp)
                .background(
                    color = when {
                        isCompleted -> Color.Black
                        isActive    -> Color.Black
                        else        -> Color.Gray.copy(alpha = 0.3f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else icon,
                contentDescription = null,
                tint = Color.White
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive || isCompleted) Color.Black else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun StepConnector(isCompleted: Boolean) {
    Box(
        Modifier
            .width(40.dp)
            .height(2.dp)
            .background(
                color = if (isCompleted) Color.Black else Color.Gray.copy(alpha = 0.3f)
            )
    )
}

// --- Section Card Wrapper (Material3) ---
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// --- Address Card ---
@Composable
fun AddressCard(
    address: AddressForm,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Black else Color(0xFFF8F9FA)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.Black
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    address.name,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.Black
                )
                if (address.isDefault) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Mặc định",
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White.copy(0.8f) else Color.Gray
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${address.street}, ${address.ward}, ${address.district}, ${address.city}",
                fontSize = 14.sp,
                color = if (isSelected) Color.White.copy(0.9f) else Color.Gray
            )
            Text(
                address.phone,
                fontSize = 12.sp,
                color = if (isSelected) Color.White.copy(0.8f) else Color.Gray
            )
        }
    }
}

// --- Address Form Dialog ---
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddAddressDialog(
//    onDismiss: () -> Unit,
//    onAddAddress: (AddressForm) -> Unit,
//    initialAddress: AddressForm? = null
//) {
//    // Khởi tạo state với giá trị từ initialAddress hoặc rỗng
//    var name by remember { mutableStateOf(initialAddress?.name ?: "") }
//    var street by remember { mutableStateOf(initialAddress?.street ?: "") }
//    var ward by remember { mutableStateOf(initialAddress?.ward ?: "") }
//    var district by remember { mutableStateOf(initialAddress?.district ?: "") }
//    var city by remember { mutableStateOf(initialAddress?.city ?: "") }
//    var phone by remember { mutableStateOf(initialAddress?.phone ?: "") }
//    var addressLine by remember { mutableStateOf(initialAddress?.addressLine ?: "") }
//    var isDefault by remember { mutableStateOf(initialAddress?.isDefault ?: false) }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(text = if (initialAddress != null) "Chỉnh sửa địa chỉ" else "Thêm địa chỉ mới")
//        },
//        text = {
//            Column(modifier = Modifier.padding(vertical = 8.dp)) {
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Tên người nhận") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                )
//                OutlinedTextField(
//                    value = street,
//                    onValueChange = { street = it },
//                    label = { Text("Đường") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                )
//                OutlinedTextField(
//                    value = ward,
//                    onValueChange = { ward = it },
//                    label = { Text("Phường") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                )
//                OutlinedTextField(
//                    value = district,
//                    onValueChange = { district = it },
//                    label = { Text("Quận/Huyện") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                )
//                OutlinedTextField(
//                    value = city,
//                    onValueChange = { city = it },
//                    label = { Text("Thành phố") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                )
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = { phone = it },
//                    label = { Text("Số điện thoại") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                )
//                OutlinedTextField(
//                    value = addressLine,
//                    onValueChange = { addressLine = it },
//                    label = { Text("Địa chỉ chi tiết") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                )
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Checkbox(
//                        checked = isDefault,
//                        onCheckedChange = { isDefault = it }
//                    )
//                    Text("Đặt làm địa chỉ mặc định")
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    val newForm = AddressForm(
//                        id = initialAddress?.id ?: 0,
//                        name = name,
//                        street = street,
//                        ward = ward,
//                        district = district,
//                        city = city,
//                        phone = phone,
//                        addressLine = addressLine,
//                        isDefault = isDefault
//                    )
//                    onAddAddress(newForm)
//                }
//            ) {
//                Text(if (initialAddress != null) "Cập nhật" else "Thêm")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Huỷ")
//            }
//        }
//    )
//}

// --- Payment Method Card (Material3) ---
@Composable
fun PaymentMethodCard(
    method: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Black else Color(0xFFF8F9FA)
        )
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else Color.Black)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(method, color = if (isSelected) Color.White else Color.Black)
                Text(subtitle, fontSize = 12.sp, color = if (isSelected) Color.White.copy(0.8f) else Color.Gray)
            }
            RadioButton(selected = isSelected, onClick = onSelect)
        }
    }
}

// --- Order Item Row (Material3) ---
@Composable
fun OrderItem(name: String, quantity: Int, price: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("$name x$quantity")
        Text("đ$price")
    }
}

// --- Summary Row (Material3) ---
@Composable
fun SummaryRow(label: String, value: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text("đ$value")
    }
}

// --- Voucher Card in Dialog (Material3) ---
@Composable
fun VoucherCard(v: Voucher, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${v.discountPercent}%", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(v.title)
                Text(v.description, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}

// --- Wallet Option Card for Digital Wallet Dialog (Material3) ---
@Composable
fun WalletOptionCard(
    walletName: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = walletName
        )
        Spacer(Modifier.width(8.dp))
        Text(walletName, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
    }
}