package com.thriftly.app.data.repo

import android.content.Context
import android.util.Log
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.data.db.ThriftlyDatabase
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.data.offline.OfflineManager
import com.thriftly.app.data.offline.OfflineException
import com.thriftly.app.net.NetworkModule
import com.thriftly.app.net.toDomain
import com.thriftly.app.net.toDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Repository with comprehensive offline support
 * Automatically falls back to Room database when network is unavailable
 */
class ThriftlyRepository(private val context: Context) {
    private val TAG = "ThriftlyRepository"
    
    // Database access
    private val database = ThriftlyDatabase.get(context)
    private val listingsDao = database.listings()
    
    // Repository scope for background operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // In-memory source of truth (exposed as read-only StateFlow)
    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listingsFlow: StateFlow<List<Listing>> = _listings.asStateFlow()
    
    // Loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    init {
        // Initialize with cached data immediately
        loadCachedData()
        
        // Monitor connectivity changes
        OfflineManager.monitorConnectivity(
            scope = repositoryScope,
            onConnected = {
                Log.d(TAG, "Connection restored - syncing data")
                repositoryScope.launch { syncWithRemote() }
            },
            onDisconnected = {
                Log.d(TAG, "Connection lost - switching to offline mode")
                _isOfflineMode.value = true
            }
        )
    }
    
    /**
     * Load cached data from Room database
     */
    private fun loadCachedData() {
        repositoryScope.launch {
            try {
                listingsDao.streamAll().collect { cachedListings ->
                    _listings.value = cachedListings
                    Log.d(TAG, "Loaded ${cachedListings.size} listings from cache")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cached data", e)
                _error.value = "Error loading cached data"
            }
        }
    }
    
    /**
     * Fetch data with automatic offline fallback
     */
    suspend fun refreshData(showOfflineMessage: Boolean = true): Result<List<Listing>> {
        _isLoading.value = true
        _error.value = null
        
        val result = OfflineManager.fetchWithOfflineSupport(
            context = context,
            remoteDataFetcher = { 
                Log.d(TAG, "Fetching from remote API")
                fetchListingsRemote() 
            },
            localDataFetcher = { 
                Log.d(TAG, "Fetching from local database")
                fetchListingsLocal() 
            },
            cacheUpdater = { listings -> 
                Log.d(TAG, "Caching ${listings.size} listings")
                cacheListings(listings)
                _listings.value = listings
                _isOfflineMode.value = false
            },
            showOfflineMessage = showOfflineMessage
        )
        
        result.fold(
            onSuccess = { listings ->
                _listings.value = listings
                _error.value = null
                Log.d(TAG, "Successfully loaded ${listings.size} listings")
            },
            onFailure = { exception ->
                val errorMessage = when (exception) {
                    is OfflineException -> "No offline data available"
                    else -> "Failed to load listings: ${exception.message}"
                }
                _error.value = errorMessage
                Log.e(TAG, "Error refreshing data", exception)
                _isOfflineMode.value = true
            }
        )
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Sync with remote server when connectivity is restored
     */
    private suspend fun syncWithRemote() {
        try {
            if (ConnectivityUtils.hasActiveConnection(context)) {
                val remoteListings = fetchListingsRemote()
                cacheListings(remoteListings)
                _listings.value = remoteListings
                _isOfflineMode.value = false
                Log.d(TAG, "Sync completed: ${remoteListings.size} listings")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed, staying in offline mode", e)
            _isOfflineMode.value = true
        }
    }
    
    /**
     * Call REST API → map DTOs to domain models → return list
     */
    private suspend fun fetchListingsRemote(): List<Listing> {
        return NetworkModule.api.getListings().map { it.toDomain() }
    }
    
    /**
     * Fetch listings from local Room database
     */
    private suspend fun fetchListingsLocal(): List<Listing> {
        return listingsDao.streamAll().first()
    }
    
    /**
     * Cache listings in Room database
     */
    private suspend fun cacheListings(listings: List<Listing>) {
        try {
            listings.forEach { listing ->
                listingsDao.upsert(listing)
            }
            Log.d(TAG, "Successfully cached ${listings.size} listings")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching listings", e)
        }
    }
    
    /**
     * Add listing with offline support
     */
    suspend fun addListing(listing: Listing): Result<Unit> {
        return OfflineManager.executeWithOfflineFirst(
            context = context,
            operation = {
                // Try to post to remote server first using real API
                Log.d(TAG, "Posting listing to remote server via REST API")
                val response = NetworkModule.api.createListing(listing.toDto())
                Log.d(TAG, "Server response: listing created with ID ${response.id}")
                
                // Cache the server response
                val serverListing = response.toDomain()
                listingsDao.upsert(serverListing)
                
                // Add to local state for immediate UI update
                _listings.update { currentListings -> currentListings + serverListing }
            },
            fallbackAction = {
                // Store locally for offline sync later
                Log.d(TAG, "Storing listing offline for later sync")
                listingsDao.upsert(listing.copy(isDraft = false))
                _listings.update { currentListings -> currentListings + listing }
                
                // Queue offline action for sync when connectivity returns
                val payload = org.json.JSONObject().apply {
                    put("title", listing.title)
                    put("price", listing.price)
                    put("imageUrl", listing.imageUris.firstOrNull() ?: "")
                    put("category", listing.category)
                    put("size", listing.size)
                    put("condition", listing.condition)
                    put("description", listing.description)
                }.toString()
                
                com.thriftly.app.OfflineRepository.addAction(context, "CREATE_LISTING", payload)
            },
            onOfflineMode = {
                OfflineManager.showOfflineNotification(context)
                _isOfflineMode.value = true
            }
        )
    }
    
    /**
     * Get single listing by ID with offline support
     */
    suspend fun getListingById(id: String): Listing? {
        return try {
            // Try cache first for better performance
            val cachedListing = listingsDao.get(id)
            if (cachedListing != null) {
                Log.d(TAG, "Found listing in cache: $id")
                return cachedListing
            }
            
            // If not in cache and online, fetch from remote REST API
            if (ConnectivityUtils.hasActiveConnection(context)) {
                Log.d(TAG, "Fetching listing from remote REST API: $id")
                try {
                    val remoteListing = NetworkModule.api.getListing(id.toLong()).toDomain()
                    listingsDao.upsert(remoteListing)
                    remoteListing
                } catch (apiError: Exception) {
                    Log.w(TAG, "API fetch failed for listing $id: ${apiError.message}")
                    null
                }
            } else {
                Log.d(TAG, "Offline: listing not found in cache: $id")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting listing: $id", e)
            null
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Force refresh from remote (ignoring cache)
     */
    suspend fun forceRefreshFromRemote(): Result<List<Listing>> {
        return if (ConnectivityUtils.hasActiveConnection(context)) {
            try {
                _isLoading.value = true
                val remoteListings = fetchListingsRemote()
                cacheListings(remoteListings)
                _listings.value = remoteListings
                _isOfflineMode.value = false
                Result.success(remoteListings)
            } catch (e: Exception) {
                Log.e(TAG, "Force refresh failed", e)
                _error.value = "Failed to refresh: ${e.message}"
                Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        } else {
            _error.value = "No internet connection"
            Result.failure(OfflineException("No internet connection"))
        }
    }
    
    // ---- Legacy methods for backward compatibility ----
    
    /** Legacy method - replaced by refreshData() */
    suspend fun refreshFromRemote() {
        refreshData()
    }
    
    /** Add one item to the in-memory list (UI updates immediately) */
    suspend fun add(listing: Listing) { 
        addListing(listing)
    }
    
    /** Replace all items (useful for preview/demo seeding) */
    fun seed(items: List<Listing>) { 
        _listings.value = items 
        repositoryScope.launch {
            try {
                items.forEach { listingsDao.upsert(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error seeding data", e)
            }
        }
    }
    
    /**
     * Create a new order from a listing
     */
    fun createOrder(
        listingId: String,
        title: String,
        price: Double,
        seller: String,
        imageRes: Int? = null,
        imageUri: String? = null
    ) {
        com.thriftly.app.data.mock.MockOrders.add(
            title = title,
            price = price,
            seller = seller,
            imageRes = imageRes,
            imageUri = imageUri
        )
        Log.d(TAG, "Order created for listing: $listingId")
    }
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/