@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.prm391_project.screen.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.prm391_project.R
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

// Data class cho thông tin shop
data class ShopInfo(
    val name: String,
    val address: String,
    val phone: String,
    val rating: Float,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val isOpen: Boolean
)

// Hàm mở Google Maps
fun openGoogleMaps(context: Context, shopInfo: ShopInfo) {
    try {
        // Tạo URI cho Google Maps với tọa độ và tên shop
        val gmmIntentUri = Uri.parse("geo:${shopInfo.latitude},${shopInfo.longitude}?q=${shopInfo.latitude},${shopInfo.longitude}(${Uri.encode(shopInfo.name)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Kiểm tra xem Google Maps có được cài đặt không
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Nếu không có Google Maps, mở trong trình duyệt
            val browserIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${shopInfo.latitude},${shopInfo.longitude}"))
            context.startActivity(browserIntent)
        }
    } catch (e: Exception) {
        Log.e("MapScreen", "Error opening Google Maps: ${e.message}")
        Toast.makeText(context, "Không thể mở Google Maps", Toast.LENGTH_SHORT).show()
    }
}

// Hàm lấy vị trí hiện tại
suspend fun getCurrentLocation(context: Context): Location? {
    return suspendCancellableCoroutine { cont ->
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                cont.resume(result.lastLocation)
                fusedClient.removeLocationUpdates(this)
            }
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            cont.invokeOnCancellation {
                fusedClient.removeLocationUpdates(callback)
            }
        } else {
            cont.resume(null)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navController: NavController) {
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var currentLocation by remember { mutableStateOf<Point?>(null) }
    var selectedShop by remember { mutableStateOf<ShopInfo?>(null) }
    var showShopInfo by remember { mutableStateOf(false) }
    val mapView = remember { mutableStateOf<MapView?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    // Dữ liệu mẫu cho các shop
    val shopList = remember {
        listOf(
            ShopInfo(
                name = "Icot Shop",
                address = "7 Đ. D1, Long Thạnh Mỹ, Thủ Đức, Hồ Chí Minh",
                phone = "0901234567",
                rating = 4.5f,
                description = "Shop chuyên cung cấp, đặt mẫu các trang phục cho khách hàng",
                latitude = 10.8411,
                longitude = 106.8106,
                isOpen = true
            ),
            ShopInfo(
                name = "Icot Store",
                address = "123 Nguyễn Văn Linh, Quận 7, Hồ Chí Minh",
                phone = "0907654321",
                rating = 4.2f,
                description = "Cửa hàng thời trang cao cấp",
                latitude = 10.7411,
                longitude = 106.7106,
                isOpen = true
            )
        )
    }

    // Hàm di chuyển camera đến vị trí hiện tại
    fun moveToCurrentLocation() {
        isLoadingLocation = true
        currentLocation?.let { location ->
            mapView.value?.getMapboxMap()?.setCamera(
                CameraOptions.Builder()
                    .center(location)
                    .zoom(16.0)
                    .build()
            )
        }
        isLoadingLocation = false
    }

    // Gọi lấy vị trí nếu có quyền
    LaunchedEffect(permissionState.status) {
        if (permissionState.status is PermissionStatus.Granted) {
            try {
                val location = getCurrentLocation(context)
                location?.let {
                    currentLocation = Point.fromLngLat(it.longitude, it.latitude)
                    Log.d("MapScreen", "Current location: $currentLocation")
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Lỗi lấy vị trí: ${e.message}")
            }
        }
    }

    // Tự động yêu cầu quyền khi khởi tạo
    LaunchedEffect(Unit) {
        if (permissionState.status is PermissionStatus.Denied) {
            permissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            when (permissionState.status) {
                is PermissionStatus.Denied -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Location",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ứng dụng cần quyền vị trí để hiển thị bản đồ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { permissionState.launchPermissionRequest() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cấp quyền vị trí")
                        }
                    }
                }

                is PermissionStatus.Granted -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val mv = MapView(ctx)
                            mapView.value = mv

                            val observer = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
                                when (event) {
                                    Lifecycle.Event.ON_START -> mv.onStart()
                                    Lifecycle.Event.ON_STOP -> mv.onStop()
                                    Lifecycle.Event.ON_DESTROY -> mv.onDestroy()
                                    else -> {}
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)

                            mv.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                                try {
                                    // Thiết lập camera ban đầu
                                    val initialLocation = Point.fromLngLat(106.8106, 10.8411)
                                    mv.getMapboxMap().setCamera(
                                        CameraOptions.Builder()
                                            .center(initialLocation)
                                            .zoom(14.0)
                                            .build()
                                    )

                                    val annotationManager = mv.annotations.createPointAnnotationManager()

                                    // Thêm marker cho các shop
                                    shopList.forEach { shop ->
                                        val shopLocation = Point.fromLngLat(shop.longitude, shop.latitude)
                                        val shopDrawable = ContextCompat.getDrawable(ctx, R.drawable.marker_svgrepo_com)
                                        val shopBitmap = shopDrawable?.let {
                                            val width = 80 // Kích thước marker cố định
                                            val height = 80
                                            it.setBounds(0, 0, width, height)
                                            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bmp ->
                                                val canvas = android.graphics.Canvas(bmp)
                                                it.draw(canvas)
                                            }
                                        }

                                        shopBitmap?.let {
                                            annotationManager.create(
                                                PointAnnotationOptions()
                                                    .withPoint(shopLocation)
                                                    .withIconImage(it)
                                            )
                                        }
                                    }

                                    // Thêm click listener cho annotations
                                    annotationManager.addClickListener { annotation ->
                                        val clickedPoint = annotation.point
                                        val clickedShop = shopList.find { shop ->
                                            val latDiff = Math.abs(shop.latitude - clickedPoint.latitude())
                                            val lonDiff = Math.abs(shop.longitude - clickedPoint.longitude())
                                            latDiff < 0.001 && lonDiff < 0.001
                                        }
                                        clickedShop?.let {
                                            selectedShop = it
                                            showShopInfo = true
                                        }
                                        true
                                    }
                                } catch (e: Exception) {
                                    Log.e("MapScreen", "Error setting up map: ${e.message}")
                                }
                            }

                            mv
                        }
                    )
                }
            }
        }

        // Nút "My Location" - Floating Action Button
        if (permissionState.status is PermissionStatus.Granted) {
            FloatingActionButton(
                onClick = { moveToCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                if (isLoadingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location"
                    )
                }
            }
        }

        // Bottom Sheet hiển thị thông tin shop
        if (showShopInfo && selectedShop != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedShop!!.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        // Trạng thái mở/đóng
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedShop!!.isOpen)
                                    Color(0xFF4CAF50) else Color(0xFFF44336)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (selectedShop!!.isOpen) "Đang mở" else "Đã đóng",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Đánh giá sao
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedShop!!.rating.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Địa chỉ
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Address",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedShop!!.address,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Số điện thoại
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedShop!!.phone,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mô tả
                    Text(
                        text = selectedShop!!.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Các nút hành động
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                showShopInfo = false
                                selectedShop = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Đóng")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                selectedShop?.let { shop ->
                                    openGoogleMaps(context, shop)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Xem chi tiết")
                        }
                    }
                }
            }
        }
    }

    // Vẽ marker cho vị trí hiện tại
    LaunchedEffect(currentLocation) {
        val mv = mapView.value ?: return@LaunchedEffect
        val point = currentLocation ?: return@LaunchedEffect

        try {
            mv.getMapboxMap().getStyle()?.let {
                val annotationManager = mv.annotations.createPointAnnotationManager()
                val iconDrawable = ContextCompat.getDrawable(context, R.drawable.marker_svgrepo_com)
                val iconBitmap = iconDrawable?.let {
                    val width = 60 // Kích thước khác cho marker vị trí hiện tại
                    val height = 60
                    it.setBounds(0, 0, width, height)
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bmp ->
                        val canvas = Canvas(bmp)
                        it.draw(canvas)
                    }
                }

                iconBitmap?.let {
                    annotationManager.create(
                        PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage(it)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Error adding current location marker: ${e.message}")
        }
    }
}