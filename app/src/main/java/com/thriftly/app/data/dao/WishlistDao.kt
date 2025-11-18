package com.thriftly.app.data.dao

import androidx.room.*
import com.thriftly.app.data.entity.WishlistItem
import com.thriftly.app.data.entity.PriceHistory
import com.thriftly.app.data.entity.WishlistNotificationSettings
import kotlinx.coroutines.flow.Flow

//Data class to represent category breakdown results
data class CategoryCount(
    val category: String,
    val count: Int
)

//Extension function to convert List
fun List<CategoryCount>.toMap(): Map<String, Int> = associate { it.category to it.count }

//Data Access Object for Wishlist operations
@Dao
interface WishlistDao {
    
    // ===== Wishlist Items =====
    @Query("SELECT * FROM wishlist_items WHERE isActive = 1 ORDER BY addedAt DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItem>>
    
    @Query("SELECT * FROM wishlist_items WHERE listingId = :listingId AND isActive = 1")
    suspend fun getWishlistItemByListingId(listingId: String): WishlistItem?
    
    @Query("SELECT * FROM wishlist_items WHERE id = :id")
    suspend fun getWishlistItemById(id: String): WishlistItem?
    
    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE listingId = :listingId AND isActive = 1)")
    suspend fun isInWishlist(listingId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE listingId = :listingId AND isActive = 1)")
    fun isInWishlistFlow(listingId: String): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItem)
    
    @Update
    suspend fun updateWishlistItem(item: WishlistItem)
    
    @Query("UPDATE wishlist_items SET isActive = 0 WHERE id = :id")
    suspend fun removeWishlistItem(id: String)
    
    @Query("UPDATE wishlist_items SET isActive = 0 WHERE listingId = :listingId")
    suspend fun removeWishlistItemByListingId(listingId: String)
    
    @Query("DELETE FROM wishlist_items WHERE isActive = 0")
    suspend fun deleteInactiveWishlistItems()
    
    @Query("SELECT COUNT(*) FROM wishlist_items WHERE isActive = 1")
    fun getWishlistCount(): Flow<Int>
    
    // Price monitoring queries
    @Query("SELECT * FROM wishlist_items WHERE isActive = 1 AND lastPriceCheck < :threshold")
    suspend fun getItemsNeedingPriceCheck(threshold: Long): List<WishlistItem>
    
    @Query("UPDATE wishlist_items SET currentPrice = :newPrice, lastPriceCheck = :checkTime WHERE id = :id")
    suspend fun updatePrice(id: String, newPrice: Double, checkTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE wishlist_items SET priceDropNotified = 1 WHERE id = :id")
    suspend fun markPriceDropNotified(id: String)
    
    // ===== Price History =====
    @Query("SELECT * FROM price_history WHERE wishlistItemId = :wishlistItemId ORDER BY checkedAt DESC")
    fun getPriceHistory(wishlistItemId: String): Flow<List<PriceHistory>>
    
    @Query("SELECT * FROM price_history WHERE wishlistItemId = :wishlistItemId ORDER BY checkedAt DESC LIMIT 10")
    suspend fun getRecentPriceHistory(wishlistItemId: String): List<PriceHistory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(priceHistory: PriceHistory)
    
    @Query("DELETE FROM price_history WHERE checkedAt < :cutoffTime")
    suspend fun deleteOldPriceHistory(cutoffTime: Long)
    
    // ===== Notification Settings =====
    @Query("SELECT * FROM wishlist_notification_settings WHERE id = 'default'")
    suspend fun getNotificationSettings(): WishlistNotificationSettings?
    
    @Query("SELECT * FROM wishlist_notification_settings WHERE id = 'default'")
    fun getNotificationSettingsFlow(): Flow<WishlistNotificationSettings?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNotificationSettings(settings: WishlistNotificationSettings)
    
    // ===== Analytics & Insights =====
    @Query("""
        SELECT AVG(priceChange) FROM price_history 
        WHERE wishlistItemId = :wishlistItemId AND priceChange < 0 
        AND checkedAt > :since
    """)
    suspend fun getAveragePriceDropForItem(wishlistItemId: String, since: Long): Double?
    
    @Query("""
        SELECT COUNT(*) FROM price_history 
        WHERE wishlistItemId = :wishlistItemId AND priceChange < 0 
        AND checkedAt > :since
    """)
    suspend fun getPriceDropCountForItem(wishlistItemId: String, since: Long): Int
    
    @Query("SELECT category, COUNT(*) as count FROM wishlist_items WHERE isActive = 1 GROUP BY category")
    suspend fun getWishlistCategoryBreakdown(): List<CategoryCount>
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/