package com.example.prm391_project.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun getStatusColorAndIcon(status: String?): Pair<Color, ImageVector> {
    return when (status?.lowercase()) {
        "pending", "chờ xử lý" -> Color(0xFFFF9800) to Icons.Default.AccessTime
        "processing", "đang xử lý" -> Color(0xFF2196F3) to Icons.Default.Receipt
        "shipped", "đang giao" -> Color(0xFF9C27B0) to Icons.Default.LocalShipping
        "delivered", "đã giao" -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
        "cancelled", "đã hủy" -> Color(0xFFF44336) to Icons.Default.Cancel
        else -> Color(0xFF757575) to Icons.Default.AccessTime
    }
}