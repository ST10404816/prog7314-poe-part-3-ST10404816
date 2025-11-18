package com.thriftly.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * ChatDao - Room database access for chat messages
 * 
 * Provides conversation-specific queries and offline sync support
 */
@Dao
interface ChatDao {
    /**
     * Insert a new message (or replace if ID exists)
     * Used for both sending new messages and caching received messages
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage)

    /**
     * Update existing message (e.g., mark as synced after successful API call)
     */
    @Update
    suspend fun update(message: ChatMessage)

    /**
     * Get all messages for a specific conversation, ordered chronologically
     * Returns a Flow for reactive UI updates
     */
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>

    /**
     * Get all messages across all conversations (for overview/debugging)
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    /**
     * Get pending (unsynced) messages that need to be sent to API
     * Used by SyncWorker to replay offline messages
     */
    @Query("SELECT * FROM chat_messages WHERE isPending = 1 ORDER BY timestamp ASC")
    suspend fun getPendingMessages(): List<ChatMessage>

    /**
     * Get specific message by ID (for retry operations)
     */
    @Query("SELECT * FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ChatMessage?

    /**
     * Delete a specific message
     */
    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Delete all messages in a conversation
     */
    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)
}

/* 
References 

Android Developers. 2025. Room Database Guide. [Online]. Available at: https://developer.android.com/training/data-storage/room [Accessed 17 Nov 2025].
*/
