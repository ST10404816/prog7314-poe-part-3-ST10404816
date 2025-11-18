package com.thriftly.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OfflineAction::class, ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offlineDao(): OfflineDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thriftly.db"
                )
                    .fallbackToDestructiveMigration() // Allow database recreation on schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/* 
References 

Android Developers. 2025. Room Database Guide. [Online]. Available at: https://developer.android.com/training/data-storage/room [Accessed 17 Nov 2025].
*/
