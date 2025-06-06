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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(auth: FirebaseAuth, onProfileCreated: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore: FirebaseFirestore = Firebase.firestore

    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Your Profile", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter your username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (username.isEmpty()) {
                    Toast.makeText(context, "Please enter a username.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        val userProfile = hashMapOf(
                            "username" to username,
                            "email" to auth.currentUser?.email, // Opcional: guardar también el email
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Marca de tiempo
                        )
                        // Guardar el perfil en una colección 'user_profiles'
                        // Usamos el UID del usuario como ID del documento
                        firestore.collection("user_profiles")
                            .document(userId)
                            .set(userProfile) // Usamos .set() para crear/sobrescribir un documento con un ID específico
                            .await()

                        Toast.makeText(context, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                        onProfileCreated() // Notificar al grafo de navegación
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error creating profile: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Save Profile")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}