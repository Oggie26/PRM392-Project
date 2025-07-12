package com.example.prm391_project.screen.user

import TokenManager
import retrofit2.HttpException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Refresh
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
import androidx.navigation.NavController
import com.example.prm391_project.R
import com.airbnb.lottie.compose.*
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.example.prm391_project.Screen
import com.example.prm391_project.config.RetrofitClient
import kotlinx.coroutines.delay
import java.io.IOException
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import okhttp3.OkHttpClient
import javax.net.ssl.TrustManager
import java.net.URI
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Data classes
interface IResponse<T> {
    val code: Int
    val data: T?
    val message: String?
}

data class UserProfileResponse(
    val id: String,
    val fullName: String
)

interface AuthService {
    suspend fun getUserProfile(authHeader: String): IResponse<UserProfileResponse>
}

data class Message(
    val id: String,
    val content: String,
    val sender: String,
    val timestamp: String,
    val status: String,
    val customerName: String? = null,
    val storeName: String? = null,
    val read: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var messageInput by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isStoreTyping by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    var typingTimer by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var socket by remember { mutableStateOf<Socket?>(null) }
    var serverIp by remember { mutableStateOf("") }
    var showIpDialog by remember { mutableStateOf(true) }
    var ipInput by remember { mutableStateOf("192.168.1.35") }
    var isConnecting by remember { mutableStateOf(false) }
    var userInfo by remember { mutableStateOf<Pair<String?, String?>>(null to null) }

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context.applicationContext) }
    val listState = rememberLazyListState()

    // Improved SSL handling
    val createTrustAllSslContext: () -> SSLContext = {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        })

        SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    }

    // Improved connection test
    suspend fun testConnection(ip: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatScreen", "Testing connection to $ip...")

                val sslContext = createTrustAllSslContext()
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .hostnameVerifier { _, _ -> true }
                    .sslSocketFactory(sslContext.socketFactory, object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    })
                    .build()

                val options = IO.Options().apply {
                    transports = arrayOf("websocket")
                    timeout = 5000
                    reconnection = false
                    callFactory = okHttpClient
                    webSocketFactory = okHttpClient
                }

                val testSocket = IO.socket("https://$ip:3000", options)
                var connected = false
                var errorOccurred = false

                testSocket.on(Socket.EVENT_CONNECT) {
                    Log.d("ChatScreen", "Test connection successful")
                    connected = true
                }

                testSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e("ChatScreen", "Test connection failed: ${args.contentToString()}")
                    errorOccurred = true
                }

                testSocket.connect()

                // Wait for connection or error
                var attempts = 0
                while (!connected && !errorOccurred && attempts < 50) {
                    delay(100)
                    attempts++
                }

                testSocket.disconnect()
                connected && !errorOccurred

            } catch (e: Exception) {
                Log.e("ChatScreen", "Connection test exception: ${e.message}", e)
                false
            }
        }
    }

    // Get user info
    suspend fun getUserInfoFromToken(): Pair<String?, String?> {
        val token = tokenManager.getToken()
        return if (token != null) {
            try {
                val authHeader = "Bearer $token"
                val response = RetrofitClient.authService.getUserProfile(authHeader)
                if (response.code == 200 && response.data != null) {
                    Pair(response.data!!.id, response.data!!.fullName)
                } else {
                    throw Exception(response.message ?: "Không thể lấy thông tin profile")
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error getting user info: ${e.message}", e)
                throw e
            }
        } else {
            throw Exception("Token không tồn tại")
        }
    }

    // Initialize socket connection
    fun initializeSocket(ip: String): Socket? {
        try {
            val sslContext = createTrustAllSslContext()
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(0, java.util.concurrent.TimeUnit.SECONDS) // No timeout for persistent connection
                .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .hostnameVerifier { _, _ -> true }
                .sslSocketFactory(sslContext.socketFactory, object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                })
                .build()

            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                timeout = 10000
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                callFactory = okHttpClient
                webSocketFactory = okHttpClient
            }

            return IO.socket("https://$ip:3000", options)
        } catch (e: Exception) {
            Log.e("ChatScreen", "Error initializing socket: ${e.message}", e)
            return null
        }
    }

    fun setupSocketListeners(socket: Socket, customerId: String, customerName: String) {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("ChatScreen", "Connected to server")
            connectionStatus = "Connected"
            isLoading = false

            // Join customer room
            socket.emit("joinCustomerRoom", JSONObject().apply {
                put("customerId", customerId)
                put("customerName", customerName)
            })
        }

        socket.on("initMessages") { args ->
            try {
                val messagesArray = args[0] as? Array<*>
                if (messagesArray != null) {
                    val newMessages = messagesArray.mapNotNull { msg ->
                        try {
                            val json = msg as JSONObject
                            Message(
                                id = json.getString("id"),
                                content = json.getString("content"),
                                sender = json.getString("sender"),
                                timestamp = json.getLong("timestamp").toString(),
                                status = json.getString("status"),
                                customerName = json.optString("customerName"),
                                storeName = json.optString("storeName"),
                                read = json.optBoolean("read", false)
                            )
                        } catch (e: Exception) {
                            Log.e("ChatScreen", "Error parsing message", e)
                            null
                        }
                    }
                    messages = newMessages
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error loading messages", e)
            }
        }

        socket.on("newMessage") { args ->
            try {
                val json = args[0] as JSONObject
                val newMessage = Message(
                    id = json.getString("id"),
                    content = json.getString("content"),
                    sender = json.getString("sender"),
                    timestamp = json.getLong("timestamp").toString(),
                    status = json.getString("status"),
                    customerName = json.optString("customerName"),
                    storeName = json.optString("storeName"),
                    read = json.optBoolean("read", false)
                )
                messages = messages + newMessage
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error receiving message", e)
            }
        }

        socket.on("userTyping") { args ->
            try {
                val json = args[0] as JSONObject
                val sender = json.getString("sender")
                val typing = json.getBoolean("isTyping")
                if (sender == "store") {
                    isStoreTyping = typing
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error handling typing indicator", e)
            }
        }

        socket.on("messagesRead") { args ->
            try {
                messages = messages.map { message ->
                    if (message.sender == "customer") {
                        message.copy(read = true)
                    } else {
                        message
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error updating read status", e)
            }
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val errorMessage = args.getOrNull(0)?.toString() ?: "Unknown error"
            Log.e("ChatScreen", "Connection error: $errorMessage")
            connectionStatus = "Connection failed"
            error = "Kết nối thất bại: $errorMessage"
            isLoading = false
        }

        socket.on(Socket.EVENT_DISCONNECT) { args ->
            val reason = args.getOrNull(0)?.toString() ?: "Unknown"
            Log.d("ChatScreen", "Disconnected: $reason")
            connectionStatus = "Disconnected"


        }

        socket.on("error") { args ->
            val errorMessage = args.getOrNull(0)?.toString() ?: "Server error"
            Log.e("ChatScreen", "Socket error: $errorMessage")
            error = "Lỗi server: $errorMessage"
        }
    }


    // Setup socket event listeners

    // Connect to server
    fun connectToServer(ip: String) {
        coroutineScope.launch {
            isConnecting = true
            connectionStatus = "Connecting..."
            error = null

            try {
                // Get user info first
                userInfo = getUserInfoFromToken()
                val (customerId, customerName) = userInfo

                if (customerId.isNullOrEmpty() || customerName.isNullOrEmpty()) {
                    throw Exception("Không thể lấy thông tin người dùng")
                }

                // Test connection
                if (!testConnection(ip)) {
                    throw Exception("Không thể kết nối đến server. Vui lòng kiểm tra:\n• IP address đúng chưa?\n• Server đã khởi động chưa?\n• Cùng mạng WiFi không?")
                }

                // Initialize socket
                socket?.disconnect()
                socket = initializeSocket(ip)

                if (socket == null) {
                    throw Exception("Không thể khởi tạo socket connection")
                }

                setupSocketListeners(socket!!, customerId, customerName)

                withContext(Dispatchers.IO) {
                    socket!!.connect()
                }

                // Wait for connection
                var attempts = 0
                while (connectionStatus == "Connecting..." && attempts < 50) {
                    delay(100)
                    attempts++
                }

                if (connectionStatus == "Connecting...") {
                    throw Exception("Kết nối timeout")
                }

            } catch (e: Exception) {
                Log.e("ChatScreen", "Connection error: ${e.message}", e)
                error = e.message
                connectionStatus = "Connection failed"
                isLoading = false
            } finally {
                isConnecting = false
            }
        }
    }





    // Send message
    val sendMessage: () -> Unit = Unit@{
        if (messageInput.isBlank() || isSending || socket == null || !socket!!.connected()) return@Unit

        coroutineScope.launch {
            isSending = true
            try {
                val messageData = JSONObject().apply {
                    put("content", messageInput.trim())
                    put("sender", "customer")
                }

                socket!!.emit("sendMessage", messageData)
                messageInput = ""

                // Stop typing indicator
                socket!!.emit("typing", JSONObject().apply {
                    put("isTyping", false)
                })

            } catch (e: Exception) {
                Log.e("ChatScreen", "Error sending message", e)
                error = "Lỗi gửi tin nhắn: ${e.message}"
            } finally {
                isSending = false
            }
        }
    }

    // Typing indicator
    val sendTypingIndicator: (Boolean) -> Unit = { isTyping ->
        try {
            socket?.emit("typing", JSONObject().apply {
                put("isTyping", isTyping)
            })
        } catch (e: Exception) {
            Log.e("ChatScreen", "Error sending typing indicator", e)
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.size - 1)
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error scrolling to bottom", e)
            }
        }
    }

    // Typing indicator logic
    LaunchedEffect(messageInput) {
        typingTimer?.cancel()
        if (messageInput.isNotBlank()) {
            sendTypingIndicator(true)
            typingTimer = coroutineScope.launch {
                delay(2000)
                sendTypingIndicator(false)
            }
        } else {
            sendTypingIndicator(false)
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            typingTimer?.cancel()
            socket?.disconnect()
        }
    }

    // IP Input Dialog
    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Cài đặt Server") },
            text = {
                Column {
                    Text(
                        text = "Nhập địa chỉ IP của server chat và cùng mạng WiFi",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it },
                        label = { Text("IP Address") },
                        placeholder = { Text("192.168.1.35") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConnecting
                    )

                    if (isConnecting) {
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang kết nối...", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ipInput.isNotBlank() && !isConnecting) {
                            serverIp = ipInput
                            showIpDialog = false
                            connectToServer(serverIp)
                        }
                    },
                    enabled = !isConnecting && ipInput.isNotBlank()
                ) {
                    Text("Kết nối")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { navController.navigate("home")},
                    enabled = !isConnecting
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Main UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat với Cửa hàng", fontWeight = FontWeight.Bold)
                        Text(
                            text = connectionStatus,
                            fontSize = 12.sp,
                            color = when (connectionStatus) {
                                "Connected" -> Color.DarkGray
                                "Connecting..." -> Color.Blue
                                else -> Color.Red
                            }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (error != null) {
                        IconButton(onClick = { connectToServer(serverIp) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column {
                if (isStoreTyping) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cửa hàng đang nhập...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Nhập tin nhắn...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        enabled = connectionStatus == "Connected"
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = sendMessage,
                        enabled = !isSending && messageInput.isNotBlank() && connectionStatus == "Connected"
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (messageInput.isNotBlank() && connectionStatus == "Connected")
                                    Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                            Text(
                                text = "Đang kết nối...",
                                modifier = Modifier.padding(top = 16.dp),
                                color = Color.Gray
                            )
                        }
                    }
                }

                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = error!!,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(
                                onClick = {
                                    showIpDialog = true
                                    error = null
                                }
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }

                messages.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Chưa có tin nhắn nào!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Gửi tin nhắn đầu tiên để bắt đầu trò chuyện",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageItem(message)
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val isCustomer = message.sender == "customer"
    val formatter = remember { SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCustomer) 16.dp else 4.dp,
                bottomEnd = if (isCustomer) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isCustomer) Color(0xFFDCF8C6) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 16.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = try {
                            formatter.format(Date(message.timestamp.toLong()))
                        } catch (e: Exception) {
                            "Thời gian không hợp lệ"
                        },
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    if (isCustomer) {
                        Text(
                            text = if (message.read) "✓✓" else "✓",
                            fontSize = 12.sp,
                            color = if (message.read) Color.Blue else Color.Gray
                        )
                    }
                }
            }
        }
    }
}