package com.thriftly.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thriftly.app.data.dao.ListingDao
import com.thriftly.app.data.dao.MessageDao
import com.thriftly.app.data.dao.UserDao
import com.thriftly.app.data.dao.WishlistDao
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.data.entity.Message
import com.thriftly.app.data.entity.User
import com.thriftly.app.data.entity.WishlistItem
import com.thriftly.app.data.entity.PriceHistory
import com.thriftly.app.data.entity.WishlistNotificationSettings

@Database(
    entities = [
        Listing::class, 
        User::class, 
        Message::class, 
        WishlistItem::class, 
        PriceHistory::class, 
        WishlistNotificationSettings::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ThriftlyDatabase : RoomDatabase() {

    abstract fun listings(): ListingDao
    abstract fun users(): UserDao
    abstract fun messages(): MessageDao
    abstract fun wishlist(): WishlistDao

    companion object {
        @Volatile
        private var INSTANCE: ThriftlyDatabase? = null

        fun get(context: Context): ThriftlyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ThriftlyDatabase::class.java,
                    "thriftly.db"
                )
                .fallbackToDestructiveMigration() // For development
                .build().also { INSTANCE = it }
            }
    }
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/