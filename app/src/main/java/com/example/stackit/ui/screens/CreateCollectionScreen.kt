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
    val scope = rememberCoroutineScope()
    val firestore: FirebaseFirestore = Firebase.firestore

    var currentUserName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(key1 = Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            try {
                val profileDoc = firestore.collection("user_profiles").document(userId).get().await()
                currentUserName = profileDoc.getString("username") ?: currentUser.email ?: "Unknown User"
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading username: ${e.message}", Toast.LENGTH_SHORT).show()
                currentUserName = "Unknown User"
            }
        } else {
            currentUserName = "Unknown User (Not Logged In)"
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
                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(context, "Title and description cannot be empty.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Toast.makeText(context, "User not logged in. Cannot create collection.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        val initialCollaborators = listOf(currentUserName)

                        val newCollectionData = hashMapOf(
                            "title" to title,
                            "description" to description,
                            "creatorUid" to userId,
                            "creatorUsername" to currentUserName,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "items" to emptyList<String>(),
                            "collaborators" to initialCollaborators
                        )

                        firestore.collection("collections")
                            .add(newCollectionData)
                            .await()

                        Toast.makeText(context, "Collection saved successfully!", Toast.LENGTH_SHORT).show()

                        onCollectionCreated()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error saving collection: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
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