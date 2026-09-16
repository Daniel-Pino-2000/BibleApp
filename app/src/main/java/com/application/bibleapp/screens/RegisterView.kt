package com.application.bibleapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.application.bibleapp.ui.theme.Spacing
import com.application.bibleapp.viewmodel.AuthViewModel

/** Mirrors the backend's own minimum (server/routes/AuthRoutes.kt) so the user finds out
 *  their password is too short here, instead of only after submitting. */
private const val MIN_PASSWORD_LENGTH = 8

@Composable
fun RegisterView(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val isLoading by authViewModel.isLoading.collectAsState()
    val remoteError by authViewModel.errorMessage.collectAsState()

    val errorToShow = localError ?: remoteError

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text("Create an account", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Your highlights, notes, and reading progress will sync across every " +
                "device you sign in on.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                localError = null
                authViewModel.clearError()
            },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
                authViewModel.clearError()
            },
            label = { Text("Password") },
            supportingText = { Text("At least $MIN_PASSWORD_LENGTH characters") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
                authViewModel.clearError()
            },
            label = { Text("Confirm password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        errorToShow?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                when {
                    password.length < MIN_PASSWORD_LENGTH ->
                        localError = "Password must be at least $MIN_PASSWORD_LENGTH characters"
                    password != confirmPassword ->
                        localError = "Passwords don't match"
                    else ->
                        authViewModel.register(email.trim(), password, onSuccess = onRegisterSuccess)
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create account")
            }
        }

        TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Already have an account? Sign in")
        }
    }
}
