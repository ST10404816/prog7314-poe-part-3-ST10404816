package com.thriftly.app.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.text.KeyboardOptions
import java.util.regex.Pattern
import com.thriftly.app.R
import com.thriftly.app.data.auth.AuthStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    nav: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(16.dp)
    val scrollState = rememberScrollState()

    // Form fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    
    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Validation functions
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
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }
    
    fun validateFields() {
        nameError = when {
            name.isBlank() -> ctx.getString(R.string.error_name_required)
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
        
        emailError = when {
            email.isBlank() -> ctx.getString(R.string.error_email_required)
            !isValidEmail(email) -> ctx.getString(R.string.error_invalid_email)
            else -> null
        }
        
        passwordError = when {
            pass.isBlank() -> ctx.getString(R.string.error_password_required)
            pass.length < 6 -> ctx.getString(R.string.error_password_too_short)
            !isValidPassword(pass) -> ctx.getString(R.string.error_password_requirements)
            else -> null
        }
        
        confirmPasswordError = when {
            pass2.isBlank() -> "Please confirm your password"
            pass != pass2 -> ctx.getString(R.string.error_passwords_dont_match)
            else -> null
        }
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
            // Logo + headings
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.app_name_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(stringResource(R.string.signup_title), style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(16.dp))

            // Name input
            OutlinedTextField(
                value = name, 
                onValueChange = { 
                    name = it
                    nameError = null // Clear error on input
                },
                placeholder = { Text(stringResource(R.string.placeholder_full_name)) },
                singleLine = true, 
                shape = shape, 
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            Spacer(Modifier.height(8.dp))
            
            // Email input
            OutlinedTextField(
                value = email, 
                onValueChange = { 
                    email = it
                    emailError = null // Clear error on input
                },
                placeholder = { Text(stringResource(R.string.placeholder_email)) },
                singleLine = true, 
                shape = shape, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                    ?: { Text(stringResource(R.string.helper_email_format), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )
            Spacer(Modifier.height(8.dp))
            
            // Password input
            OutlinedTextField(
                value = pass, 
                onValueChange = { 
                    pass = it
                    passwordError = null // Clear error on input
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
                    ?: { Text(stringResource(R.string.helper_password_requirements), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )
            Spacer(Modifier.height(8.dp))
            
            // Confirm password input
            OutlinedTextField(
                value = pass2, 
                onValueChange = { 
                    pass2 = it
                    confirmPasswordError = null // Clear error on input
                },
                placeholder = { Text(stringResource(R.string.placeholder_confirm_password)) },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                shape = shape, 
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )

            Spacer(Modifier.height(16.dp))

            // Submit button with comprehensive validation
            Button(
                onClick = {
                    validateFields()
                    
                    val hasErrors = nameError != null || emailError != null || passwordError != null || confirmPasswordError != null
                    
                    if (hasErrors) {
                        Toast.makeText(ctx, "Please fix the errors above", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    scope.launch {
                        try {
                            // Mock storage using DataStore (see AuthStore)
                            AuthStore.register(ctx, name, email, pass)
                            val welcomeText = String.format(ctx.getString(R.string.welcome_name), name)
                            Toast.makeText(ctx, welcomeText, Toast.LENGTH_SHORT).show()
                            // Go to home and clear back stack so back won't return to signup
                            nav.navigate("home") { popUpTo(0) }
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(stringResource(R.string.signup_title)) }

            Spacer(Modifier.height(8.dp))

            // Link to login screen
            TextButton(onClick = { nav.navigate("login") }) {
                Text(stringResource(R.string.already_have_account))
            }
        }
    }
}

/* ------- Previews ------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = "id:pixel_7")
@Composable
fun SignUpPreviewLight() {
    com.thriftly.app.ui.theme.ThriftlyTheme { SignUpScreen(rememberNavController()) }
}

@Preview(
    showBackground = true, backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, device = "id:pixel_7"
)
@Composable
fun SignUpPreviewDark() {
    com.thriftly.app.ui.theme.ThriftlyTheme { SignUpScreen(rememberNavController()) }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
