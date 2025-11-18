package com.thriftly.app.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thriftly.app.ChatRepository
import com.thriftly.app.ConnectivityUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.thriftly.app.ChatMessage as DbChatMessage

/**
 * ChatViewModel - Manages chat UI state with offline support
 * 
 * Satisfies POE user-defined feature: "In-app chat with offline support"
 * 
 * Features:
 * - Reactive message list from Room database
 * - Send messages (online/offline)
 * - Periodic polling for new messages from API
 * - Connectivity monitoring
 * - Pending message sync status
 */

// UI model for chat sender
enum class ChatSender { Me, Seller }

// UI model for chat message (maps from Room entity)
data class ChatMessage(
    val id: String,
    val text: String,
    val sender: ChatSender,
    val conversationId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false  // Shows "sending..." indicator in UI
)

class ChatViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        private const val TAG = "ChatViewModel"
        private const val POLL_INTERVAL_MS = 5000L  // Poll every 5 seconds when chat is open
    }
    
    // Observable message list for UI
    private val _messageFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messageFlow: StateFlow<List<ChatMessage>> = _messageFlow.asStateFlow()
    
    // Connection status
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    
    // Currently active conversation (for polling)
    private var activeConversationId: String? = null
    private var pollingJob: Job? = null
    
    init {
        monitorConnectivity()
    }

    /**
     * Load messages for a specific conversation
     * 
     * Called when user opens a conversation
     * Starts observing Room database and polling API for updates
     */
    fun loadConversation(conversationId: String) {
        Log.d(TAG, "Loading conversation: $conversationId")
        activeConversationId = conversationId
        
        // Observe Room database for this conversation
        viewModelScope.launch {
            try {
                ChatRepository.getMessages(conversationId, context)
                    .collect { dbMessages ->
                        // Convert Room entities to UI models
                        _messageFlow.value = dbMessages.map { dbMsg ->
                            ChatMessage(
                                id = dbMsg.id,
                                text = dbMsg.text,
                                sender = if (dbMsg.isMine) ChatSender.Me else ChatSender.Seller,
                                conversationId = dbMsg.conversationId,
                                timestamp = dbMsg.timestamp,
                                isPending = dbMsg.isPending
                            )
                        }
                        Log.d(TAG, "Loaded ${dbMessages.size} messages for conversation $conversationId")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading conversation", e)
                _messageFlow.value = emptyList()
            }
        }
        
        // Fetch from API in background
        viewModelScope.launch {
            try {
                if (ConnectivityUtils.hasActiveConnection(context)) {
                    ChatRepository.fetchMessagesFromApi(conversationId, context)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Background fetch failed", e)
            }
        }
        
        // Start polling for new messages
        startPolling(conversationId)
    }

    /**
     * Send a message in the active conversation
     * 
     * UI behavior:
     * - Message appears immediately with "sending..." if offline
     * - When synced, "sending..." disappears
     */
    fun sendMessageTo(conversationId: String, text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            try {
                ChatRepository.sendMessage(conversationId, text, context)
                Log.d(TAG, "Message queued/sent: $text")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }

    /**
     * Start periodic polling for new messages
     * 
     * Polls API every 5 seconds while conversation is open
     * Stops when user navigates away
     */
    private fun startPolling(conversationId: String) {
        stopPolling()
        
        pollingJob = viewModelScope.launch {
            while (activeConversationId == conversationId) {
                try {
                    if (ConnectivityUtils.hasActiveConnection(context)) {
                        ChatRepository.fetchMessagesFromApi(conversationId, context)
                        Log.d(TAG, "Polled for new messages")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Polling failed", e)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop polling when user leaves conversation
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        activeConversationId = null
    }

    /**
     * Monitor device connectivity
     */
    private fun monitorConnectivity() {
        viewModelScope.launch {
            // Simple connectivity check
            // In production, use ConnectivityManager callbacks
            while (true) {
                _isOffline.value = !ConnectivityUtils.hasActiveConnection(context)
                delay(2000)
            }
        }
    }

    /**
     * Manual refresh (pull-to-refresh)
     */
    fun refresh() {
        activeConversationId?.let { conversationId ->
            viewModelScope.launch {
                try {
                    if (ConnectivityUtils.hasActiveConnection(context)) {
                        ChatRepository.fetchMessagesFromApi(conversationId, context)
                        Log.d(TAG, "Manual refresh completed")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Refresh failed", e)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

/* 
References 

Android Developers. 2025. Kotlin Coroutines and Flow. [Online]. Available at: https://developer.android.com/kotlin/coroutines [Accessed 17 Nov 2025].
*/
