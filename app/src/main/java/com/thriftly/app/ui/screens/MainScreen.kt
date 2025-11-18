package com.thriftly.app.ui.screens

/*
 * MainScreen
 *
 * Hosts the app's navigation graph and bottom navigation. Keep logic here
 * limited to navigation concerns; individual screens own their own UI.
 */
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument

@Composable
fun MainScreen(navController: NavHostController) {
    // Bottom tabs to show in the navigation bar
    val tabs = listOf(
        BottomTab.HOME,
        BottomTab.WISHLIST,
        BottomTab.CREATE,
        BottomTab.CHAT,
        BottomTab.ORDERS,
        BottomTab.PROFILE
    )

    // Observe current destination so we can highlight the active tab
    val backStack by navController.currentBackStackEntryAsState()
    val currentDest = backStack?.destination

    // Decide when the bottom bar should be visible
    val showBottomBar = shouldShowBottomBar(currentDest, tabs)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentDest.isInHierarchy(tab.route)
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.titleRes)
                                )
                            },
                            label = { Text(stringResource(tab.titleRes)) },
                            selected = selected,
                            onClick = {
                                // Navigate to tab root while preserving state
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // App navigation graph
        NavHost(
            navController = navController,
            startDestination = "welcome" // Start on welcome to flow into login/signup
        ) {
            // --- Welcome (opens Login / Signup) ---
            composable("welcome") {
                WelcomeScreen(
                    onLogin  = { navController.navigate("login") },
                    onSignup = { navController.navigate("signup") },
                    onGoogle = {
                        // Start Google SSO; on success you could:
                        // navController.navigate("home") { popUpTo("welcome") { inclusive = true } }
                    }
                )
            }

            // --- Bottom tabs (top-level destinations) ---
            composable(BottomTab.HOME.route) {
                HomeScreen(nav = navController, contentPadding = innerPadding)
            }
            composable(BottomTab.WISHLIST.route) {
                WishlistScreen(nav = navController, contentPadding = innerPadding)
            }
            composable(BottomTab.CREATE.route) {
                CreateListingScreen(nav = navController, contentPadding = innerPadding)
            }
            composable(BottomTab.CHAT.route) {
                ChatScreen(nav = navController, contentPadding = innerPadding)
            }
            
            // Fix: Chat detail route with conversationId argument
            // Navigates from chat list to individual conversation
            composable(
                route = "chat/{conversationId}",
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                // Fix: Safely read conversationId from nav args, show error if missing
                val conversationId = backStackEntry.arguments?.getString("conversationId")
                if (conversationId != null) {
                    ChatDetailScreen(
                        nav = navController,
                        conversationId = conversationId
                    )
                } else {
                    // Fallback: show error and go back
                    androidx.compose.material3.Text("Error: No conversation ID")
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
            
            composable(BottomTab.ORDERS.route) {
                OrdersScreen(contentPadding = innerPadding)
            }
            composable(BottomTab.PROFILE.route) {
                ProfileScreen(nav = navController, contentPadding = innerPadding)
            }

            // Offline test/debug screen for queued actions
            composable("offline") {
                OfflineScreen(onBack = { navController.popBackStack() })
            }

            // --- Auth / non-tab routes ---
            composable("login")  { LoginScreen(nav = navController) }
            composable("signup") { SignUpScreen(nav = navController) }

            // --- Detail screen with argument ---
            composable(
                route = "detail/{listingId}",
                arguments = listOf(navArgument("listingId") { type = NavType.StringType })
            ) { backStackEntry ->
                val listingId = backStackEntry.arguments?.getString("listingId") ?: return@composable
                ListingDetailScreen(
                    nav = navController,
                    listingId = listingId
                )
            }
        }
    }
}

/* ------------------------- helpers & tabs ------------------------- */

// Show the bottom bar only on tab routes; hide on auth/welcome/detail
private fun shouldShowBottomBar(
    destination: NavDestination?,
    tabs: List<BottomTab>
): Boolean {
    if (destination == null) return true
    val hidden = setOf("welcome", "login", "signup", "detail/{listingId}")
    val route = destination.route ?: return true
    if (route in hidden) return false
    val tabRoutes = tabs.map { it.route }.toSet()
    return destination.hierarchy.any { it.route in tabRoutes }
}

// Convenience: check if current destination is within a given route hierarchy
private fun NavDestination?.isInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}

// Bottom tab definitions (title, icons, and unique route)
sealed class BottomTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    object HOME    : BottomTab(com.thriftly.app.R.string.tab_home,     Icons.Filled.Home,         Icons.Outlined.Home,         "home")
    object WISHLIST: BottomTab(com.thriftly.app.R.string.tab_wishlist, Icons.Filled.Favorite,     Icons.Outlined.FavoriteBorder, "wishlist")
    object CREATE  : BottomTab(com.thriftly.app.R.string.tab_sell,     Icons.Filled.Add,          Icons.Outlined.Add,          "create")
    object CHAT    : BottomTab(com.thriftly.app.R.string.tab_chat,     Icons.Filled.Message,      Icons.Outlined.Message,      "chat")
    object ORDERS  : BottomTab(com.thriftly.app.R.string.tab_orders,   Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "orders")
    object PROFILE : BottomTab(com.thriftly.app.R.string.tab_profile,  Icons.Filled.Person,       Icons.Outlined.Person,       "profile")
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
