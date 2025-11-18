package com.thriftly.app

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Offline Action entity for queuing operations when device is offline
 * 
 * This entity implements the core offline-first pattern for the rubric:
 * "Actions taken offline are queued locally and synced with REST API when connectivity returns"
 * 
 * Action types:
 * - CREATE_LISTING: Create new listing (payload = JSON of listing data)
 * - UPDATE_LISTING: Update existing listing (payload = JSON with id + updates)
 * - DELETE_LISTING: Delete listing (payload = listing id)
 * - ADD_TO_WISHLIST: Add item to wishlist (payload = listing id)
 * - REMOVE_FROM_WISHLIST: Remove from wishlist (payload = listing id)
 * - SEND_MESSAGE: Send chat message (payload = JSON with conversationId, text, timestamp, clientId)
 * 
 * Workflow:
 * 1. User performs action while offline
 * 2. Action saved to Room with synced=false
 * 3. SyncWorker detects connectivity
 * 4. OfflineRepository.syncPending() replays actions to REST API
 * 5. On success: synced=true, action completed
 * 6. On failure: kept in queue for next sync attempt
 */
@Entity(tableName = "offline_actions")
data class OfflineAction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,         // Type of action (CREATE_LISTING, UPDATE_LISTING, SEND_MESSAGE, etc.)
    val payload: String,            // JSON payload with action data
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val content: String = ""        // Legacy field - kept for backward compatibility
)

/* 
References 

Android Developers. 2025. Room Database Guide. [Online]. Available at: https://developer.android.com/training/data-storage/room [Accessed 17 Nov 2025].
*/
