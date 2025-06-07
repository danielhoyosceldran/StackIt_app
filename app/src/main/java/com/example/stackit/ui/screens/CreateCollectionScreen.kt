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
fun CreateCollectionScreen(auth: FirebaseAuth, onCollectionCreated: () -> Unit) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firestore: FirebaseFirestore = Firebase.firestore

    var currentUserName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(key1 = Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Intentar recuperar el username del perfil del usuario logueado desde Firestore
            try {
                val profileDoc = firestore.collection("user_profiles").document(userId).get().await()
                currentUserName = profileDoc.getString("username") ?: currentUser.email ?: "Unknown User"
            } catch (e: Exception) {
                // Manejar errores al recuperar el username
                Toast.makeText(context, "Error loading username: ${e.message}", Toast.LENGTH_SHORT).show()
                currentUserName = "Unknown User" // Fallback al email
            }
        } else {
            currentUserName = "Unknown User (Not Logged In)" // En caso de que no haya usuario autenticado
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Your Collection", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Enter your Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Write a Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                getPlaceholderCollections.add(Collection("x", title, description, emptyList(), currentUserName, emptyList()))
                onCollectionCreated()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Save Collection")
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}