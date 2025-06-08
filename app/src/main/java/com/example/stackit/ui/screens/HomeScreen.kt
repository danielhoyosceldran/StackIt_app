package com.example.stackit.ui.screens

import android.R.attr.fontWeight
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stackit.ui.theme.StackitTheme
import com.example.stackit.R
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogoutClicked: () -> Unit,
               onCreateCollectionClicked: () -> Unit,
               auth: FirebaseAuth?,
               firestore: FirebaseFirestore? = null
) {

    val firestore: FirebaseFirestore = Firebase.firestore
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val collections = remember { mutableStateListOf<Collection>() }
    val invitations = remember { mutableStateListOf<Invitation>() }
    var isLoadingCollections by remember { mutableStateOf(true) }

    val currentUserCollectionIds = remember { mutableStateListOf<String>() }
    val currentUserInvitationsIds = remember { mutableStateListOf<String>() }

    var invitationFlag by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        val currentUser = auth?.currentUser
        val userId = currentUser?.uid
        // 1. Escuchar el perfil del usuario actual (para obtener sus IDs de colección)
        if (userId != null) {
            firestore.collection("user_profiles").document(userId)
                .addSnapshotListener { profileSnapshot, e ->
                    if (e != null) {
                        Toast.makeText(
                            context,
                            "Error listening for user profile: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        currentUserCollectionIds.clear()
                        currentUserInvitationsIds.clear()
                        return@addSnapshotListener
                    }

                    if (profileSnapshot != null && profileSnapshot.exists()) {
                        // Obtener la lista de IDs de colecciones del perfil del usuario
                        val ids = profileSnapshot.get("collections") as? List<String> ?: emptyList()
                        val idsInv = profileSnapshot.get("invitations") as? List<String> ?: emptyList()
                        currentUserCollectionIds.clear()
                        currentUserCollectionIds.addAll(ids)
                        currentUserInvitationsIds.clear()
                        currentUserInvitationsIds.addAll(idsInv)
                    } else {
                        currentUserCollectionIds.clear() // No hay perfil o está vacío
                        currentUserInvitationsIds.clear()
                    }
                }
        }

        // 2. Escuchar TODAS las colecciones (filtrado se hará en la UI o en reglas si es necesario)
        firestore.collection("collections")
            .addSnapshotListener { snapshots, e ->
                isLoadingCollections = false // La carga inicial ha terminado

                if (e != null) {
                    Toast.makeText(
                        context,
                        "Error listening for collections: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    collections.clear()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val newCollections = mutableListOf<Collection>()
                    for (doc in snapshots.documents) {
                        try {
                            val collection = doc.toObject(Collection::class.java)?.copy(id = doc.id)
                            if (collection != null) {
                                newCollections.add(collection)
                            }
                        } catch (mappingError: Exception) {
                            println("Error mapping Firestore document to Collection: ${mappingError.message}")
                        }
                    }
                    collections.clear()
                    // Opcional: ordenar por fecha de creación
                    collections.addAll(newCollections.sortedByDescending { it.createdAt.toDate().time })
                }
            }

        firestore.collection("invitations")
            .addSnapshotListener { snapshots, e ->
                isLoadingCollections = false

                if (e != null) {
                    Toast.makeText(
                        context,
                        "Error listening for invitations: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    invitations.clear()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val newInvitations = mutableListOf<Invitation>()
                    for (doc in snapshots.documents) {
                        try {
                            val invitation = doc.toObject(Invitation::class.java)?.copy(id = doc.id)
                            if (invitation != null) {
                                newInvitations.add(invitation)
                            }
                        } catch (mappingError: Exception) {
                            println("Error mapping Firestore document to Invitation: ${mappingError.message}")
                        }
                    }
                    invitations.clear()
                    invitations.addAll(newInvitations.sortedByDescending { it.createdAt.toDate().time })
                }
            }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = stringResource(R.string.app_name),
                            tint = Color.Unspecified,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(R.string.app_name))
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClicked) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout",
                            modifier = Modifier.padding(end = 8.dp))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Row (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center)
                {
                    Button(onClick = {invitationFlag = false}) {
                        Text("Collections")
                    }
                    Button(onClick = {invitationFlag = true}) {
                        Text("Invitations")
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateCollectionClicked) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoadingCollections) {
                    //CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
                } else {
                    val filteredCollections = collections.filter { it.id in currentUserCollectionIds }
                    val filteredInvitations = invitations.filter { it.id in currentUserInvitationsIds }

                    if (!invitationFlag) {
                        if (filteredCollections.isEmpty()) {
                            Text(text = "No collections found for you. Start by creating one!")
                        } else {

                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredCollections) { collection ->
                                    CollectionCard(
                                        collection = collection,
                                        currentUserId = auth?.currentUser?.uid,
                                        firestore = firestore
                                    )
                                }
                            }
                        }
                    }
                    else {
                        if (filteredInvitations.isEmpty()) { // Comprueba si las invitaciones están vacías
                            Text(text = "No invitations found for you.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredInvitations) { invitation ->
                                    InvitationCard(
                                        invitation = invitation,
                                        currentUserId = auth?.currentUser?.uid,
                                        firestore = firestore
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Collection(
    val id: String = "", // Firestore document ID
    val title: String = "",
    val description: String = "",
    val users: List<String> = emptyList(), // Opcional: si 'users' es para otros tipos de usuarios
    val creatorUid: String = "", // UID del creador
    val creatorUsername: String = "", // Username del creador
    val items: List<Item> = emptyList(),
    val collaborators: List<String> = emptyList(), // Esta es la lista que queremos actualizar
    val createdAt: Timestamp = Timestamp.now()
)

data class Invitation(
    val id: String = "", // Firestore document ID
    val title: String = "",
    val description: String = "",
    val users: List<String> = emptyList(), // Opcional: si 'users' es para otros tipos de usuarios
    val creatorUid: String = "", // UID del creador
    val creatorUsername: String = "", // Username del creador
    val items: List<Item> = emptyList(),
    val collaborators: List<String> = emptyList(), // Esta es la lista que queremos actualizar
    val createdAt: Timestamp = Timestamp.now()
)


data class Item(val id: String = "", val name: String = "", val description: String = "")

@Composable
fun CollectionCard(
    collection: Collection,
    currentUserId: String?,
    firestore: FirebaseFirestore?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isCreator = currentUserId != null && currentUserId == collection.creatorUid
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = collection.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = collection.description,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = collection.creatorUsername,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
            }
            IconButton (
                onClick = { /* TODO: Handle button click */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
        }
    }
}

@Composable
fun InvitationCard(
    invitation: Invitation,
    currentUserId: String?,
    firestore: FirebaseFirestore?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isCreator = currentUserId != null && currentUserId == invitation.creatorUid
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = invitation.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = invitation.description,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = invitation.creatorUsername,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
            }
            IconButton (
                onClick = { /* TODO: Handle button click */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    StackitTheme {
        // --- CÓDIGO CORREGIDO PARA EL PREVIEW ---
        // Simplemente pasar 'null' para las instancias de Firebase.
        // Los LaunchedEffects y operaciones de Firebase no funcionarán en el preview,
        // por lo que los datos deberán ser simulados.

        // Simula la lista de colecciones que se mostrarían en el preview
        val previewCollectionsList = listOf(
            Collection("1", "Mi Colección Favorita", "Una descripción genial para mi colección.", emptyList(), "uid123", "Creador1", emptyList(), listOf("Collab1", "Collab2")),
            Collection("2", "Proyectos de Trabajo", "Cosas importantes del trabajo.", emptyList(), "uid456", "Creador2", emptyList(), listOf("CollabA")),
            Collection("3", "Colección Vacía", "No tiene elementos.", emptyList(), "uid123", "Creador1", emptyList(), emptyList())
        )

        Column {
            Text("Preview de HomeScreen: los datos de Firestore NO son en tiempo real.", Modifier.padding(16.dp))
            Text("Las colecciones mostradas son datos simulados.", Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp))

            // Puedes previsualizar LazyColumn directamente con los datos simulados
            // Para ver cómo se renderiza la lista
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .height(300.dp) // Limitar altura para el preview si es necesario
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(previewCollectionsList) { collection ->
                    CollectionCard(
                        collection = collection,
                        currentUserId = "uid123", // Simula que el usuario actual es el creador de algunas
                        firestore = null // Pasa null a Firestore en el preview
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Llama a HomeScreen pasando null para las instancias de Firebase
            HomeScreen(
                onLogoutClicked = {},
                onCreateCollectionClicked = {},
                auth = null, // Pasa null para FirebaseAuth
                firestore = null // Pasa null para FirebaseFirestore
            )
        }
    }
}