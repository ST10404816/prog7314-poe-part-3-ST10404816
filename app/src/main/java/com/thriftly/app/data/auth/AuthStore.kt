package com.thriftly.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Create a Preferences DataStore named "auth"
private val Context.dataStore by preferencesDataStore(name = "auth")

// Simple auth/session storage using DataStore
object AuthStore {
    // Keys used to save values in DataStore
    private val KEY_NAME = stringPreferencesKey("name")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_PASSWORD = stringPreferencesKey("password")
    private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
    private val KEY_BIOMETRIC = booleanPreferencesKey("biometric_enabled")

    // Small user model returned by flows
    data class User(val name: String, val email: String)

    // Stream current user (null if missing)
    fun userFlow(ctx: Context): Flow<User?> =
        ctx.dataStore.data.map { p ->
            val n = p[KEY_NAME]; val e = p[KEY_EMAIL]
            if (n.isNullOrBlank() || e.isNullOrBlank()) null else User(n, e)
        }

    // Stream logged-in state (true/false)
    fun isLoggedInFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[KEY_LOGGED_IN] == true }

    // Register a new user and mark as logged in
    suspend fun register(ctx: Context, name: String, email: String, pass: String) {
        ctx.dataStore.edit {
            it[KEY_NAME] = name.trim()
            it[KEY_EMAIL] = email.trim()
            it[KEY_PASSWORD] = pass
            it[KEY_LOGGED_IN] = true
        }
    }

    // Check email+password and set logged-in flag
    suspend fun login(ctx: Context, email: String, pass: String): Boolean {
        val ok = ctx.dataStore.data.map { p ->
            p[KEY_EMAIL] == email.trim() && p[KEY_PASSWORD] == pass
        }.first()
        if (ok) ctx.dataStore.edit { it[KEY_LOGGED_IN] = true }
        return ok
    }

    // Enable/disable biometric preference
    suspend fun enableBiometric(ctx: Context, enabled: Boolean) {
        ctx.dataStore.edit { it[KEY_BIOMETRIC] = enabled }
    }

    // Stream biometric enabled flag
    fun biometricEnabledFlow(ctx: Context): Flow<Boolean> =
        ctx.dataStore.data.map { it[KEY_BIOMETRIC] == true }

    // Log out
    suspend fun logout(ctx: Context) {
        ctx.dataStore.edit { it[KEY_LOGGED_IN] = false }
    }

    // --- Helpers ---

    /** Save user details and mark as logged in (used by SSO). */
    suspend fun setUser(ctx: Context, name: String, email: String) {
        ctx.dataStore.edit {
            it[KEY_NAME] = name.trim()
            it[KEY_EMAIL] = email.trim()
            it[KEY_LOGGED_IN] = true
        }
    }

    /** Convenience wrapper for Google sign-in success. */
    suspend fun googleLogin(ctx: Context, name: String, email: String) {
        setUser(ctx, name, email)
    }
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/