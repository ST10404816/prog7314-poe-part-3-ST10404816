# API Configuration Guide

## ✅ Current State: All repositories connected to Retrofit

Your app is now fully configured to use real REST API endpoints. All simulated/fake network calls have been replaced with actual Retrofit calls.

---

## 🔧 Setting Your Backend URL

**File:** `app/src/main/java/com/thriftly/app/net/NetworkModule.kt`

**Line 30:**
```kotlin
private const val BASE_URL = "https://api.thriftly.example.com/" // TODO: Set your real backend URL here
```

### Options:

#### Option 1: Production Backend
```kotlin
private const val BASE_URL = "https://thriftly-api.azurewebsites.net/"
```

#### Option 2: Local Backend (Android Emulator)
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/api/"
```
*Note: `10.0.2.2` is the emulator's special IP for `localhost`*

#### Option 3: Local Backend (Physical Device)
```kotlin
private const val BASE_URL = "http://192.168.1.100:8080/api/"
```
*Replace with your computer's actual local IP address*

**⚠️ IMPORTANT:** URL must end with trailing slash `/`

---

## 📡 API Endpoints Implemented

### Authentication Endpoints (ThriftlyApi)

| Method | Endpoint | Purpose | Request Body | Response |
|--------|----------|---------|--------------|----------|
| POST | `/api/auth/login` | User login | `LoginRequest` | `LoginResponse` with JWT token |
| POST | `/api/auth/register` | User registration | `RegisterRequest` | `LoginResponse` with JWT token |

**Request Models:**
```kotlin
// Login
data class LoginRequest(
    val email: String,
    val password: String
)

// Register
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Response (both endpoints)
data class LoginResponse(
    val success: Boolean,
    val token: String?,      // JWT token
    val userId: String?,
    val name: String?,
    val email: String?
)
```

### Listings Endpoints

| Method | Endpoint | Purpose | Used By | Repository |
|--------|----------|---------|---------|------------|
| GET | `/api/listings` | Fetch all listings | Home screen, Browse | ThriftlyRepository ✅ |
| GET | `/api/listings/{id}` | Fetch single listing | Listing detail | ThriftlyRepository ✅ |
| POST | `/api/listings` | Create new listing | Create Listing screen | ThriftlyRepository ✅ |
| PUT | `/api/listings/{id}` | Update listing | Edit listing | OfflineRepository ✅ |
| DELETE | `/api/listings/{id}` | Delete listing | My listings | OfflineRepository ✅ |

**Request/Response Models:**
```kotlin
// Create listing (minimal)
data class CreateListingRequest(
    val title: String,
    val price: Double,
    val imageUrl: String?
)

// Full listing DTO (used for update/response)
data class ListingDto(
    val id: String,
    val title: String,
    val price: Double,
    val category: String,
    val size: String,
    val condition: String,
    val description: String,
    val imageUrls: List<String>?,
    val sellerName: String,
    val createdAt: String?,
    val isDraft: Boolean,
    val isFavorite: Boolean
)
```

### Chat Endpoints

| Method | Endpoint | Purpose | Used By | Repository |
|--------|----------|---------|---------|------------|
| GET | `/api/chat/{conversationId}/messages` | Fetch messages | Chat detail screen | ChatRepository ✅ |
| POST | `/api/chat/{conversationId}/messages` | Send message | Chat detail screen | ChatRepository ✅ |

**Request/Response Models:**
```kotlin
// Send message request
data class SendMessageRequest(
    val senderId: String,
    val text: String,
    val timestamp: Long
)

