@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.prm391_project.screen.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var currentLocation by remember { mutableStateOf<Point?>(null) }
    val mapView = remember { mutableStateOf<MapView?>(null) }


    // Gọi lấy vị trí nếu có quyền
    LaunchedEffect(permissionState.status) {
        if (permissionState.status is PermissionStatus.Granted) {
            try {
                val location = getCurrentLocation(context)
                location?.let {
                    currentLocation = Point.fromLngLat(it.longitude, it.latitude)
                    Log.d("Mapbox", "Current location: $currentLocation")
                }
            } catch (e: Exception) {
                Log.e("Mapbox", "Lỗi lấy vị trí mới: ${e.message}")
            }
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    Column(Modifier.fillMaxSize()) {
        when (permissionState.status) {
            is PermissionStatus.Denied -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Ứng dụng cần quyền vị trí để hiển thị bản đồ")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Cấp quyền")
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
                            val landmark = Point.fromLngLat(106.8106, 10.8411)
                            mv.getMapboxMap().setCamera(
                                CameraOptions.Builder()
                                    .center(landmark)
                                    .zoom(14.0)
                                    .build()
                            )

                            val annotationManager = mv.annotations.createPointAnnotationManager()

                            val landmarkDrawable = ContextCompat.getDrawable(ctx, R.drawable.marker_svgrepo_com)
                            val landmarkBitmap = landmarkDrawable?.let {
                                val width = it.intrinsicWidth
                                val height = it.intrinsicHeight
                                it.setBounds(0, 0, width, height)
                                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bmp ->
                                    val canvas = android.graphics.Canvas(bmp)
                                    it.draw(canvas)
                                }
                            }

                            landmarkBitmap?.let {
                                annotationManager.create(
                                    PointAnnotationOptions()
                                        .withPoint(landmark)
                                        .withIconImage(it)
                                )
                            }
                        }

                        mv
                    }
                )
            }
        }
    }

    // Vẽ marker khi có vị trí
    LaunchedEffect(currentLocation) {
        val mv = mapView.value ?: return@LaunchedEffect
        val point = currentLocation ?: return@LaunchedEffect

        mv.getMapboxMap().getStyle()?.let {
            val annotationManager = mv.annotations.createPointAnnotationManager()
            val iconDrawable = ContextCompat.getDrawable(context, R.drawable.marker_svgrepo_com)
            val iconBitmap = iconDrawable?.let {
                val width = it.intrinsicWidth
                val height = it.intrinsicHeight
                it.setBounds(0, 0, width, height)
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bmp ->
                    val canvas = android.graphics.Canvas(bmp)
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
    }
}
