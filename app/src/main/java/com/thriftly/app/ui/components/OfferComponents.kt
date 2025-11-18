package com.thriftly.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.thriftly.app.data.entity.Offer
import com.thriftly.app.data.entity.OfferStatus
import com.thriftly.app.ui.theme.ThriftlyTheme

/**
 * Make Offer button component
 * Displays prominently on listing detail screens
 * Encourages price negotiation in marketplace
 */
@Composable
fun MakeOfferButton(
    originalPrice: Double,
    onOfferSubmitted: (Double, String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Button(
        onClick = { showDialog = true },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Icon(
            Icons.Default.LocalOffer,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Make Offer",
            fontWeight = FontWeight.Medium
        )
    }
    
    if (showDialog) {
        MakeOfferDialog(
            originalPrice = originalPrice,
            onDismiss = { showDialog = false },
            onOfferSubmitted = { amount, message ->
                onOfferSubmitted(amount, message)
                showDialog = false
            }
        )
    }
}

/**
 * Dialog for making an offer on an item
 * Simple form with price input and optional message
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MakeOfferDialog(
    originalPrice: Double,
    onDismiss: () -> Unit,
    onOfferSubmitted: (Double, String) -> Unit
) {
    var offerAmount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    // Calculate percentage discount for user feedback
    val offerPercentage = remember(offerAmount) {
        val amount = offerAmount.toDoubleOrNull()
        if (amount != null && amount > 0) {
            ((originalPrice - amount) / originalPrice * 100).toInt()
        } else 0
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Make an Offer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Original price reference
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Listed Price",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "R${String.format("%.0f", originalPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Offer amount input
                OutlinedTextField(
                    value = offerAmount,
                    onValueChange = { offerAmount = it },
                    label = { Text("Your Offer") },
                    placeholder = { Text("Enter amount") },
                    leadingIcon = { Text("R", style = MaterialTheme.typography.bodyLarge) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Show discount percentage if valid offer
                if (offerPercentage > 0) {
                    Text(
                        text = "That's $offerPercentage% off the listed price",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (offerPercentage > 20) {
                            MaterialTheme.colorScheme.error  // High discount warning
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Optional message to seller
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (Optional)") },
                    placeholder = { Text("Add a note to the seller...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val amount = offerAmount.toDoubleOrNull()
                            if (amount != null && amount > 0) {
                                onOfferSubmitted(amount, message.trim())
                            }
                        },
                        enabled = offerAmount.toDoubleOrNull() != null && offerAmount.toDouble() > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Submit Offer")
                    }
                }
            }
        }
    }
}

/**
 * Offer status badge component
 * Shows current status of offers in a clear visual way
 */
@Composable
fun OfferStatusBadge(
    status: OfferStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (status) {
        OfferStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Pending"
        )
        OfferStatus.ACCEPTED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Accepted"
        )
        OfferStatus.DECLINED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Declined"
        )
        OfferStatus.COUNTERED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Countered"
        )
        OfferStatus.EXPIRED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Expired"
        )
        OfferStatus.WITHDRAWN -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Withdrawn"
        )
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Simple offer item card for listing offers
 * Shows offer details in a compact format
 */
@Composable
fun OfferItemCard(
    offer: Offer,
    buyerName: String,  // In real app, fetch from user table
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onCounter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Offer header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Offer from $buyerName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = formatOfferTime(offer.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                OfferStatusBadge(status = offer.status)
            }
            
            // Price comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Original: R${String.format("%.0f", offer.originalPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Offer: R${String.format("%.0f", offer.offerAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Show discount percentage
                val discountPercent = ((offer.originalPrice - offer.offerAmount) / offer.originalPrice * 100).toInt()
                if (discountPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "$discountPercent% off",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Message if provided
            if (offer.message.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = offer.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            // Action buttons (only show for pending offers)
            if (offer.status == OfferStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Decline")
                    }
                    
                    OutlinedButton(
                        onClick = onCounter,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Counter")
                    }
                    
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

/**
 * Format offer timestamp for display
 */
private fun formatOfferTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 3600000 -> "${diff / 60000}m ago"      // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000}h ago"   // Less than 24 hours
        else -> "${diff / 86400000}d ago"             // Days ago
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeOfferButtonPreview() {
    ThriftlyTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MakeOfferButton(
                originalPrice = 250.0,
                onOfferSubmitted = { _, _ -> }
            )
            
            OfferStatusBadge(status = OfferStatus.PENDING)
            OfferStatusBadge(status = OfferStatus.ACCEPTED)
            OfferStatusBadge(status = OfferStatus.DECLINED)
        }
    }
}