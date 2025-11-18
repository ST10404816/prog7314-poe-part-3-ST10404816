package com.thriftly.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlinx.coroutines.flow.first

private const val DATASTORE_NAME = "thriftly_prefs"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object DataStoreManager {
    private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    // store ISO tags (e.g. "en", "af", "zu") for robustness
    private val LANGUAGE = stringPreferencesKey("language")
    private val NOTIF_OFFERS = booleanPreferencesKey("notif_offers")
    private val NOTIF_MESSAGES = booleanPreferencesKey("notif_messages")
    private val NOTIF_GENERAL = booleanPreferencesKey("notif_general")

    suspend fun setBiometricEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setLanguage(context: Context, langTag: String) {
        // expect a language tag like "en", "af", "zu"
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE] = langTag
        }
    }

    fun languageFlow(context: Context) = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[LANGUAGE] ?: "en" }

    suspend fun getLanguage(context: Context): String = languageFlow(context).first()

    suspend fun setNotifOffers(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[NOTIF_OFFERS] = enabled }
    }

    fun notifOffersFlow(context: Context) = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[NOTIF_OFFERS] ?: true }

    suspend fun setNotifMessages(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[NOTIF_MESSAGES] = enabled }
    }

    fun notifMessagesFlow(context: Context) = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[NOTIF_MESSAGES] ?: true }

    suspend fun setNotifGeneral(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[NOTIF_GENERAL] = enabled }
    }

    fun notifGeneralFlow(context: Context) = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[NOTIF_GENERAL] ?: true }

    fun isBiometricEnabledFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { prefs ->
                prefs[BIOMETRIC_ENABLED] ?: false
            }
    }

    // convenience suspend reader
    suspend fun isBiometricEnabled(context: Context): Boolean = isBiometricEnabledFlow(context).first()
    
    /** Clear all stored data (for account deletion) */
    suspend fun clearAllData(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        
        // Also clear SharedPreferences
        val sharedPrefs = context.getSharedPreferences("thriftly_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
    }
}
