package com.thriftly.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset   // required for indicator position
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf                  // lightweight Int state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thriftly.app.R
import com.thriftly.app.data.mock.MockOrders
import com.thriftly.app.data.mock.Order
import com.thriftly.app.data.mock.OrderStatus
import com.thriftly.app.ui.theme.ThriftlyTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import com.thriftly.app.ui.animations.bounceClick
import com.thriftly.app.ui.components.ProductImage

@Composable
fun OrdersScreen(contentPadding: PaddingValues = PaddingValues()) {
    var tab by remember { mutableIntStateOf(0) } // 0=All, 1=Active, 2=Completed
    val all = MockOrders.orders                   // simple in-memory source

    // Filter list based on selected tab
    val filtered = remember(tab, all.size) {
        when (tab) {
            1 -> all.filter { it.status == OrderStatus.Processing || it.status == OrderStatus.Shipped }
            2 -> all.filter { it.status == OrderStatus.Delivered || it.status == OrderStatus.Completed }
            else -> all.toList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header with logo + title
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = stringResource(R.string.logo_desc),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 4.dp),
                    contentScale = ContentScale.Fit
                )
                Text(stringResource(R.string.orders_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Tabs (All / Active / Completed) with a subtle indicator
        TabRow(
            selectedTabIndex = tab,
            containerColor = MaterialTheme.colorScheme.background,
            indicator = { positions -> SecondaryIndicator(Modifier.tabIndicatorOffset(positions[tab])) }
        ) {
            listOf(
                stringResource(R.string.tab_all),
                stringResource(R.string.tab_active),
                stringResource(R.string.tab_completed)
            ).forEachIndexed { i, label ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
            }
        }

        Spacer(Modifier.height(12.dp))

        // Empty state vs list
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_orders_yet), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { order ->
                    OrderCard(order = order, onAction = ::handleOrderAction)
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onAction: (Order) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = { /* Handle order click */ })
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Left: product thumbnail (resource, URI, or placeholder)
                val thumbModifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))

                // Enhanced product image with shimmer loading
                ProductImage(
                    imageUrl = order.imageUri,
                    imageRes = order.imageRes,
                    title = order.title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    aspectRatio = 1f
                )

                Spacer(Modifier.width(10.dp))

                // Title + order number/date
                Column(Modifier.weight(1f)) {
                    Text(
                        order.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(order.orderNo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Right: status badge
                StatusBadge(order.status)
            }

            Spacer(Modifier.height(8.dp))

            // Price + action button (advances status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("R${order.price}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)

                val buttonText = when (order.status) {
                    OrderStatus.Processing -> stringResource(R.string.mark_as_shipped)
                    OrderStatus.Shipped    -> stringResource(R.string.confirm_delivery)
                    OrderStatus.Delivered  -> stringResource(R.string.complete)
                    OrderStatus.Completed  -> stringResource(R.string.completed)
                }

                Button(
                    onClick = { onAction(order) },
                    enabled = order.status != OrderStatus.Completed,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) { Text(buttonText) }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: OrderStatus) {
    // Compute colors & label from status
    val (bg, fg, text) = when (status) {
        OrderStatus.Processing -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(R.string.status_processing)
        )
        OrderStatus.Shipped -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            stringResource(R.string.status_shipped)
        )
        OrderStatus.Delivered -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            stringResource(R.string.status_delivered)
        )
        OrderStatus.Completed -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            stringResource(R.string.status_completed)
        )
    }

    // Small rounded pill with status text
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(10.dp)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// Advance an order's status (mock: update in-place on the list)
private fun handleOrderAction(order: Order) {
    val list = MockOrders.orders
    val idx = list.indexOfFirst { it.id == order.id }
    if (idx == -1) return

    val next = when (order.status) {
        OrderStatus.Processing -> OrderStatus.Shipped
        OrderStatus.Shipped    -> OrderStatus.Delivered
        OrderStatus.Delivered  -> OrderStatus.Completed
        OrderStatus.Completed  -> OrderStatus.Completed
    }
    list[idx] = list[idx].copy(status = next)
}

/* ──────────────── Previews ──────────────── */

@Preview(name = "Orders – Light", showBackground = true, backgroundColor = 0xFFFFFFFF, device = "id:pixel_7")
@Composable
private fun OrdersPreviewLight() {
    ThriftlyTheme {
        if (MockOrders.orders.isEmpty()) {
            // Seed a couple of sample orders for preview
            MockOrders.add("Green T-shirt", 200.0, OrderStatus.Delivered, imageRes = android.R.drawable.ic_menu_gallery)
            MockOrders.add("White Sneakers", 500.0, OrderStatus.Shipped,   imageRes = android.R.drawable.ic_menu_camera)
        }
        OrdersScreen()
    }
}

@Preview(
    name = "Orders – Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    device = "id:pixel_7"
)
@Composable
private fun OrdersPreviewDark() {
    ThriftlyTheme {
        OrdersScreen()
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
