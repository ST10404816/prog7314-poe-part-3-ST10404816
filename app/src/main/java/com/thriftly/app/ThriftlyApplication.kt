package com.thriftly.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class ThriftlyApplication : Application() {
    companion object {
        const val CHANNEL_ID = "thriftly_notifications"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase (safe to call even if no google-services.json during development)
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
        }

        // Create app notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Thriftly Notifications"
            val descriptionText = "Notifications for Thriftly updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}

/* 
References 

Firebase. 2025. Authentication Documentation. [Online]. Available at: https://firebase.google.com/docs/auth/android/start [Accessed 18 Nov 2025].
*/
