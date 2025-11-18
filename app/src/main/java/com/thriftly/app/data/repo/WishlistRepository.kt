package com.thriftly.app.data.repo

import android.content.Context
import android.util.Log
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.data.offline.OfflineManager
import com.thriftly.app.data.db.ThriftlyDatabase
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.data.entity.WishlistItem
import com.thriftly.app.data.entity.PriceHistory
import com.thriftly.app.data.entity.WishlistNotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Repository for Wishlist operations with offline support
 * Handles wishlist CRUD operations, price monitoring, and notifications
 */
class WishlistRepository(ctx: Context) : BaseOfflineRepository<List<WishlistItem>>(ctx) {
    
    private val database = ThriftlyDatabase.get(ctx)
    private val wishlistDao = database.wishlist()
    private val listingsDao = database.listings()
    
    companion object {
        private const val TAG = "WishlistRepository"
        private const val PRICE_CHECK_INTERVAL_HOURS = 6
    }
    
    // ===== Wishlist CRUD Operations =====
    
    /**
     * Get all wishlist items as a Flow for reactive UI updates
     */
    fun getWishlistItems(): Flow<List<WishlistItem>> {
        return wishlistDao.getAllWishlistItems()
    }
    
    /**
     * Get wishlist count for UI badges
     */
    fun getWishlistCount(): Flow<Int> {
        return wishlistDao.getWishlistCount()
    }
    
    /**
     * Check if a listing is in the wishlist
     */
    fun isInWishlist(listingId: String): Flow<Boolean> {
        return wishlistDao.isInWishlistFlow(listingId)
    }
    
