package com.thriftly.app.data.offline

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Reusable offline management system for ViewModels and Repositories
 * Handles connectivity checking, offline notifications, and cache management
 */
object OfflineManager {
    private const val TAG = "OfflineManager"
    
    /**
     * Data fetch strategy with offline support
     */
    suspend fun <T> fetchWithOfflineSupport(
        context: Context,
        remoteDataFetcher: suspend () -> T,
        localDataFetcher: suspend () -> T,
        cacheUpdater: suspend (T) -> Unit,
        showOfflineMessage: Boolean = true
    ): Result<T> {
        return try {
            if (ConnectivityUtils.hasActiveConnection(context)) {
                Log.d(TAG, "Online: Fetching from remote")
                val remoteData = remoteDataFetcher()
                cacheUpdater(remoteData)
                Result.success(remoteData)
            } else {
                Log.d(TAG, "Offline: Fetching from cache")
                val cachedData = localDataFetcher()
                if (showOfflineMessage) {
                    showOfflineNotification(context)
                }
                Result.success(cachedData)
            }
        } catch (networkException: Exception) {
            Log.w(TAG, "Network error, falling back to cache", networkException)
            try {
                val cachedData = localDataFetcher()
                if (showOfflineMessage) {
                    showOfflineNotification(context)
                }
                Result.success(cachedData)
            } catch (cacheException: Exception) {
                Log.e(TAG, "Cache access failed", cacheException)
                Result.failure(OfflineException("No cached data available", cacheException))
            }
        }
    }
    
    /**
     * Execute operation with offline-first approach
     */
    suspend fun <T> executeWithOfflineFirst(
        context: Context,
        operation: suspend () -> T,
        fallbackAction: suspend () -> T,
        onOfflineMode: () -> Unit = {}
    ): Result<T> {
        return if (ConnectivityUtils.hasActiveConnection(context)) {
            try {
                Result.success(operation())
            } catch (e: Exception) {
                Log.w(TAG, "Operation failed, using fallback", e)
                onOfflineMode()
                try {
                    Result.success(fallbackAction())
                } catch (fallbackException: Exception) {
                    Result.failure(OfflineException("Fallback operation failed", fallbackException))
                }
            }
        } else {
            Log.d(TAG, "Offline mode detected")
            onOfflineMode()
            try {
                Result.success(fallbackAction())
            } catch (e: Exception) {
                Result.failure(OfflineException("Offline operation failed", e))
            }
        }
    }
    
    /**
     * Show offline mode notification via Toast
     */
    fun showOfflineNotification(context: Context) {
        val message = context.getString(R.string.offline_mode_message)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Log.i(TAG, "Offline notification shown")
    }
    
    /**
     * Show offline mode notification via Snackbar
     */
    suspend fun showOfflineSnackbar(
        snackbarHostState: SnackbarHostState,
        context: Context,
        actionLabel: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        val message = context.getString(R.string.offline_mode_message)
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel
        )
        
        if (result == SnackbarResult.ActionPerformed && onActionClick != null) {
            onActionClick()
        }
    }
    
    /**
     * Monitor connectivity and execute actions on connectivity changes
     */
    fun monitorConnectivity(
        scope: CoroutineScope,
        onConnected: () -> Unit,
        onDisconnected: () -> Unit
    ) {
        scope.launch {
            var previousState: Boolean? = null
            ConnectivityUtils.isConnected.collect { isConnected ->
                if (previousState != null && previousState != isConnected) {
                    if (isConnected) {
                        Log.d(TAG, "Connectivity restored")
                        onConnected()
                    } else {
                        Log.d(TAG, "Connectivity lost")
                        onDisconnected()
                    }
                }
                previousState = isConnected
            }
        }
    }
    
    /**
     * Check if cached data is available and not empty
     */
    suspend fun <T : Collection<*>?> hasCachedData(dataProvider: suspend () -> T): Boolean {
        return try {
            val data = dataProvider()
            data != null && data.isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Error checking cached data", e)
            false
        }
    }
    
    /**
     * Check connectivity state from StateFlow
     */
    suspend fun isCurrentlyConnected(): Boolean {
        return ConnectivityUtils.isConnected.first()
    }
}

/**
 * Custom exception for offline-related errors
 */
class OfflineException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Data class for offline operation results
 */
data class OfflineResult<T>(
    val data: T?,
    val fromCache: Boolean,
    val error: String? = null
) {
    val isSuccess: Boolean = data != null && error == null
    val isFromCache: Boolean = fromCache
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/