package com.thriftly.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// Represents a chat message between users
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val threadId: String,
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/