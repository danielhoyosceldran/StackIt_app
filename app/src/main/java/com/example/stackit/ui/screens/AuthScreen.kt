package com.example.stackit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Import to use .await()

@OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental Material3 APIs
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
){
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Coroutine scope for async operations
    val auth: FirebaseAuth = Firebase.auth // Get Firebase Auth instance

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // State for loading indicator

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Authentication", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            // visualTransformation = PasswordVisualTransformation() // Uncomment to hide password
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true // Show loading
                scope.launch {
                    try {
                        auth.createUserWithEmailAndPassword(email, password).await() // Await the async task
                        Toast.makeText(context, "Registration successful.", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false // Hide loading
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Disable button while loading
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true // Show loading
                scope.launch {
                    try {
                        auth.signInWithEmailAndPassword(email, password).await() // Await the async task
                        Toast.makeText(context, "Login successful.", Toast.LENGTH_SHORT).show()
                        onLoginSuccess() // Notify parent of successful login
                    } catch (e: Exception) {
                        Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false // Hide loading
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Disable button while loading
        ) {
            Text("Login")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator() // Loading indicator
        }
    }
}