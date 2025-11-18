package com.thriftly.app.ui.screens

/*
 * LoginScreen
 *
 * Presents classic email/password login plus a small biometric shortcut.
 * The biometric path delegates to a helper that shows the system prompt.
 */
import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thriftly.app.R
import com.thriftly.app.data.auth.AuthStore
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    nav: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Input state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val shape = RoundedCornerShape(16.dp)
    val scrollState = rememberScrollState()
    
    // Error states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    
    // Password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Validation function
    fun isValidEmail(email: String): Boolean {
        return Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
        ).matcher(email).matches()
    }

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(contentPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo + title
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(84.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            // Show general login error if any
            loginError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            
            // Email field
            OutlinedTextField(
                value = email, 
                onValueChange = { 
                    email = it
                    emailError = null
                    loginError = null // Clear login error when user types
                },
                placeholder = { Text(stringResource(R.string.placeholder_email)) },
                singleLine = true, 
                shape = shape, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(10.dp))

            // Password field
            OutlinedTextField(
                value = password, 
                onValueChange = { 
                    password = it
                    passwordError = null
                    loginError = null // Clear login error when user types
                },
                placeholder = { Text(stringResource(R.string.placeholder_password)) },
                singleLine = true, 
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                shape = shape, 
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(16.dp))

            // Login button with validation
            Button(
                onClick = {
                    // Clear previous errors
                    emailError = null
                    passwordError = null
                    loginError = null
                    
                    // Validate fields
                    var hasErrors = false
                    
                    if (email.isBlank()) {
                        emailError = ctx.getString(R.string.error_email_required)
                        hasErrors = true
                    } else if (!isValidEmail(email)) {
                        emailError = ctx.getString(R.string.error_invalid_email)
                        hasErrors = true
                    }
                    
                    if (password.isBlank()) {
                        passwordError = ctx.getString(R.string.error_password_required)
                        hasErrors = true
                    } else if (password.length < 6) {
                        passwordError = ctx.getString(R.string.error_password_too_short)
                        hasErrors = true
                    }
                    
                    if (hasErrors) {
                        return@Button
                    }
                    
                    scope.launch {
                        try {
                            val ok = AuthStore.login(ctx, email, password)
                            if (ok) {
                                val welcomeText = ctx.getString(R.string.toast_welcome_back)
                                Toast.makeText(ctx, welcomeText, Toast.LENGTH_SHORT).show()
                                nav.navigate("home") { popUpTo(0) } // clear back stack
                            } else {
                                loginError = ctx.getString(R.string.error_login_failed)
                            }
                        } catch (e: Exception) {
                            loginError = "Login failed. Please check your connection and try again."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) { Text(stringResource(R.string.btn_login)) }

            Spacer(Modifier.height(16.dp))

            // Biometric login (mock flow)
            TextButton(onClick = {
                biometricLogin(ctx) { success ->
                    if (success) {
                        scope.launch {
                            // Mark biometric pref and go to home (demo only)
                            AuthStore.enableBiometric(ctx, true)
                            Toast.makeText(ctx, "Logged in with fingerprint", Toast.LENGTH_SHORT).show()
                            nav.navigate("home") { popUpTo(0) }
                        }
                    } else {
                        Toast.makeText(ctx, "Fingerprint failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_lock_idle_lock),
                        contentDescription = null
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.login_with_fingerprint), textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Link to signup
            TextButton(onClick = { nav.navigate("signup") }) {
                Text(stringResource(R.string.no_account_sign_up))
            }
        }
    }
}

/** Small helper to show BiometricPrompt and return success/failure (demo use). */
private fun biometricLogin(ctx: Context, onResult: (Boolean) -> Unit) {
    // Check if device supports biometrics/credentials
    val mgr = BiometricManager.from(ctx)
    val can = mgr.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    if (can != BiometricManager.BIOMETRIC_SUCCESS) {
        onResult(false); return
    }

    // Build and show the prompt
    val prompt = BiometricPrompt(
        ctx.findActivity(),
        Executors.newSingleThreadExecutor(),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onResult(true)
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onResult(false)
            override fun onAuthenticationFailed() = onResult(false)
        }
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(ctx.getString(com.thriftly.app.R.string.biometric_title))
        .setSubtitle(ctx.getString(com.thriftly.app.R.string.biometric_subtitle))
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()
    prompt.authenticate(info)
}

/** Find a FragmentActivity from a Context (needed for BiometricPrompt). */
private fun Context.findActivity(): androidx.fragment.app.FragmentActivity {
    var c = this
    while (c is android.content.ContextWrapper) {
        if (c is androidx.fragment.app.FragmentActivity) return c
        c = c.baseContext
    }
    throw IllegalStateException("No FragmentActivity found")
}

/* ------- Previews ------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = "id:pixel_7")
@Composable fun LoginPreviewLight() {
    com.thriftly.app.ui.theme.ThriftlyTheme { LoginScreen(rememberNavController()) }
}

@Preview(
    showBackground = true, backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, device = "id:pixel_7"
)
@Composable fun LoginPreviewDark() {
    com.thriftly.app.ui.theme.ThriftlyTheme { LoginScreen(rememberNavController()) }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
