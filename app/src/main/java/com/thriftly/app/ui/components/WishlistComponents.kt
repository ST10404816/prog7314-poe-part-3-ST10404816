package com.thriftly.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thriftly.app.R
import com.thriftly.app.data.entity.WishlistItem
import com.thriftly.app.data.entity.WishlistNotificationSettings
import java.text.NumberFormat
import java.util.*

/**
 * Individual wishlist item card showing listing details and price changes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistItemCard(
    item: WishlistItem,
    onRemove: () -> Unit,
    onSetAlert: (Double?) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPriceAlertDialog by remember { mutableStateOf(false) }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    
    // Calculate price change
    val priceChange = item.originalPrice - item.currentPrice
    val priceChangePercent = if (item.originalPrice > 0) {
        ((priceChange / item.originalPrice) * 100)
    } else 0.0
    
    val hasPriceDrop = priceChange > 0
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Image and main content
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Item image
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (item.imageUrl != null) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.listingTitle,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = item.listingTitle,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    
                    // Item details
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.listingTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(Modifier.height(4.dp))
                        
                        Text(
                            text = "${item.category} • ${item.condition}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "by ${item.sellerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // Current price
                        Text(
                            text = currencyFormatter.format(item.currentPrice),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (hasPriceDrop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Original price and change indicator
                        if (hasPriceDrop) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currencyFormatter.format(item.originalPrice),
                                    style = MaterialTheme.typography.bodySmall,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Surface(
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "-${priceChangePercent.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Action buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Remove button
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove from wishlist",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Price alert button
                    IconButton(
                        onClick = { showPriceAlertDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (item.targetPrice != null) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Set price alert",
                            modifier = Modifier.size(16.dp),
                            tint = if (item.targetPrice != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Target price indicator
            item.targetPrice?.let { targetPrice ->
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Alert when below ${currencyFormatter.format(targetPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Time since added
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Added ${getTimeAgo(item.addedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Price alert dialog
    if (showPriceAlertDialog) {
        PriceAlertDialog(
            currentPrice = item.currentPrice,
            currentTarget = item.targetPrice,
            onDismiss = { showPriceAlertDialog = false },
            onSetAlert = { targetPrice ->
                onSetAlert(targetPrice)
                showPriceAlertDialog = false
            }
        )
    }
}

/**
 * Dialog for setting price alerts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceAlertDialog(
    currentPrice: Double,
    currentTarget: Double?,
    onDismiss: () -> Unit,
    onSetAlert: (Double?) -> Unit
) {
    var alertPriceText by remember { 
        mutableStateOf(currentTarget?.toString() ?: "") 
    }
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Price Alert") },
        text = {
            Column {
                Text(
                    text = "Get notified when the price drops below your target.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Current price: ${currencyFormatter.format(currentPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = alertPriceText,
                    onValueChange = { alertPriceText = it },
                    label = { Text("Target price") },
                    prefix = { Text("R") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val targetPrice = alertPriceText.toDoubleOrNull()
                    onSetAlert(targetPrice)
                }
            ) {
                Text("Set Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Settings bottom sheet for wishlist notifications
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistSettingsSheet(
    settings: WishlistNotificationSettings,
    onDismiss: () -> Unit,
    onSettingsChanged: (WishlistNotificationSettings) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Wishlist Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Enable price drop alerts
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Price Drop Alerts",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Get notified when prices drop",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = settings.enablePriceDropAlerts,
                            onCheckedChange = { enabled ->
                                onSettingsChanged(settings.copy(enablePriceDropAlerts = enabled))
                            }
                        )
                    }
                    
                    if (settings.enablePriceDropAlerts) {
                        Spacer(Modifier.height(12.dp))
                        
                        // Minimum drop percentage
                        Text(
                            text = "Minimum drop: ${settings.minimumDropPercentage.toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Slider(
                            value = settings.minimumDropPercentage.toFloat(),
                            onValueChange = { value ->
                                onSettingsChanged(settings.copy(minimumDropPercentage = value.toDouble()))
                            },
                            valueRange = 5f..50f,
                            steps = 8
                        )
                        
                        // Minimum drop amount
                        Text(
                            text = "Minimum amount: R${settings.minimumDropAmount.toInt()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Slider(
                            value = settings.minimumDropAmount.toFloat(),
                            onValueChange = { value ->
                                onSettingsChanged(settings.copy(minimumDropAmount = value.toDouble()))
                            },
                            valueRange = 10f..500f,
                            steps = 48
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Target price alerts
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Target Price Alerts",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Alerts for custom price targets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = settings.enableTargetPriceAlerts,
                        onCheckedChange = { enabled ->
                            onSettingsChanged(settings.copy(enableTargetPriceAlerts = enabled))
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Check interval
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Check Frequency",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "How often to check for price changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "Every ${settings.checkIntervalHours} hours",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Slider(
                        value = settings.checkIntervalHours.toFloat(),
                        onValueChange = { value ->
                            onSettingsChanged(settings.copy(checkIntervalHours = value.toInt()))
                        },
                        valueRange = 1f..24f,
                        steps = 22
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Helper function to format time ago
 */
private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000} minutes ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        diff < 604_800_000 -> "${diff / 86_400_000} days ago"
        else -> "${diff / 604_800_000} weeks ago"
    }
}