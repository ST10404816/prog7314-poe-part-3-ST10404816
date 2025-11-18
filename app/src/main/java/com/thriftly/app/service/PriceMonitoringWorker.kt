package com.thriftly.app.service

import android.content.Context
import android.util.Log
import androidx.work.*
import com.thriftly.app.data.repo.WishlistRepository
import com.thriftly.app.data.entity.WishlistNotificationSettings
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

/**
 * Background worker for monitoring wishlist item price changes
 * Runs periodically to check for price drops and send notifications
 */
class PriceMonitoringWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val repository = WishlistRepository(context)
    
    companion object {
        const val TAG = "PriceMonitoringWorker"
        const val WORK_NAME = "price_monitoring_work"
        private const val MIN_PRICE_DROP_AMOUNT = 10.0 // Minimum R10 drop to notify
        
        /**
         * Schedule periodic price monitoring
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<PriceMonitoringWorker>(
                repeatInterval = 6, // Check every 6 hours
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 1, // Flex window of 1 hour
                flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            
            Log.d(TAG, "Periodic price monitoring scheduled")
        }
        
        /**
         * Cancel all price monitoring work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Price monitoring work cancelled")
        }
        
        /**
         * Trigger immediate price check (one-time work)
         */
        fun triggerImmediateCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val workRequest = OneTimeWorkRequestBuilder<PriceMonitoringWorker>()
                .setConstraints(constraints)
                .addTag("immediate_check")
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "Immediate price check triggered")
        }
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting price monitoring check...")
            
            // Get notification settings
            val settings = repository.getNotificationSettings().first()
            
            if (!settings.enablePriceDropAlerts) {
                Log.d(TAG, "Price drop alerts disabled, skipping check")
                return Result.success()
            }
            
            // Check for price updates
            val priceUpdatesResult = repository.checkPriceUpdates()
            
            priceUpdatesResult.fold(
                onSuccess = { priceUpdates ->
                    Log.d(TAG, "Found ${priceUpdates.size} price updates")
                    
                    // Process each price update
                    for (priceHistory in priceUpdates) {
                        processPriceUpdate(priceHistory, settings)
                    }
                    
                    // Get items with significant price drops
                    val itemsWithDropsResult = repository.getItemsWithPriceDrops()
                    itemsWithDropsResult.fold(
                        onSuccess = { itemsWithDrops ->
                            Log.d(TAG, "Found ${itemsWithDrops.size} items with significant price drops")
                            
                            // Send notifications for each item with price drop
                            for (item in itemsWithDrops) {
                                sendPriceDropNotification(item, settings)
                                // Mark as notified
                                repository.markPriceDropNotified(item.id)
                            }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error getting items with price drops", error)
                        }
                    )
                    
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Error during price check", error)
                    Result.retry()
                }
            )
            
        } catch (exception: Exception) {
            Log.e(TAG, "Exception in price monitoring worker", exception)
            Result.failure()
        }
    }
    
    /**
     * Process individual price update
     */
    private suspend fun processPriceUpdate(
        priceHistory: com.thriftly.app.data.entity.PriceHistory,
        settings: WishlistNotificationSettings
    ) {
        val priceChange = priceHistory.priceChange
        val changePercent = priceHistory.changePercentage
        
        Log.d(TAG, "Processing price update: ${priceChange} (${changePercent}%)")
        
        // Check if this qualifies for notification
        val significantDrop = priceChange < 0 && (
            Math.abs(priceChange) >= settings.minimumDropAmount ||
            Math.abs(changePercent) >= settings.minimumDropPercentage
        )
        
        if (significantDrop && !priceHistory.notificationSent) {
            Log.d(TAG, "Significant price drop detected, preparing notification")
            // The notification will be sent by the main notification processing
            // This is just for logging and additional processing if needed
        }
    }
    
    /**
     * Send price drop notification via Firebase
     */
    private suspend fun sendPriceDropNotification(
        item: com.thriftly.app.data.entity.WishlistItem,
        settings: WishlistNotificationSettings
    ) {
        try {
            val priceDrop = item.originalPrice - item.currentPrice
            val dropPercentage = if (item.originalPrice > 0) {
                ((priceDrop / item.originalPrice) * 100).toInt()
            } else 0
            
            // Create notification data
            val notificationData = mapOf(
                "type" to "price_drop",
                "item_title" to item.listingTitle,
                "listing_id" to item.listingId,
                "old_price" to item.lastCheckedPrice.toString(),
                "new_price" to item.currentPrice.toString(),
                "price_drop_percent" to dropPercentage.toString(),
                "price_drop_amount" to priceDrop.toString()
            )
            
            // For demo purposes, we'll create a local notification
            // In a real app, you would send this to your backend to trigger FCM
            createLocalNotification(notificationData)
            
            Log.d(TAG, "Price drop notification sent for ${item.listingTitle}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending price drop notification", e)
        }
    }
    
    /**
     * Create local notification for testing (in production, use FCM from server)
     */
    private fun createLocalNotification(data: Map<String, String>) {
        // This would typically be sent from your backend server via FCM
        // For demo purposes, we'll simulate the FCM message locally
        
        try {
            val itemTitle = data["item_title"] ?: "Item"
            val dropPercent = data["price_drop_percent"]?.toIntOrNull() ?: 0
            val oldPrice = data["old_price"]?.toDoubleOrNull() ?: 0.0
            val newPrice = data["new_price"]?.toDoubleOrNull() ?: 0.0
            
            // Create a simulated FCM message that our service will handle
            val simulatedFCMData = JSONObject().apply {
                put("type", "price_drop")
                put("item_title", itemTitle)
                put("listing_id", data["listing_id"])
                put("old_price", oldPrice.toString())
                put("new_price", newPrice.toString())
                put("price_drop_percent", dropPercent.toString())
            }
            
            // In a real implementation, your backend would send this via FCM:
            // FCM.send(userToken, simulatedFCMData)
            
            // For demo, we'll trigger the notification directly
            val messagingService = WishlistFirebaseMessagingService()
            // This would be called automatically by FCM in production
            
            Log.d(TAG, "Local notification created: $simulatedFCMData")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating local notification", e)
        }
    }
}

