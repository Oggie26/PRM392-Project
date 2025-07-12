@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.prm391_project.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



/**
 * Dialog thêm địa chỉ mới
 */
@Composable
fun AddAddressDialog(
    initialAddress: AddressForm? = null,
    onDismiss: () -> Unit,
    onAddAddress: (AddressForm) -> Unit
) {
    var name by remember(initialAddress) { mutableStateOf(initialAddress?.name ?: "") }
    var phone by remember(initialAddress) { mutableStateOf(initialAddress?.phone ?: "") }
    var street by remember(initialAddress) { mutableStateOf(initialAddress?.street ?: "") }
    var district by remember(initialAddress) { mutableStateOf(initialAddress?.district ?: "") }
    var city by remember(initialAddress) { mutableStateOf(initialAddress?.city ?: "") }
    var ward by remember(initialAddress) { mutableStateOf(initialAddress?.ward ?: "") }
    var addressLine by remember(initialAddress) { mutableStateOf(initialAddress?.addressLine ?: "") }
    var isDefault by remember(initialAddress) { mutableStateOf(initialAddress?.isDefault ?: false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add New Address", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Street Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 2
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ward,
                    onValueChange = { ward = it },
                    label = { Text("Ward") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = addressLine,
                    onValueChange = { addressLine = it },
                    label = { Text("Address Line") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Set as default address")
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val newForm = AddressForm(
                            id          = initialAddress?.id ?: 0,    // ← dùng id gốc nếu có
                            name        = name,
                            phone       = phone,
                            street      = street,
                            district    = district,
                            city        = city,
                            ward        = ward,
                            addressLine = addressLine,
                            isDefault   = isDefault
                        )
                        onAddAddress(newForm)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Address")
                }
            }
        }
    }
}

/**
 * Dialog chọn Voucher (hard-code)
 */
@Composable
fun VoucherDialog(
    onDismiss: () -> Unit,
    onSelectVoucher: (Voucher) -> Unit
) {
    val vouchers = listOf(
        Voucher("SAVE10","Save 10%",10,"Valid until Dec 31"),
        Voucher("SAVE15","Save 15%",15,"Valid until Dec 31"),
        Voucher("SAVE20","Save 20%",20,"Valid until Dec 31"),
        Voucher("NEWUSER","New User 25% Off",25,"Valid until Dec 31")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Voucher", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(Modifier.height(12.dp))
                vouchers.forEach { v ->
                    VoucherCard(v) { onSelectVoucher(v) }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

/**
 * Dialog thanh toán ví điện tử + QRCode
 */
@Composable
fun DigitalWalletDialog(
    amount: Int,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    var selectedWallet by remember { mutableStateOf("VNPay") }
    var showQR by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payment", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (!showQR) {
                    Text("Select Wallet", fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    listOf("VNPay","Momo","ZaloPay").forEach { w ->
                        WalletOptionCard(w, selectedWallet == w) { selectedWallet = w }
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Amount: $$amount", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showQR = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Generate QR Code")
                    }
                } else {
                    QRCodeSection(
                        wallet = selectedWallet,
                        amount = amount,
                        onPaymentSuccess = onPaymentSuccess
                    )
                }
            }
        }
    }
}

/**
 * QRCode + countdown + simulate payment
 */
@Composable
fun QRCodeSection(
    wallet: String,
    amount: Int,
    onPaymentSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var timeLeft by remember { mutableStateOf(300) }
    var processing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scan QR Code to Pay", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .size(200.dp)
                .background(Color.LightGray, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.QrCode, contentDescription = "QR", modifier = Modifier.size(80.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Time left: ${timeLeft/60}:${(timeLeft%60).toString().padStart(2,'0')}")
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                processing = true
                scope.launch {
                    delay(2000)
                    onPaymentSuccess()
                }
            },
            enabled = !processing,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (processing) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Simulate Payment Success")
        }
    }
}

/**
 * Dialog đặt hàng thành công
 */
@Composable
fun OrderSuccessDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color.Green, modifier = Modifier.size(80.dp))
                Spacer(Modifier.height(16.dp))
                Text("Order Placed Successfully!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Thank you for your purchase. Your order will arrive soon.", textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White)
                ) {
                    Text("Back to Shopping")
                }
            }
        }
    }
}


