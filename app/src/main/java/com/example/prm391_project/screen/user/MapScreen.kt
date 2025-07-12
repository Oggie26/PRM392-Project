//@file:OptIn(ExperimentalPermissionsApi::class)

package com.example.prm391_project.screen.user

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
//import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
//import kotlinx.coroutines.tasks.await

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current

    val fixedLatLng = LatLng(10.7942, 106.7223)
//    val permissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

//    LaunchedEffect(permissionState.status) {
//        when (permissionState.status) {
//            is PermissionStatus.Granted -> {
//                try {
//                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//                    val location = fusedLocationClient.lastLocation.await()
//                    location?.let {
//                        currentLocation = LatLng(it.latitude, it.longitude)
//                    }
//                } catch (e: Exception) {
//                    Log.e("MapScreen", "Lỗi lấy vị trí: ${e.message}")
//                }
//            }
//
//            is PermissionStatus.Denied -> {
//                permissionState.launchPermissionRequest()
//            }
//        }
//    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fixedLatLng, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
//        properties = MapProperties(isMyLocationEnabled = permissionState.status.isGranted),
        uiSettings = MapUiSettings(myLocationButtonEnabled = true)
    ) {
        Marker(
            state = MarkerState(position = fixedLatLng),
            title = "Landmark 81",
            snippet = "Tọa độ cố định"
        )

        currentLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Vị trí hiện tại",
                snippet = "${it.latitude}, ${it.longitude}"
            )
        }
    }
}