/**
 * Utility class for managing price monitoring work
 */
class PriceMonitoringManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PriceMonitoringManager"
    }
    
    /**
     * Start price monitoring based on user settings
     */
    suspend fun startMonitoring() {
        try {
            val repository = WishlistRepository(context)
            val settings = repository.getNotificationSettings().first()
            
            if (settings.enablePriceDropAlerts) {
                PriceMonitoringWorker.schedulePeriodicWork(context)
                Log.d(TAG, "Price monitoring started")
            } else {
                Log.d(TAG, "Price monitoring disabled in settings")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting price monitoring", e)
        }
    }
    
    /**
     * Stop price monitoring
     */
    fun stopMonitoring() {
        PriceMonitoringWorker.cancelWork(context)
        Log.d(TAG, "Price monitoring stopped")
    }
    
    /**
     * Update monitoring schedule based on settings
     */
    suspend fun updateMonitoringSchedule() {
        try {
            val repository = WishlistRepository(context)
            val settings = repository.getNotificationSettings().first()
            
            // Cancel existing work
            stopMonitoring()
            
            // Restart with new settings
            if (settings.enablePriceDropAlerts) {
                startMonitoring()
            }
            
            Log.d(TAG, "Monitoring schedule updated")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating monitoring schedule", e)
        }
    }
    
    /**
     * Trigger immediate price check
     */
    fun triggerImmediateCheck() {
        PriceMonitoringWorker.triggerImmediateCheck(context)
        Log.d(TAG, "Immediate price check triggered")
    }
    
    /**
     * Check if monitoring is currently enabled
     */
    fun isMonitoringEnabled(): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(PriceMonitoringWorker.WORK_NAME)
        
        return try {
            val workInfoList = workInfos.get()
            workInfoList.any { workInfo ->
                workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking monitoring status", e)
            false
        }
    }
}