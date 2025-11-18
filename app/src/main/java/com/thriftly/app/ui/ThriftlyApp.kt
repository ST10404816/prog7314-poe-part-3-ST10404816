package com.thriftly.app.ui

/**
 * ThriftlyApp - Root composable for the Thriftly marketplace application
 * 
 * This is the top-level composable that initializes the navigation controller
 * and sets up the main screen architecture. Following Jetpack Compose best
 * practices, this composable acts as the single source of truth for app-wide
 * navigation state.
 * 
 * Design Decisions:
 * - Uses NavController for type-safe navigation
 * - Implements single-activity architecture
 * - Delegates routing logic to MainScreen
 * - Maintains separation of concerns
 * 
 * @see MainScreen for detailed navigation implementation
 * @see androidx.navigation.compose.NavController
 */

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.thriftly.app.ui.screens.MainScreen

@Composable
fun ThriftlyApp() {
    val nav = rememberNavController()
    MainScreen(navController = nav)
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