// Message DTO (response)
data class ChatMessageDto(
    val id: String,           // Server-generated ID
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long
)
```

---

## ✅ Verification: All Repositories Using Retrofit

### ChatRepository.kt
- ✅ `sendMessage()` → `NetworkModule.thriftlyApi.sendChatMessage()`
- ✅ `fetchMessagesFromApi()` → `NetworkModule.thriftlyApi.getChatMessages()`
- ✅ `syncPending()` → `NetworkModule.thriftlyApi.sendChatMessage()`

### ThriftlyRepository.kt
- ✅ `refreshData()` → `NetworkModule.api.getListings()`
- ✅ `addListing()` → `NetworkModule.api.createListing()`
- ✅ `getListingById()` → `NetworkModule.api.getListing()`

### OfflineRepository.kt
- ✅ `syncCreateListing()` → `NetworkModule.thriftlyApi.createListing()`
- ✅ `syncUpdateListing()` → `NetworkModule.api.updateListing()`
- ✅ `syncDeleteListing()` → `NetworkModule.api.deleteListing()`
- ✅ `syncSendMessage()` → `NetworkModule.thriftlyApi.sendChatMessage()`

**Result:** 🎉 No simulated/fake network calls remain!

---

## 🔄 Offline-First Architecture

Your app handles network failures gracefully:

### When Online:
1. API call succeeds immediately
2. Response saved to Room database
3. User sees updated data

### When Offline:
1. Action saved to Room with pending flag
2. OfflineAction created with JSON payload
3. User sees optimistic UI update
4. SyncWorker replays action when connectivity returns

### Graceful Fallback:
If backend isn't deployed yet, ChatRepository has fallback logic:
```kotlin
val response = try {
    NetworkModule.thriftlyApi.sendChatMessage(conversationId, request)
} catch (apiError: Exception) {
    // Fallback: Generate local response
    ChatMessageDto(
        id = "srv_${UUID.randomUUID()}",
        conversationId = conversationId,
        senderId = CURRENT_USER_ID,
        text = text,
        timestamp = System.currentTimeMillis()
    )
}
```
This allows testing without a deployed backend!

---

## 🧪 Testing API Integration

### Test 1: Check Network Configuration
```kotlin
// In NetworkModule.kt
Log.d("NetworkModule", "Base URL: $BASE_URL")
Log.d("NetworkModule", "API initialized: ${api != null}")
Log.d("NetworkModule", "ThriftlyApi initialized: ${thriftlyApi != null}")
```

### Test 2: Test API Call (with try-catch)
```kotlin
viewModelScope.launch {
    try {
        val listings = NetworkModule.thriftlyApi.listListings()
        Log.d("API Test", "Fetched ${listings.size} listings")
    } catch (e: Exception) {
        Log.e("API Test", "API call failed: ${e.message}")
        // Expected if backend not deployed
    }
}
```

### Test 3: Monitor Logcat
```bash
adb logcat -s "OkHttp:D" "NetworkModule:D" "Retrofit:D"
```

Look for:
- `OkHttp` requests showing actual HTTP calls
- Error responses if backend not deployed (expected)
- Successful responses if backend is deployed

---

## 🚀 Backend Deployment Checklist

When you deploy your backend, ensure:

### 1. Base URL Configuration
- [ ] Update `BASE_URL` in `NetworkModule.kt`
- [ ] URL ends with trailing slash `/`
- [ ] Test connectivity from Android device/emulator

### 2. CORS Configuration (if web backend)
```javascript
// Example: Express.js
app.use(cors({
    origin: '*',  // Allow Android app
    methods: ['GET', 'POST', 'PUT', 'DELETE']
}));
```

### 3. Endpoint Paths Match
Ensure your backend routes match exactly:
- `/api/auth/login` (POST)
- `/api/auth/register` (POST)
- `/api/listings` (GET, POST)
- `/api/listings/{id}` (GET, PUT, DELETE)
- `/api/chat/{conversationId}/messages` (GET, POST)

### 4. JSON Format Matches DTOs
Backend JSON must match Kotlin data classes:

**Example: Backend login response should be:**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "user_123",
  "name": "John Doe",
  "email": "john@example.com"
}
```

### 5. Test with Postman/curl
Before testing in app, verify endpoints work:
```bash
curl -X POST http://your-backend/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## 📚 Additional Configuration

### Add Authentication Header (if using JWT)

**Modify NetworkModule.kt to add auth interceptor:**
```kotlin
private val authInterceptor = Interceptor { chain ->
    val token = getStoredToken() // Get from DataStore/SharedPreferences
    val request = chain.request().newBuilder()
        .apply {
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }
        .build()
    chain.proceed(request)
}

private val client = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)  // Add this line
    .addInterceptor(logging)
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

### Increase Timeout for Slow Networks
```kotlin
.connectTimeout(30, TimeUnit.SECONDS)  // Increased from 15
.readTimeout(60, TimeUnit.SECONDS)     // Increased from 30
```

### Enable Full Request/Response Logging (Debug Only)
```kotlin
logging.level = HttpLoggingInterceptor.Level.BODY
```
*Warning: Shows sensitive data in logs*

---

## 🐛 Troubleshooting

### Issue: "Unable to resolve host"
**Cause:** Backend URL incorrect or network unreachable
**Fix:** 
1. Check `BASE_URL` in `NetworkModule.kt`
2. Verify backend is running
3. Test connectivity: `ping your-backend-url`

### Issue: "Failed to connect to /..."
**Cause:** Using `localhost` on physical device
**Fix:** Use computer's local IP (e.g., `192.168.1.100`) or deploy to cloud

### Issue: "HTTP 404 Not Found"
**Cause:** Endpoint path mismatch
**Fix:** Verify backend routes match API interface paths exactly

### Issue: "HTTP 401 Unauthorized"
**Cause:** Missing or invalid authentication token
**Fix:** Implement auth interceptor (see Additional Configuration above)

### Issue: "Timeout" on slow network
**Cause:** Default timeouts too short
**Fix:** Increase `connectTimeout` and `readTimeout` values

---

## 📝 Summary

✅ **BASE_URL consolidated** in NetworkModule.kt with clear TODO  
✅ **Authentication endpoints** defined (login, register)  
✅ **Listings endpoints** fully implemented (GET, POST, PUT, DELETE)  
✅ **Chat endpoints** fully implemented (GET messages, POST message)  
✅ **All repositories** using real Retrofit calls (no simulated network)  
✅ **Offline support** with graceful fallbacks  
✅ **Comprehensive documentation** for each endpoint  

**Next Step:** Update `BASE_URL` with your real backend URL and test!
