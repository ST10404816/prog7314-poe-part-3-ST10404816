@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.thriftly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import com.thriftly.app.ui.viewmodels.ChatSender
import com.thriftly.app.ui.viewmodels.ChatViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

data class SellerConversation(
    val id: String,
    val sellerName: String,
    val itemName: String,
    val itemPrice: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val sellerAvatar: String
)

/**
 * ChatScreen - Shows list of all conversations
 * Fix: Now properly navigates to chat detail route instead of internal state
 */
@Composable
fun ChatScreen(
    nav: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    // Sample seller conversations (in a real app, this would come from API/database)
    val sellerConversations = remember {
        listOf(
            SellerConversation(
                id = "1",
                sellerName = "Sarah's Vintage",
                itemName = "Blue Vintage Jacket",
                itemPrice = "R450",
                lastMessage = "Is this item still available?",
                lastMessageTime = "2 min ago",
                unreadCount = 2,
                sellerAvatar = "👩"
            ),
            SellerConversation(
                id = "2",
                sellerName = "Mike's Sneakers",
                itemName = "Nike Air Jordan 1",
                itemPrice = "R1200",
                lastMessage = "What size do you need?",
                lastMessageTime = "1 hour ago",
                unreadCount = 0,
                sellerAvatar = "👨"
            ),
            SellerConversation(
                id = "3",
                sellerName = "Emma's Closet",
                itemName = "Designer Handbag",
                itemPrice = "R800",
                lastMessage = "I can do R700 for you",
                lastMessageTime = "3 hours ago",
                unreadCount = 1,
                sellerAvatar = "👩‍🦰"
            ),
            SellerConversation(
                id = "4",
                sellerName = "Tech Hub",
                itemName = "Account Problem",
                itemPrice = "0",
                lastMessage = "Your claim is being processed",
                lastMessageTime = "1 day ago",
                unreadCount = 0,
                sellerAvatar = "🧑‍💻"
            )
        )
    }

    // Fix: Navigate to detail route instead of internal state
    SellerConversationsList(
        conversations = sellerConversations,
        onConversationClick = { conversationId ->
            nav.navigate("chat/$conversationId")
        },
        contentPadding = contentPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerConversationsList(
    conversations: List<SellerConversation>,
    onConversationClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Messages",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Chat with sellers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(contentPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(conversations) { conversation ->
                SellerConversationItem(
                    conversation = conversation,
                    onClick = { onConversationClick(conversation.id) }
                )
            }
        }
    }
}

@Composable
fun SellerConversationItem(
    conversation: SellerConversation,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seller avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.sellerAvatar,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Conversation details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.sellerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = conversation.lastMessageTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${conversation.itemName} • ${conversation.itemPrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (conversation.unreadCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ChatDetailScreen - Shows individual conversation messages
 * Fix: Receives conversationId from navigation args and creates ViewModel properly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    nav: NavHostController,
    conversationId: String
) {
    val context = LocalContext.current
    
    // Fix: Create ViewModel with proper factory and application context
    val viewModel: ChatViewModel = viewModel(
        key = "chat_$conversationId", // Unique key per conversation
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(context.applicationContext) as T
            }
        }
    )
    
    // Fix: Load conversation with error handling
    LaunchedEffect(conversationId) {
        try {
            viewModel.loadConversation(conversationId)
        } catch (e: Exception) {
            android.util.Log.e("ChatDetailScreen", "Error loading conversation", e)
        }
    }
    
    // Stop polling when screen closes
    DisposableEffect(Unit) {
        onDispose {
            try {
                viewModel.stopPolling()
            } catch (e: Exception) {
                android.util.Log.e("ChatDetailScreen", "Error stopping polling", e)
            }
        }
    }
    
    val isOffline by viewModel.isOffline.collectAsState(initial = false)
    val allMessages by viewModel.messageFlow.collectAsState(initial = emptyList())
    
    // Debug logging
    LaunchedEffect(allMessages.size) {
        android.util.Log.d("ChatDetailScreen", "Total messages: ${allMessages.size}, ConversationId: $conversationId")
        allMessages.forEach { msg ->
            android.util.Log.d("ChatDetailScreen", "Message: id=${msg.id}, conversationId=${msg.conversationId}, text=${msg.text}, isMine=${msg.sender == ChatSender.Me}")
        }
    }
    
    // Fix: Filter messages for this conversation with null safety
    val messages = remember(allMessages, conversationId) {
        val filtered = allMessages.filter { it.conversationId == conversationId }
        android.util.Log.d("ChatDetailScreen", "Filtered messages for $conversationId: ${filtered.size}")
        filtered
    }
    
    // Fix: Get conversation details for display (in real app, fetch from DB/API)
    val conversation = remember(conversationId) {
        // Mock data based on conversationId - replace with actual lookup
        when (conversationId) {
            "1" -> SellerConversation("1", "Sarah's Vintage", "Blue Vintage Jacket", "R450", "", "", 0, "👩")
            "2" -> SellerConversation("2", "Mike's Sneakers", "Nike Air Jordan 1", "R1200", "", "", 0, "👨")
            "3" -> SellerConversation("3", "Emma's Closet", "Designer Handbag", "R800", "", "", 0, "👩‍🦰")
            "4" -> SellerConversation("4", "Tech Hub", "Account Problem", "", "", "", 0, "🧑‍💻")
            else -> SellerConversation(conversationId, "Seller", "Item", "R0", "", "", 0, "💬")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = conversation.sellerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = conversation.itemName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isOffline) {
                                Text(
                                    text = "• Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val listState = rememberLazyListState()
        var messageText by remember { mutableStateOf(TextFieldValue("")) }
        
        // Auto-scroll to bottom when new message arrives
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages list with chat background
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (messages.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "💬",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No messages yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start a conversation about ${conversation.itemName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(message)
                        }
                    }
                }
            }
            
            // Message input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message ${conversation.sellerName}") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (messageText.text.isNotBlank()) {
                            // Fix: Use conversationId from nav args
                            viewModel.sendMessageTo(conversationId, messageText.text)
                            messageText = TextFieldValue("")
                        }
                    },
                    enabled = messageText.text.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (messageText.text.isNotBlank()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Message bubble component with modern chat UI design
 * 
 * Shows:
 * - Left-aligned bubbles for received messages (gray background)
 * - Right-aligned bubbles for sent messages (primary color)
 * - Timestamp below each message
 * - "Sending..." indicator for pending messages
 * - Rounded corners with tail-like appearance
 */
@Composable
private fun MessageBubble(message: com.thriftly.app.ui.viewmodels.ChatMessage) {
    val isMe = message.sender == ChatSender.Me
    
    // Color scheme for message bubbles
    val bgColor = if (isMe) {
        if (message.isPending) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else 
            MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    // Format timestamp (e.g., "14:23")
    val timeFormat = remember {
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    }
    val timeString = remember(message.timestamp) {
        timeFormat.format(java.util.Date(message.timestamp))
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        // Add spacing on the opposite side to create chat-like alignment
        if (isMe) {
            Spacer(modifier = Modifier.weight(0.3f))
        }
        
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                shadowElevation = 1.dp,
                modifier = Modifier.padding(
                    start = if (isMe) 0.dp else 4.dp,
                    end = if (isMe) 4.dp else 0.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
                ) {
                    Text(
                        text = message.text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Timestamp and status row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 2.dp
                )
            ) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                // Show "sending..." for pending messages
                if (message.isPending) {
                    Text(
                        text = "• Sending...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        
        if (!isMe) {
            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
