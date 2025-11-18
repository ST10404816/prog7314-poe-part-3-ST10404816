package com.thriftly.app.data.db

import androidx.room.TypeConverter

// Room type converters for storing complex types in the database
class Converters {
    // Convert a list of strings to a single delimited string for storage
    @TypeConverter fun listToString(list: List<String>?): String = list?.joinToString("||") ?: ""
    
    // Convert a delimited string back to a list of strings
    @TypeConverter fun stringToList(s: String?): List<String> = s?.takeIf { it.isNotBlank() }?.split("||") ?: emptyList()
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/