    /**
     * Add a listing to the wishlist
     */
    suspend fun addToWishlist(listing: Listing): Result<WishlistItem> = withContext(Dispatchers.IO) {
        try {
            // Check if already in wishlist
            val existing = wishlistDao.getWishlistItemByListingId(listing.id)
            if (existing != null && existing.isActive) {
                return@withContext Result.failure(Exception("Item already in wishlist"))
            }
            
            val wishlistItem = WishlistItem(
                listingId = listing.id,
                listingTitle = listing.title,
                originalPrice = listing.price,
                currentPrice = listing.price,
                category = listing.category,
                condition = listing.condition,
                sellerName = listing.sellerName,
                imageUrl = listing.imageUris.firstOrNull()
            )
            
            wishlistDao.insertWishlistItem(wishlistItem)
            
            // If online, sync with remote server
            if (ConnectivityUtils.isNetworkAvailable(context)) {
                syncWishlistWithRemote()
            } else {
                // Queue offline action with OfflineManager object
                // For now, just log the action as OfflineManager object doesn't have queue method
                Log.d(TAG, "Offline: Would queue add_to_wishlist action for ${listing.id}")
            }
            
            Log.d(TAG, "Added ${listing.title} to wishlist")
            Result.success(wishlistItem)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to wishlist", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove a listing from the wishlist
     */
    suspend fun removeFromWishlist(listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            wishlistDao.removeWishlistItemByListingId(listingId)
            
            // If online, sync with remote server
            if (ConnectivityUtils.isNetworkAvailable(context)) {
                syncWishlistWithRemote()
            } else {
                // Queue offline action with OfflineManager object
                // For now, just log the action as OfflineManager object doesn't have queue method
                Log.d(TAG, "Offline: Would queue remove_from_wishlist action for $listingId")
            }
            
            Log.d(TAG, "Removed listing $listingId from wishlist")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from wishlist", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle wishlist status for a listing
     */
    suspend fun toggleWishlist(listing: Listing): Result<Boolean> = withContext(Dispatchers.IO) {
        val isInWishlist = wishlistDao.isInWishlist(listing.id)
        
        return@withContext if (isInWishlist) {
            removeFromWishlist(listing.id).map { false }
        } else {
            addToWishlist(listing).map { true }
        }
    }
    
    /**
     * Set target price alert for a wishlist item
     */
    suspend fun setTargetPrice(wishlistItemId: String, targetPrice: Double?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val item = wishlistDao.getWishlistItemById(wishlistItemId)
            if (item != null) {
                wishlistDao.updateWishlistItem(item.copy(targetPrice = targetPrice))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Wishlist item not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting target price", e)
            Result.failure(e)
        }
    }
    
    // ===== Price Monitoring =====
    
    /**
     * Check for price updates on all wishlist items
     */
    suspend fun checkPriceUpdates(): Result<List<PriceHistory>> = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val checkThreshold = currentTime - TimeUnit.HOURS.toMillis(PRICE_CHECK_INTERVAL_HOURS.toLong())
            
            val itemsToCheck = wishlistDao.getItemsNeedingPriceCheck(checkThreshold)
            val priceUpdates = mutableListOf<PriceHistory>()
            
            if (!ConnectivityUtils.isNetworkAvailable(context)) {
                Log.d(TAG, "No internet connection - skipping price checks")
                return@withContext Result.success(emptyList())
            }
            
            for (item in itemsToCheck) {
                try {
                    // Fetch current listing data (simulate API call)
                    val currentListing = getCurrentListingPrice(item.listingId)
                    if (currentListing != null && currentListing.price != item.currentPrice) {
                        // Price has changed
                        val priceHistory = PriceHistory(
                            wishlistItemId = item.id,
                            listingId = item.listingId,
                            oldPrice = item.currentPrice,
                            newPrice = currentListing.price
                        )
                        
                        // Update wishlist item with new price
                        wishlistDao.updatePrice(item.id, currentListing.price, currentTime)
                        
                        // Save price history
                        wishlistDao.insertPriceHistory(priceHistory)
                        priceUpdates.add(priceHistory)
                        
                        Log.d(TAG, "Price changed for ${item.listingTitle}: ${item.currentPrice} -> ${currentListing.price}")
                    } else {
                        // No price change, just update last check time
                        wishlistDao.updatePrice(item.id, item.currentPrice, currentTime)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking price for ${item.listingTitle}", e)
                }
            }
            
            Result.success(priceUpdates)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during price check", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get price history for a wishlist item
     */
    fun getPriceHistory(wishlistItemId: String): Flow<List<PriceHistory>> {
        return wishlistDao.getPriceHistory(wishlistItemId)
    }
    
    /**
     * Get items that have significant price drops for notifications
     */
    suspend fun getItemsWithPriceDrops(): Result<List<WishlistItem>> = withContext(Dispatchers.IO) {
        try {
            val settings = getNotificationSettings().first()
            val allItems = wishlistDao.getAllWishlistItems().first()
            
            val itemsWithDrops = allItems.filter { item ->
                val priceDrop = item.lastCheckedPrice - item.currentPrice
                val dropPercentage = if (item.lastCheckedPrice > 0) {
                    (priceDrop / item.lastCheckedPrice) * 100
                } else 0.0
                
                !item.priceDropNotified && (
                    priceDrop >= settings.minimumDropAmount || 
                    dropPercentage >= settings.minimumDropPercentage ||
                    (item.targetPrice != null && item.currentPrice <= item.targetPrice)
                )
            }
            
            Result.success(itemsWithDrops)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting items with price drops", e)
            Result.failure(e)
        }
    }
    
    /**
     * Mark price drop as notified
     */
    suspend fun markPriceDropNotified(wishlistItemId: String) = withContext(Dispatchers.IO) {
        wishlistDao.markPriceDropNotified(wishlistItemId)
    }
    
    // ===== Notification Settings =====
    
    /**
     * Get notification settings
     */
    fun getNotificationSettings(): Flow<WishlistNotificationSettings> {
        return wishlistDao.getNotificationSettingsFlow()
            .map { settings ->
                settings ?: WishlistNotificationSettings() // Default settings
            }
    }
    
    /**
     * Update notification settings
     */
    suspend fun updateNotificationSettings(settings: WishlistNotificationSettings): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            wishlistDao.updateNotificationSettings(settings.copy(lastUpdated = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification settings", e)
            Result.failure(e)
        }
    }
    
    // ===== Data Management =====
    
    /**
     * Clean up old price history data
     */
    suspend fun cleanupOldData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30) // Keep 30 days
            wishlistDao.deleteOldPriceHistory(cutoffTime)
            wishlistDao.deleteInactiveWishlistItems()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync wishlist with remote server
     */
    private suspend fun syncWishlistWithRemote() {
        // Implementation would sync with your backend API
        // For now, this is a placeholder
        Log.d(TAG, "Syncing wishlist with remote server")
    }
    
    /**
     * Simulate fetching current listing price from API
     */
    private suspend fun getCurrentListingPrice(listingId: String): Listing? {
        // In a real app, this would make an API call to get current listing data
        // For demo, we'll get from local database
        return listingsDao.get(listingId)
    }
    
    // Abstract method implementations for BaseOfflineRepository
    override suspend fun fetchFromRemote(): List<WishlistItem> {
        // In a real implementation, this would fetch from API
        // For now, return empty list as we handle remote sync differently
        return emptyList()
    }
    
    override suspend fun fetchFromLocal(): List<WishlistItem> {
        return wishlistDao.getAllWishlistItems().first()
    }
    
    override suspend fun cacheData(data: List<WishlistItem>) {
        // Cache is handled by Room database automatically
        // This method is required by base class but not used in our implementation
    }
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/