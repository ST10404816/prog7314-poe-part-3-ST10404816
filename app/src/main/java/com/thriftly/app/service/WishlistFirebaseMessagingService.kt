package com.thriftly.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.thriftly.app.R
import com.thriftly.app.MainActivity
import kotlinx.coroutines.*

/**
 * Firebase Messaging Service for push notifications
 * Handles price drop alerts and other wishlist notifications
 */
class WishlistFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "WishlistFCM"
        const val PRICE_DROP_CHANNEL_ID = "price_drop_notifications"
        const val GENERAL_CHANNEL_ID = "general_notifications"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    /**
     * Called when message is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(
                title = it.title ?: "Thriftly",
                body = it.body ?: "",
                type = remoteMessage.data["type"] ?: "general"
            )
        }
    }
    
    /**
     * Called when new token is generated
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }
    
    /**
     * Handle data message for different notification types
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: "general"
        
        when (type) {
            "price_drop" -> handlePriceDropNotification(data)
            "target_price_reached" -> handleTargetPriceNotification(data)
            "daily_digest" -> handleDailyDigestNotification(data)
            else -> handleGeneralNotification(data)
        }
    }
    
    /**
     * Handle price drop notifications
     */
    private fun handlePriceDropNotification(data: Map<String, String>) {
        val itemTitle = data["item_title"] ?: "Item"
        val oldPrice = data["old_price"]?.toDoubleOrNull() ?: 0.0
        val newPrice = data["new_price"]?.toDoubleOrNull() ?: 0.0
        val priceDropPercent = data["price_drop_percent"]?.toIntOrNull() ?: 0
        
        val title = "💸 Price Drop Alert!"
        val body = "$itemTitle dropped ${priceDropPercent}% (R${oldPrice.toInt()} → R${newPrice.toInt()})"
        
        sendNotification(
            title = title,
            body = body,
            type = "price_drop",
            channelId = PRICE_DROP_CHANNEL_ID,
            data = data
        )
    }
    
    /**
     * Handle target price reached notifications
     */
    private fun handleTargetPriceNotification(data: Map<String, String>) {
        val itemTitle = data["item_title"] ?: "Item"
        val targetPrice = data["target_price"]?.toDoubleOrNull() ?: 0.0
        val currentPrice = data["current_price"]?.toDoubleOrNull() ?: 0.0
        
        val title = "🎯 Target Price Reached!"
        val body = "$itemTitle is now R${currentPrice.toInt()} (target: R${targetPrice.toInt()})"
        
        sendNotification(
            title = title,
            body = body,
            type = "target_price_reached",
            channelId = PRICE_DROP_CHANNEL_ID,
            data = data
        )
    }
    
    /**
     * Handle daily digest notifications
     */
    private fun handleDailyDigestNotification(data: Map<String, String>) {
        val priceDropCount = data["price_drop_count"]?.toIntOrNull() ?: 0
        val totalSavings = data["total_savings"]?.toDoubleOrNull() ?: 0.0
        
        val title = "📊 Daily Wishlist Digest"
        val body = if (priceDropCount > 0) {
            "$priceDropCount price drops today! Total potential savings: R${totalSavings.toInt()}"
        } else {
            "No price changes in your wishlist today"
        }
        
        sendNotification(
            title = title,
            body = body,
            type = "daily_digest",
            channelId = GENERAL_CHANNEL_ID,
            data = data
        )
    }
    
    /**
     * Handle general notifications
     */
    private fun handleGeneralNotification(data: Map<String, String>) {
        val title = data["title"] ?: "Thriftly"
        val body = data["body"] ?: "You have a new notification"
        
        sendNotification(
            title = title,
            body = body,
            type = "general",
            channelId = GENERAL_CHANNEL_ID,
            data = data
        )
    }
    
    /**
     * Create the notification and display it
     */
    private fun sendNotification(
        title: String,
        body: String,
        type: String,
        channelId: String = GENERAL_CHANNEL_ID,
        data: Map<String, String> = emptyMap()
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type)
            // Add any additional data for deep linking
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Choose appropriate icon based on notification type
        val iconRes = when (type) {
            "price_drop", "target_price_reached" -> R.drawable.logo // Use your wishlist icon
            else -> R.drawable.logo
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROMO)
        
        // Add action buttons for price drop notifications
        if (type == "price_drop" || type == "target_price_reached") {
            val listingId = data["listing_id"]
            if (!listingId.isNullOrEmpty()) {
                // View item action
                val viewIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("deep_link", "listing/$listingId")
                }
                val viewPendingIntent = PendingIntent.getActivity(
                    this,
                    1,
                    viewIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    R.drawable.logo,
                    "View Item",
                    viewPendingIntent
                )
                
                // View wishlist action
                val wishlistIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("deep_link", "wishlist")
                }
                val wishlistPendingIntent = PendingIntent.getActivity(
                    this,
                    2,
                    wishlistIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    R.drawable.logo,
                    "Wishlist",
                    wishlistPendingIntent
                )
            }
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Generate unique notification ID
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    /**
     * Create notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Price drop notifications channel
            val priceDropChannel = NotificationChannel(
                PRICE_DROP_CHANNEL_ID,
                "Price Drop Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for price drops on your wishlist items"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // General notifications channel
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Register channels
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(priceDropChannel)
            notificationManager.createNotificationChannel(generalChannel)
            
            Log.d(TAG, "Notification channels created")
        }
    }
    
    /**
     * Send registration token to your server for targeted notifications
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement server registration
        // This would typically send the token to your backend server
        // For demo purposes, we'll just log it
        Log.d(TAG, "Token registered: $token")
        
        // In a real implementation, you would:
        // 1. Send token to your server
        // 2. Associate it with the user's account
        // 3. Use it for targeted notifications
        
        // Example API call:
        // ApiService.registerFCMToken(token, userId)
    }
}