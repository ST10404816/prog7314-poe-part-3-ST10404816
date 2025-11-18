package com.thriftly.app

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ChatMessage - Room entity for storing chat messages with offline support
 * 
 * Satisfies POE user-defined feature: "In-app chat with offline support"
 * 
 * Fields:
 * - id: Unique message identifier (generated client-side or from server)
 * - conversationId: Groups messages between two users (e.g., "user1_user2")
 * - senderId: Who sent this message (e.g., "current_user" or "seller_123")
 * - text: Message content
 * - timestamp: When message was created
 * - isMine: true if current user sent it, false if received
 * - isPending: true if not yet synced to server (offline mode)
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val conversationId: String,           // e.g., "conv_1", "user_current_seller_123"
    val senderId: String,                 // e.g., "current_user", "seller_123"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = true,          // true = sent by me, false = received
    val isPending: Boolean = false        // true = not yet synced to server
)

/* 
References 

Android Developers. 2025. Room Database Guide. [Online]. Available at: https://developer.android.com/training/data-storage/room [Accessed 17 Nov 2025].
*/
