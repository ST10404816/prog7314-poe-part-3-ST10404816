package com.thriftly.app.data.dao

import androidx.room.*
import com.thriftly.app.data.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    // Stream all messages in a conversation thread
    @Query("SELECT * FROM messages WHERE threadId = :thread ORDER BY timestamp ASC")
    fun stream(thread: String): Flow<List<Message>>

    // Insert or replace a message
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: Message)
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/