package com.thriftly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.R
import kotlinx.coroutines.launch

/**
 * Reusable offline indicator component
 * Shows connection status and provides retry functionality
 */
@Composable
fun OfflineIndicator(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    showWhenOnline: Boolean = false,
    alwaysShow: Boolean = false
) {
    val context = LocalContext.current
    val isConnected by ConnectivityUtils.isConnected.collectAsState()
    
    AnimatedVisibility(
        visible = alwaysShow || !isConnected || (showWhenOnline && isConnected),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isConnected) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else 
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (isConnected) 
                            "Online" 
                        else 
                            context.getString(R.string.offline_mode_message),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isConnected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (!isConnected && onRetry != null) {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = context.getString(R.string.retry),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact offline status indicator for app bars
 */
@Composable
fun CompactOfflineIndicator(
    modifier: Modifier = Modifier
) {
    val isConnected by ConnectivityUtils.isConnected.collectAsState()
    
    AnimatedVisibility(
        visible = !isConnected,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Offline banner that slides down from the top
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isConnected by ConnectivityUtils.isConnected.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            isVisible = true
        } else {
            isVisible = false
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = context.getString(R.string.offline_mode_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (onDismiss != null) {
                    TextButton(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        }
                    ) {
                        Text(
                            "Dismiss",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Connection status chip
 */
@Composable
fun ConnectionStatusChip(
    modifier: Modifier = Modifier
) {
    val isConnected by ConnectivityUtils.isConnected.collectAsState()
    
    FilterChip(
        selected = isConnected,
        onClick = { },
        label = {
            Text(
                text = if (isConnected) "Online" else "Offline",
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer,
            labelColor = if (isConnected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onErrorContainer,
            iconColor = if (isConnected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = modifier
    )
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/