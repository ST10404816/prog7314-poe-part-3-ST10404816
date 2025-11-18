package com.thriftly.app.net

import retrofit2.http.*

/**
 * Request/response models for REST API communication
 * Match these to your backend JSON schema
 */

/** Login request body */
data class LoginRequest(
    val email: String,
    val password: String
)

/** Registration request body */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/** Authentication response (login/register) */
data class LoginResponse(
    val success: Boolean,
    val token: String?,     // JWT token for authenticated requests (store securely)
    val userId: String?,    // User ID from backend
    val name: String?,      // User's display name
    val email: String?      // User's email
)

/** Minimal create request (expand as needed) */
data class CreateListingRequest(
    val title: String,
    val price: Double,
    val imageUrl: String?
)

/**
 * Chat message DTOs for API communication
 */
data class ChatMessageDto(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long
)

data class SendMessageRequest(
    val senderId: String,
    val text: String,
    val timestamp: Long
)

/**
 * Retrofit API interface for Thriftly backend
 * 
 * All endpoints use suspend functions for coroutine-based async calls
 * Automatically serialized/deserialized with Moshi JSON converter
 * 
 * API Categories:
 * - Authentication: login, register
 * - Listings: fetch, create
 * - Chat: send/receive messages
 */
interface ThriftlyApi {

    // ========================================
    // Authentication Endpoints
    // ========================================
    
    /**
     * POST /api/auth/login
     * 
     * Authenticates user with email/password
     * Returns JWT token on success for authenticated requests
     * 
     * @param body LoginRequest with email and password
     * @return LoginResponse with token, userId, name, email
     */
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
    
    /**
     * POST /api/auth/register
     * 
     * Creates new user account
     * Returns JWT token on success (auto-login after registration)
     * 
     * @param body RegisterRequest with name, email, password
     * @return LoginResponse with token, userId, name, email
     */
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): LoginResponse

    // ========================================
    // Listings Endpoints
    // ========================================
    
    /**
     * GET /api/listings
     * 
     * Fetches all listings from backend database
     * Used for: Home screen, Browse screen, Search results
     * 
     * @return List of ListingDto objects
     */
    @GET("api/listings")
    suspend fun listListings(): List<ListingDto>

    /**
     * POST /api/listings
     * 
     * Creates new listing in backend database
     * Backend generates ID and returns complete listing
     * 
     * Used for: Create Listing screen
     * Offline support: Queued as OfflineAction when offline
     * 
     * @param body CreateListingRequest with title, price, imageUrl
     * @return ListingDto with server-generated ID
     */
    @POST("api/listings")
    suspend fun createListing(@Body body: CreateListingRequest): ListingDto

    // ========================================
    // Chat Endpoints
    // ========================================
    
    /**
     * GET /api/chat/{conversationId}/messages
     * 
     * Fetches all messages for a specific conversation from backend database
     * Backend returns messages ordered by timestamp (oldest first)
     * 
     * Called when:
     * - User opens a conversation
     * - Periodic refresh (polling)
     * - Push notification received
     * 
     * Satisfies: "User can receive messages from seller/support"
     * 
     * @param conversationId Unique conversation identifier (e.g., "1", "user_123_seller_456")
     * @return List of ChatMessageDto ordered by timestamp
     */
    @GET("api/chat/{conversationId}/messages")
    suspend fun getChatMessages(
        @Path("conversationId") conversationId: String
    ): List<ChatMessageDto>

    /**
     * POST /api/chat/{conversationId}/messages
     * 
     * Sends a new message to backend database
     * Backend validates, saves to database, and returns message with server-generated ID
     * 
     * OFFLINE SUPPORT:
     * - When online: Called immediately, returns server ID
     * - When offline: Message queued as OfflineAction, synced later by SyncWorker
     * - Client ID replaced with server ID after successful sync
     * 
     * Satisfies: "User can send messages with offline support and sync"
     * 
     * @param conversationId Unique conversation identifier
     * @param message SendMessageRequest with senderId, text, timestamp
     * @return ChatMessageDto with server-generated ID and timestamp
     */
    @POST("api/chat/{conversationId}/messages")
    suspend fun sendChatMessage(
        @Path("conversationId") conversationId: String,
        @Body message: SendMessageRequest
    ): ChatMessageDto
}

/* 
References 

Android Developers. 2025. Android Architecture Guidelines. [Online]. Available at: https://developer.android.com/topic/architecture [Accessed 17 Nov 2025].
*/
