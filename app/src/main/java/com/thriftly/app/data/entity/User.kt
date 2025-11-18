package com.thriftly.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents a user account in the app
@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val displayName: String,
    val email: String,
    val bio: String = ""
)

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/