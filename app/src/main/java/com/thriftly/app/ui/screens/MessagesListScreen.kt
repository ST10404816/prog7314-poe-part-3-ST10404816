package com.thriftly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.thriftly.app.ui.theme.ThriftlyTheme

/**
 * Simple conversation list for marketplace messaging
 * Shows all your active chats with buyers/sellers
 * Essential for managing multiple transactions
 */
data class ConversationPreview(
    val id: String,
    val otherUserName: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isSellerConversation: Boolean = false  // True if you're the seller in this chat
)

@Composable
fun MessagesListScreen(
    nav: NavController,
    contentPadding: PaddingValues = PaddingValues()
) {
    // Mock conversations - in real app, these would come from repository
    val conversations = remember {
        listOf(
            // Sample marketplace conversations
            ConversationPreview(
                id = "conv_1",
                otherUserName = "Sarah M.",
                lastMessage = "When would be a good time to meet?",
                timestamp = System.currentTimeMillis() - 1800000,  // 30 min ago
                unreadCount = 1,
                isSellerConversation = false  // You're the buyer
            ),
            ConversationPreview(
                id = "conv_2", 
                otherUserName = "Mike K.",
                lastMessage = "Thanks! The jacket fits perfectly.",
                timestamp = System.currentTimeMillis() - 7200000,  // 2 hours ago
                unreadCount = 0,
                isSellerConversation = true   // You're the seller
            ),
            ConversationPreview(
                id = "conv_3",
                otherUserName = "Lisa P.",
                lastMessage = "Hi! Is this still available?",
                timestamp = System.currentTimeMillis() - 21600000, // 6 hours ago
                unreadCount = 2,
                isSellerConversation = true
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        // Header section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Message,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (conversations.isEmpty()) {
            // Empty state - no conversations yet
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Start browsing to connect with sellers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // List of conversations
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = {
                            // Navigate to chat screen
                            // nav.navigate("chat/${conversation.id}")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual conversation item showing preview info
 * Displays key details buyers/sellers need to see
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationItem(
    conversation: ConversationPreview,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.otherUserName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
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
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = conversation.otherUserName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Role indicator (buyer/seller)
                    Text(
                        text = if (conversation.isSellerConversation) "Seller" else "Buyer",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (conversation.isSellerConversation) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Last message preview
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Timestamp
                        Text(
                            text = formatConversationTime(conversation.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Unread message indicator
                        if (conversation.unreadCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = conversation.unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format timestamp for conversation list
 * Shows relative time in a user-friendly way
 */
private fun formatConversationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 3600000 -> "${diff / 60000}m"      // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000}h"   // Less than 24 hours
        else -> "${diff / 86400000}d"             // Days ago
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagesListScreenPreview() {
    ThriftlyTheme {
        MessagesListScreen(nav = rememberNavController())
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/