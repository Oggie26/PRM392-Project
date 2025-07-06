// File: app/src/main/java/com/example/prm391_project/utils/NotificationHelper.kt
package com.example.prm391_project.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.prm391_project.MainActivity
import com.example.prm391_project.R

object NotificationHelper {

    private const val CHANNEL_ID = "cart_alert_channel"
    private const val CHANNEL_NAME = "Cart Alerts"
    private const val CHANNEL_DESCRIPTION = "Alerts for items in your shopping cart"
    private const val NOTIFICATION_ID = 101

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // HIGH importance cho heads-up
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = context.resources.getColor(R.color.purple_200, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100)
                // Đặt thêm các thuộc tính để đảm bảo heads-up notification
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                // Đặt âm thanh mặc định
                setSound(
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationHelper", "Notification Channel created with HIGH importance for heads-up display.")
        }
    }

    fun showCartNotification(context: Context, productCount: Int) {
        // Tạo Intent cho full screen (quan trọng cho heads-up)
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_cart", true)
            putExtra("navigate_to_route", "cart")
        }

        val fullScreenPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Tạo Intent cho tap thông thường
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_cart", true)
            putExtra("navigate_to_route", "cart")
        }

        val tapPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "🛒 Giỏ hàng của bạn đang chờ!"
        val message = "Bạn có $productCount sản phẩm trong giỏ hàng. Hoàn tất mua sắm ngay! 🛍️"
        val bigText = "🎉 Đừng để sản phẩm yêu thích chờ đợi quá lâu! Bạn có $productCount sản phẩm trong giỏ hàng. Hoàn tất đơn hàng ngay để không bỏ lỡ cơ hội sở hữu những món đồ tuyệt vời này. 💫"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cart_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH priority cho heads-up
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Âm thanh, rung, đèn
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(false) // Luôn alert
            // QUAN TRỌNG: Thêm full screen intent để đảm bảo heads-up
            .setFullScreenIntent(fullScreenPendingIntent, true)
            // Thêm action button
            .addAction(
                R.drawable.ic_cart_notification,
                "Xem giỏ hàng ngay",
                tapPendingIntent
            )
            .setColor(context.resources.getColor(R.color.purple_200, null))
            .setLargeIcon(null as android.graphics.Bitmap?)
            // Đặt thời gian hiển thị
            .setTimeoutAfter(15000L) // 15 giây
            // Đặt âm thanh và rung riêng để chắc chắn
            .setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            // Đặt ticker text cho heads-up
            .setTicker("Giỏ hàng đang chờ bạn!")
            // Đặt sub text
            .setSubText("Mua sắm ngay!")

        // Hiển thị notification
        try {
            with(NotificationManagerCompat.from(context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED) {

                        // Kiểm tra channel settings
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
                            Log.d("NotificationHelper", "Channel importance: ${channel?.importance}")
                        }

                        notify(NOTIFICATION_ID, builder.build())
                        Log.d("NotificationHelper", "Heads-up notification sent with $productCount items")
                    } else {
                        Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted")
                    }
                } else {
                    notify(NOTIFICATION_ID, builder.build())
                    Log.d("NotificationHelper", "Heads-up notification sent with $productCount items")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error showing notification: ${e.message}", e)
        }
    }

    // Thêm method để kiểm tra notification có thể hiển thị heads-up không
    fun canShowHeadsUpNotification(context: Context): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)

        // Kiểm tra notification có được enable không
        if (!notificationManager.areNotificationsEnabled()) {
            Log.w("NotificationHelper", "Notifications are disabled for this app")
            return false
        }

        // Kiểm tra channel (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = systemNotificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel == null) {
                Log.w("NotificationHelper", "Notification channel not found")
                return false
            }
            if (channel.importance < NotificationManager.IMPORTANCE_HIGH) {
                Log.w("NotificationHelper", "Channel importance too low for heads-up: ${channel.importance}")
                return false
            }
        }

        // Kiểm tra Do Not Disturb
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val filterLevel = systemNotificationManager.currentInterruptionFilter
            if (filterLevel == NotificationManager.INTERRUPTION_FILTER_NONE ||
                filterLevel == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
                Log.w("NotificationHelper", "Do Not Disturb is enabled")
                return false
            }
        }

        return true
    }

    // Method để tạo một notification test
    fun showTestNotification(context: Context) {
        if (!canShowHeadsUpNotification(context)) {
            Log.w("NotificationHelper", "Cannot show heads-up notification due to settings")
            return
        }

        showCartNotification(context, 5) // Test với 5 sản phẩm
    }
}