package com.thriftly.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.data.repo.ThriftlyRepository
import com.thriftly.app.data.offline.OfflineManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel with comprehensive offline support
 * Handles UI data, loading states, and error management
 */
class ListingsViewModel(private val repo: ThriftlyRepository, private val context: Context) : ViewModel() {
    private val TAG = "ListingsViewModel"
    
    // Read-only stream of listings for the UI to collect
    val items: StateFlow<List<Listing>> = repo.listingsFlow
    
    // Loading and error states from repository
    val isLoading: StateFlow<Boolean> = repo.isLoading
    val error: StateFlow<String?> = repo.error
    val isOfflineMode: StateFlow<Boolean> = repo.isOfflineMode
    
    // UI-specific states
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _showOfflineMessage = MutableStateFlow(false)
    val showOfflineMessage: StateFlow<Boolean> = _showOfflineMessage.asStateFlow()
    
    init {
        // Monitor connectivity for UI feedback
        OfflineManager.monitorConnectivity(
            scope = viewModelScope,
            onConnected = {
                Log.d(TAG, "Connected - refreshing data")
                _showOfflineMessage.value = false
                viewModelScope.launch {
                    refreshFromRemote(showLoadingIndicator = false)
                }
            },
            onDisconnected = {
                Log.d(TAG, "Disconnected - showing offline message")
                _showOfflineMessage.value = true
                OfflineManager.showOfflineNotification(context)
            }
        )
    }
    
    /**
     * Refresh data with offline support
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                repo.refreshData(showOfflineMessage = true)
            } catch (t: Throwable) {
                Log.e(TAG, "Error refreshing data", t)
                // Error is handled by repository
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Force refresh from remote server
     */
    fun refreshFromRemote(showLoadingIndicator: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoadingIndicator) {
                    _isRefreshing.value = true
                }
                
                val result = repo.forceRefreshFromRemote()
                result.fold(
                    onSuccess = { listings ->
                        Log.d(TAG, "Successfully refreshed ${listings.size} listings from remote")
                        _showOfflineMessage.value = false
                    },
                    onFailure = { exception ->
                        Log.w(TAG, "Failed to refresh from remote", exception)
                        if (!ConnectivityUtils.hasActiveConnection(context)) {
                            _showOfflineMessage.value = true
                        }
                    }
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Error in force refresh", t)
            } finally {
                if (showLoadingIndicator) {
                    _isRefreshing.value = false
                }
            }
        }
    }
    
    /**
     * Add new listing with offline support
     */
    fun addListing(listing: Listing) {
        viewModelScope.launch {
            val result = repo.addListing(listing)
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Successfully added listing: ${listing.title}")
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to add listing", exception)
                }
            )
        }
    }
    
    /**
     * Get listing by ID with offline support
     */
    suspend fun getListingById(id: String): Listing? {
        return repo.getListingById(id)
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        repo.clearError()
    }
    
    /**
     * Dismiss offline message
     */
    fun dismissOfflineMessage() {
        _showOfflineMessage.value = false
    }
    
    /**
     * Check if we have cached data
     */
    suspend fun hasCachedData(): Boolean {
        return OfflineManager.hasCachedData { items.value }
    }
    
    /**
     * Retry failed operations
     */
    fun retry() {
        viewModelScope.launch {
            if (ConnectivityUtils.hasActiveConnection(context)) {
                refreshFromRemote()
            } else {
                refresh() // Load from cache
            }
        }
    }
}

/* 
References 

Android Developers. 2025. Kotlin Coroutines and Flow. [Online]. Available at: https://developer.android.com/kotlin/coroutines [Accessed 17 Nov 2025].
*/
