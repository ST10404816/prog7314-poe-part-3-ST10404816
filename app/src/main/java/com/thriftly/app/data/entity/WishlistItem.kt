package com.thriftly.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// Wishlist item entity for Room database
@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val listingId: String,
    val listingTitle: String,
    val originalPrice: Double,
    val currentPrice: Double,
    val lastCheckedPrice: Double = currentPrice,
    val category: String,
    val condition: String,
    val sellerName: String,
    val imageUrl: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val lastPriceCheck: Long = System.currentTimeMillis(),
    val priceDropNotified: Boolean = false,
    val targetPrice: Double? = null, // Optional price alert threshold
    val isActive: Boolean = true // Allow soft deletion
)


// Price history tracking for wishlist items
@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val wishlistItemId: String,
    val listingId: String,
    val oldPrice: Double,
    val newPrice: Double,
    val priceChange: Double = newPrice - oldPrice,
    val changePercentage: Double = if (oldPrice > 0) ((newPrice - oldPrice) / oldPrice) * 100 else 0.0,
    val checkedAt: Long = System.currentTimeMillis(),
    val notificationSent: Boolean = false
)

// Notification preferences for price monitoring
@Entity(tableName = "wishlist_notification_settings")
data class WishlistNotificationSettings(
    @PrimaryKey val id: String = "default",
    val enablePriceDropAlerts: Boolean = true,
    val enableTargetPriceAlerts: Boolean = true,
    val minimumDropPercentage: Double = 10.0, // Minimum % drop to trigger notification
    val minimumDropAmount: Double = 50.0, // Minimum rand amount drop to trigger notification
    val checkIntervalHours: Int = 6, // How often to check prices
    val enableDailyDigest: Boolean = false,
    val dailyDigestTime: String = "09:00", // Time for daily digest
    val lastUpdated: Long = System.currentTimeMillis()
)

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/