package com.thriftly.app.ui.screens

/*
 * ProfileScreen
 *
 * Shows user preferences (language, notification toggles) and account controls.
 * Preferences are persisted via DataStore so they survive app restarts.
 */

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thriftly.app.DataStoreManager
import com.thriftly.app.MainActivity
import com.thriftly.app.R
import com.thriftly.app.ui.components.CompactOfflineIndicator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    nav: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    val ctx = LocalContext.current

    // Persisted preferences (language + notification toggles)
    val scope = rememberCoroutineScope()

    // DataStore now stores ISO tags ("en","af","zu")
    val languageState by DataStoreManager.languageFlow(ctx).collectAsState(initial = "en")
    var language by remember { mutableStateOf(languageState) }

    // Update local state when DataStore state changes
    LaunchedEffect(languageState) {
        language = languageState
    }

    val notifOffersState by DataStoreManager.notifOffersFlow(ctx).collectAsState(initial = true)
    var notifOffers by rememberSaveable { mutableStateOf(notifOffersState) }

    val notifMessagesState by DataStoreManager.notifMessagesFlow(ctx).collectAsState(initial = true)
    var notifMessages by rememberSaveable { mutableStateOf(notifMessagesState) }

    val notifGeneralState by DataStoreManager.notifGeneralFlow(ctx).collectAsState(initial = true)
    var notifGeneral by rememberSaveable { mutableStateOf(notifGeneralState) }

    // Biometrics availability + toggle (persisted)
    val deviceHasBiometrics = biometricsAvailableSafe(ctx)
    var useBiometrics by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        useBiometrics = DataStoreManager.isBiometricEnabled(ctx)
    }

    // Dialog state
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Mock user profile state
    var userName by remember { mutableStateOf("User") }
    var userProfilePicUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Replace with real data load later
        userName = "User"
        userProfilePicUrl = ""
    }

    val scroll = rememberScrollState()

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scroll)
                .navigationBarsPadding()
        ) {
            // ===== Header =====
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showEditProfileDialog = true }) {
                    Text(stringResource(R.string.edit_profile))
                }
                Spacer(Modifier.height(8.dp))
                CompactOfflineIndicator()
            }

            Spacer(Modifier.height(20.dp))

            // ===== Preferences =====
            SectionHeader(stringResource(R.string.preferences))

            SettingRow(
                title = stringResource(R.string.order_history),
                showChevron = true
            ) {
                nav.safeNavigateOrToast("orders", ctx)
            }

            var langPickerOpen by remember { mutableStateOf(false) }
            val currentLangRes = when (language) {
                "af" -> R.string.lang_afrikaans
                "zu" -> R.string.lang_zulu
                else -> R.string.lang_english
            }

            SettingRow(
                title = stringResource(R.string.language),
                value = stringResource(currentLangRes),
                showChevron = true
            ) { langPickerOpen = true }

            if (langPickerOpen) {
                LanguagePickerDialog(
                    selectedTag = language,
                    onDismiss = { langPickerOpen = false },
                    onSelectTag = { newTag ->
                        Log.d("ProfileScreen", "Language selected: $newTag")
                        language = newTag
                        try {
                            ctx.getSharedPreferences("thriftly_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putString("language", newTag)
                                .apply()
                        } catch (e: Exception) {
                            Log.w("ProfileScreen", "Failed to write SharedPreferences: ${e.message}")
                        }
                        scope.launch {
                            DataStoreManager.setLanguage(ctx, newTag)
                        }
                        langPickerOpen = false
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ===== Notifications =====
            SectionHeader(stringResource(R.string.preferences)) // reuse label
            SettingSwitchRow(
                title = stringResource(R.string.offers_and_orders),
                checked = notifOffers,
                onCheckedChange = { new ->
                    notifOffers = new
                    scope.launch { DataStoreManager.setNotifOffers(ctx, new) }
                }
            )
            SettingSwitchRow(
                title = stringResource(R.string.messages),
                checked = notifMessages,
                onCheckedChange = { new ->
                    notifMessages = new
                    scope.launch { DataStoreManager.setNotifMessages(ctx, new) }
                }
            )
            SettingSwitchRow(
                title = stringResource(R.string.general_updates),
                checked = notifGeneral,
                onCheckedChange = { new ->
                    notifGeneral = new
                    scope.launch { DataStoreManager.setNotifGeneral(ctx, new) }
                }
            )

            Spacer(Modifier.height(8.dp))

            // ===== Security =====
            SectionHeader(stringResource(R.string.preferences))
            SettingSwitchRow(
                title = stringResource(R.string.biometric_login),
                checked = useBiometrics,
                onCheckedChange = { newValue ->
                    if (newValue && ctx is MainActivity) {
                        ctx.authenticateForRegistration(
                            onSuccess = {
                                scope.launch {
                                    DataStoreManager.setBiometricEnabled(ctx, true)
                                }
                                useBiometrics = true
                            },
                            onFailure = { msg ->
                                Toast.makeText(
                                    ctx,
                                    "Enrollment failed: $msg",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        scope.launch { DataStoreManager.setBiometricEnabled(ctx, false) }
                        useBiometrics = false
                    }
                },
                enabled = deviceHasBiometrics
            )

            SettingRow(
                title = stringResource(R.string.change_password),
                showChevron = true
            ) { nav.safeNavigateOrToast("settings", ctx) }

            SettingRow(
                title = stringResource(R.string.delete_account),
                showChevron = false,
                destructive = true
            ) { showDeleteAccountDialog = true }

            Spacer(Modifier.height(32.dp))
        }

        // ===== Dialogs =====

        if (showDeleteAccountDialog) {
            DeleteAccountDialog(
                onDismiss = { showDeleteAccountDialog = false },
                onConfirm = {
                    scope.launch {
                        DataStoreManager.clearAllData(ctx)
                        nav.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                        Toast.makeText(
                            ctx,
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    showDeleteAccountDialog = false
                }
            )
        }

        if (showEditProfileDialog) {
            EditProfileDialog(
                currentName = userName,
                currentProfilePicUrl = userProfilePicUrl,
                onDismiss = { showEditProfileDialog = false },
                onSave = { newName, newPicUrl ->
                    userName = newName
                    userProfilePicUrl = newPicUrl
                    showEditProfileDialog = false
                    Toast.makeText(ctx, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Small reusable pieces
// -----------------------------------------------------------------------------

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingRow(
    title: String,
    value: String? = null,
    showChevron: Boolean = false,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    val labelStyle =
        if (destructive) MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
        else MaterialTheme.typography.bodyLarge

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = labelStyle)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium)
            if (showChevron) {
                Spacer(Modifier.width(6.dp))
                Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null)
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
    HorizontalDivider()
}

@Composable
private fun LanguagePickerDialog(
    selectedTag: String,
    onDismiss: () -> Unit,
    onSelectTag: (String) -> Unit
) {
    val options = listOf(
        "en" to R.string.lang_english,
        "af" to R.string.lang_afrikaans,
        "zu" to R.string.lang_zulu
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_language)) },
        text = {
            Column {
                options.forEach { (tag, resId) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTag(tag) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTag == tag,
                            onClick = { onSelectTag(tag) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(resId))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.delete_account_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_account_warning),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentProfilePicUrl: String,
    onDismiss: () -> Unit,
    onSave: (newName: String, newPicUrl: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var profilePicUrl by remember { mutableStateOf(currentProfilePicUrl) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { profilePicUrl = it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.full_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Profile Picture",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (profilePicUrl.isNotEmpty()) "Photo selected" else "No photo selected",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Select Photo",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Choose Photo")
                    }
                }

                if (profilePicUrl.isNotEmpty()) {
                    TextButton(
                        onClick = { profilePicUrl = "" },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Remove Photo",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), profilePicUrl.trim())
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/** ---- Previews ---- */

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Profile – Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    device = "id:pixel_7"
)
@Composable
fun ProfileScreenPreviewLight() {
    val nav = rememberNavController()
    ProfileScreen(nav = nav)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Profile – Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    device = "id:pixel_7"
)
@Composable
fun ProfileScreenPreviewDark() {
    val nav = rememberNavController()
    ProfileScreen(nav = nav)
}

/** Quick biometrics availability check that never throws. */
fun biometricsAvailableSafe(context: Context): Boolean {
    return try {
        val mgr = BiometricManager.from(context)
        val res = mgr.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        res == BiometricManager.BIOMETRIC_SUCCESS
    } catch (t: Throwable) {
        Log.w("Profile", "Biometric check failed", t)
        false
    }
}

/** Navigate safely: try route, otherwise show a toast instead of crashing. */
fun NavHostController.safeNavigateOrToast(route: String, ctx: Context) {
    runCatching { navigate(route) }
        .onFailure {
            Log.w("Profile", "Route '$route' not found", it)
            Toast.makeText(ctx, "That screen isn't wired yet (mock)", Toast.LENGTH_SHORT).show()
        }
}

/* 
References 

Google. 2025. Material Design 3 Accessibility Guidelines. [Online]. Available at: https://m3.material.io/foundations/accessible-design [Accessed 17 Nov 2025].
*/
