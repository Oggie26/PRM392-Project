package com.example.prm391_project.screen.user

import TokenManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import retrofit2.HttpException
import com.example.prm391_project.config.RetrofitClient
import okio.IOException

data class CartDetail(
    val id: String,
    val productName: String,
    val price: String,
    val imageThumnail: String,
    val image: String,
    val quantity: Int,
    val color: String,
    val sizes: String,
    val description : String,

)

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    //Logic gọi API get detail
    val fetchProductDetail: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            error = null

            try {
                val response = RetrofitClient.productService.getProductDetail(productId)

                Log.d("ProductCartScreen", "Cart API Response Code: ${response.code}")
                Log.d("ProductCartScreen", "Cart API Response Message: ${response.message}")
                Log.d("ProductCartScreen", "Cart API Response Data: ${response.data}")

                if (response.code == 200) {
                    val product = response.data
                }

            } catch (e: HttpException) {
                error = "Lỗi HTTP: ${e.code()} - ${e.message()}"
                Log.e("ProductCartScreen", "HTTP Exception: ${e.message()}", e)
            } catch (e: IOException) {
                error = "Lỗi mạng: Không thể kết nối đến server."
                Log.e("ProductCartScreen", "IO Exception: ${e.message}", e)
            } catch (e: Exception) {
                error = "Lỗi không xác định: ${e.message}"
                Log.e("ProductCartScreen", "General Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchProductDetail()
    }

}