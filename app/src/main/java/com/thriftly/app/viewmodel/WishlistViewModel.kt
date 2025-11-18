package com.thriftly.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.data.entity.WishlistItem
import com.thriftly.app.data.entity.PriceHistory
import com.thriftly.app.data.entity.WishlistNotificationSettings
import com.thriftly.app.data.repo.WishlistRepository
import com.thriftly.app.ConnectivityUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import android.util.Log

/**
 * ViewModel for Wishlist screen and wishlist-related operations
 * Manages wishlist state, price monitoring, and user interactions
 */
class WishlistViewModel(context: Context) : ViewModel() {
    
    private val repository = WishlistRepository(context)
    private val connectivityUtils = ConnectivityUtils
    
    companion object {
        private const val TAG = "WishlistViewModel"
    }
    
    // ===== UI State =====
    
    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()
    
    private val _selectedItem = MutableStateFlow<WishlistItem?>(null)
    val selectedItem: StateFlow<WishlistItem?> = _selectedItem.asStateFlow()
    
    // ===== Data Streams =====
    
    val wishlistItems: Flow<List<WishlistItem>> = repository.getWishlistItems()
        .catch { exception ->
            Log.e(TAG, "Error loading wishlist items", exception)
            _uiState.update { it.copy(error = exception.message) }
        }
    
    val wishlistCount: Flow<Int> = repository.getWishlistCount()
    
    val notificationSettings: Flow<WishlistNotificationSettings> = repository.getNotificationSettings()
    
    val isConnected: StateFlow<Boolean> = connectivityUtils.isConnected
    
    // ===== Wishlist Operations =====
    
    /**
     * Add listing to wishlist
     */
    fun addToWishlist(listing: Listing) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.addToWishlist(listing)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            message = "Added ${listing.title} to wishlist"
                        ) 
                    }
                    Log.d(TAG, "Successfully added ${listing.title} to wishlist")
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to add to wishlist"
                        )
                    }
                    Log.e(TAG, "Failed to add to wishlist", exception)
                }
        }
    }
    
    /**
     * Remove listing from wishlist
     */
    fun removeFromWishlist(listingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.removeFromWishlist(listingId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            message = "Removed from wishlist"
                        ) 
                    }
                    Log.d(TAG, "Successfully removed item from wishlist")
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to remove from wishlist"
                        )
                    }
                    Log.e(TAG, "Failed to remove from wishlist", exception)
                }
        }
    }
    
    /**
     * Toggle wishlist status for a listing
     */
    fun toggleWishlist(listing: Listing) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.toggleWishlist(listing)
                .onSuccess { isNowInWishlist ->
                    val message = if (isNowInWishlist) {
                        "Added ${listing.title} to wishlist"
                    } else {
                        "Removed ${listing.title} from wishlist"
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            message = message
                        ) 
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update wishlist"
                        )
                    }
                }
        }
    }
    
    /**
     * Set target price alert for an item
     */
    fun setTargetPrice(wishlistItemId: String, targetPrice: Double?) {
        viewModelScope.launch {
            repository.setTargetPrice(wishlistItemId, targetPrice)
                .onSuccess {
                    _uiState.update { 
                        it.copy(message = "Price alert ${if (targetPrice != null) "set" else "removed"}")
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message ?: "Failed to set price alert")
                    }
                }
        }
    }
    
    /**
     * Check if a listing is in the wishlist
     */
    fun isInWishlist(listingId: String): Flow<Boolean> {
        return repository.isInWishlist(listingId)
    }
    
    // ===== Price Monitoring =====
    
    /**
     * Manually refresh price data
     */
    fun refreshPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            repository.checkPriceUpdates()
                .onSuccess { priceUpdates ->
                    val message = if (priceUpdates.isNotEmpty()) {
                        "Found ${priceUpdates.size} price changes"
                    } else {
                        "All prices are up to date"
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isRefreshing = false,
                            message = message,
                            lastRefresh = System.currentTimeMillis()
                        ) 
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isRefreshing = false,
                            error = exception.message ?: "Failed to refresh prices"
                        )
                    }
                }
        }
    }
    
    /**
     * Get price history for a specific item
     */
    fun getPriceHistory(wishlistItemId: String): Flow<List<PriceHistory>> {
        return repository.getPriceHistory(wishlistItemId)
    }
    
    // ===== Settings Management =====
    
    /**
     * Update notification settings
     */
    fun updateNotificationSettings(settings: WishlistNotificationSettings) {
        viewModelScope.launch {
            repository.updateNotificationSettings(settings)
                .onSuccess {
                    _uiState.update { 
                        it.copy(message = "Notification settings updated")
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message ?: "Failed to update settings")
                    }
                }
        }
    }
    
    // ===== UI State Management =====
    
    /**
     * Select an item for detailed view
     */
    fun selectItem(item: WishlistItem?) {
        _selectedItem.value = item
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Clear success message
     */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    /**
     * Set filter category
     */
    fun setFilterCategory(category: String?) {
        _uiState.update { it.copy(filterCategory = category) }
    }
    
    /**
     * Set sort option
     */
    fun setSortOption(sortOption: WishlistSortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
    }
    
    /**
     * Toggle settings sheet visibility
     */
    fun toggleSettings() {
        _uiState.update { it.copy(showSettings = !it.showSettings) }
    }
    
    // ===== Data Cleanup =====
    
    /**
     * Clean up old data
     */
    fun cleanupOldData() {
        viewModelScope.launch {
            repository.cleanupOldData()
                .onSuccess {
                    Log.d(TAG, "Old data cleaned up successfully")
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to cleanup old data", exception)
                }
        }
    }
    
    init {
        // Initialize cleanup on startup
        cleanupOldData()
        
        // Monitor connectivity changes
        viewModelScope.launch {
            connectivityUtils.isConnected.collect { connected ->
                if (connected && _uiState.value.lastRefresh == 0L) {
                    // Auto-refresh when coming back online
                    refreshPrices()
                }
            }
        }
    }
}

/**
 * UI State data class for Wishlist screen
 */
data class WishlistUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val lastRefresh: Long = 0L,
    val filterCategory: String? = null,
    val sortOption: WishlistSortOption = WishlistSortOption.DATE_ADDED,
    val showSettings: Boolean = false
)

/**
 * Sorting options for wishlist
 */
enum class WishlistSortOption(val displayName: String) {
    DATE_ADDED("Date Added"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    TITLE_A_TO_Z("Title: A to Z"),
    CATEGORY("Category"),
    BIGGEST_PRICE_DROP("Biggest Price Drop")
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/