package com.thriftly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.res.stringResource
import com.thriftly.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController) {
    // Local UI state (could be persisted via DataStore later)
    var dark by remember { mutableStateOf(false) }
    var biometrics by remember { mutableStateOf(false) }

    // Basic settings layout with a top app bar
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(12.dp)
        ) {
            // Dark mode toggle (demo only)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.dark_mode))
                Switch(checked = dark, onCheckedChange = { dark = it })
            }

            Spacer(Modifier.height(8.dp))

            // Biometric unlock toggle (demo only)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.biometric_unlock))
                Switch(checked = biometrics, onCheckedChange = { biometrics = it })
            )
        }
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
