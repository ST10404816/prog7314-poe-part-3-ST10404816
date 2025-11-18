package com.thriftly.app.data.repo

import android.content.Context
import android.util.Log
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.data.offline.OfflineManager
import com.thriftly.app.data.offline.OfflineException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base repository class providing common offline functionality
 * Extend this class for repositories that need offline support
 */
abstract class BaseOfflineRepository<T>(protected val context: Context) {
    protected val TAG = this::class.java.simpleName
    
    // Common state flows
    protected val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    protected val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    /**
     * Abstract method to fetch data from remote source
     */
    protected abstract suspend fun fetchFromRemote(): T
    
    /**
     * Abstract method to fetch data from local cache
     */
    protected abstract suspend fun fetchFromLocal(): T
    
    /**
     * Abstract method to cache data locally
     */
    protected abstract suspend fun cacheData(data: T)
    
    /**
     * Generic fetch with offline support
     */
    protected suspend fun fetchWithOfflineSupport(
        showOfflineMessage: Boolean = true
    ): Result<T> {
        _isLoading.value = true
        _error.value = null
        
        val result = OfflineManager.fetchWithOfflineSupport(
            context = context,
            remoteDataFetcher = { 
                Log.d(TAG, "Fetching from remote")
                fetchFromRemote()
            },
            localDataFetcher = { 
                Log.d(TAG, "Fetching from local cache")
                fetchFromLocal()
            },
            cacheUpdater = { data ->
                Log.d(TAG, "Caching data")
                cacheData(data)
                _isOfflineMode.value = false
            },
            showOfflineMessage = showOfflineMessage
        )
        
        result.fold(
            onSuccess = { data ->
                _error.value = null
                Log.d(TAG, "Successfully loaded data")
            },
            onFailure = { exception ->
                val errorMessage = when (exception) {
                    is OfflineException -> "No offline data available"
                    else -> "Failed to load data: ${exception.message}"
                }
                _error.value = errorMessage
                Log.e(TAG, "Error loading data", exception)
                _isOfflineMode.value = true
            }
        )
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Check if device is currently online
     */
    protected fun isOnline(): Boolean {
        return ConnectivityUtils.hasActiveConnection(context)
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Show offline notification
     */
    protected fun showOfflineNotification() {
        OfflineManager.showOfflineNotification(context)
        _isOfflineMode.value = true
    }
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/