package com.example.stackit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogoutClicked: () -> Unit,
               onCreateCollectionClicked: () -> Unit,
               auth: FirebaseAuth?,
               firestore: FirebaseFirestore? = null
) {
    // Usar la instancia de Firestore pasada, o la real si no se pasa nada (solo en runtime)
    val actualFirestore: FirebaseFirestore = firestore ?: Firebase.firestore
    val actualAuth: FirebaseAuth = auth ?: Firebase.auth // Usar la instancia de Auth pasada o la real

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val collections = remember { mutableStateListOf<Collection>() }
    val invitations = remember { mutableStateListOf<Invitation>() } // Lista para las invitaciones
    var isLoadingCollections by remember { mutableStateOf(true) }
    var isLoadingInvitations by remember { mutableStateOf(true) } // Estado de carga para invitaciones

    val currentUserCollectionIds = remember { mutableStateListOf<String>() }
    val currentUserInvitationsIds = remember { mutableStateListOf<String>() }

    var invitationFlag by remember { mutableStateOf(false) } // Controla qué lista se muestra

    LaunchedEffect(key1 = Unit) {
        val currentUser = actualAuth.currentUser // Usar actualAuth
        val userId = currentUser?.uid
        // 1. Escuchar el perfil del usuario actual (para obtener sus IDs de colección)
        if (userId != null) {
            actualFirestore.collection("user_profiles").document(userId) // Usar actualFirestore
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

        // 2. Escuchar TODAS las colecciones (filtrado se hará en la UI)
        actualFirestore.collection("collections") // Usar actualFirestore
            .addSnapshotListener { snapshots, e ->
                isLoadingCollections = false // La carga de colecciones ha terminado

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
                    collections.addAll(newCollections.sortedByDescending { it.createdAt.toDate().time })
                }
            }

        // 3. Escuchar TODAS las invitaciones (filtrado se hará en la UI)
        actualFirestore.collection("invitations") // Usar actualFirestore
            .addSnapshotListener { snapshots, e ->
                isLoadingInvitations = false // La carga de invitaciones ha terminado

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
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                actualAuth.signOut() // Usar actualAuth
                                Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                                onLogoutClicked()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error logging out: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
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
                    Spacer(Modifier.width(8.dp)) // Espacio entre botones
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
                // Indicador de carga para ambas listas
                if (isLoadingCollections || isLoadingInvitations) { // Muestra si CUALQUIERA está cargando
                    CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
                } else {
                    val filteredCollections = collections.filter { it.id in currentUserCollectionIds }
                    val filteredInvitations = invitations.filter { it.id in currentUserInvitationsIds }

                    // --- FUNCIÓN REFECTORIZADA PARA MOSTRAR LA LISTA DE COLECCIONES/INVITACIONES ---
                    DisplayUserItems(
                        isInvitationFlag = invitationFlag,
                        collections = filteredCollections,
                        invitations = filteredInvitations,
                        currentUserId = actualAuth.currentUser?.uid,
                        firestore = actualFirestore
                    )
                    // -----------------------------------------------------------------------------
                }
            }
        }
    }
}

// =================================================================================================
// FUNCIÓN COMPOSABLE REFECTORIZADA PARA MOSTRAR LA LISTA
// =================================================================================================
@Composable
fun DisplayUserItems(
    isInvitationFlag: Boolean,
    collections: List<Collection>,
    invitations: List<Invitation>,
    currentUserId: String?,
    firestore: FirebaseFirestore?
) {
    if (!isInvitationFlag) { // Mostrar colecciones
        if (collections.isEmpty()) {
            Text(text = "No collections found for you. Start by creating one!")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(collections) { collection ->
                    CollectionCard(
                        collection = collection,
                        currentUserId = currentUserId,
                        firestore = firestore
                    )
                }
            }
        }
    } else { // Mostrar invitaciones
        if (invitations.isEmpty()) {
            Text(text = "No invitations found for you.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(invitations) { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        currentUserId = currentUserId,
                        firestore = firestore
                    )
                }
            }
        }
    }
}
// =================================================================================================


// =================================================================================================
// DATA CLASSES (Mantengo aquí para que el archivo sea autocontenido para el ejemplo)
// =================================================================================================
data class Collection(
    val id: String = "", // Firestore document ID
    val title: String = "",
    val description: String = "",
    val users: List<String> = emptyList(),
    val creatorUid: String = "",
    val creatorUsername: String = "",
    val items: List<Item> = emptyList(),
    val collaborators: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
)

data class Invitation(
    val id: String = "", // Firestore document ID
    val title: String = "",
    val description: String = "",
    val users: List<String> = emptyList(),
    val creatorUid: String = "",
    val creatorUsername: String = "",
    val items: List<Item> = emptyList(),
    val collaborators: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
)


data class Item(val id: String = "", val name: String = "", val description: String = "")

// =================================================================================================
// CollectionCard Composable
// =================================================================================================
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
            .height(IntrinsicSize.Min) // Usar IntrinsicSize.Min para altura flexible
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Padding general para el contenido de la tarjeta
            ) {
                Text(
                    text = collection.title, // Usar 'title'
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold // Añadido FontWeight.Bold para el título
                )
                Text(
                    text = collection.description,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = "Creador: ${collection.creatorUsername}", // Usar 'creatorUsername' y añadir etiqueta
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp)) // Espacio entre elementos
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

// =================================================================================================
// InvitationCard Composable
// =================================================================================================
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
            .height(IntrinsicSize.Min) // Usar IntrinsicSize.Min para altura flexible
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Padding general para el contenido de la tarjeta
            ) {
                Text(
                    text = invitation.title, // Usar 'title'
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold // Añadido FontWeight.Bold
                )
                Text(
                    text = invitation.description,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
                Text(
                    text = "Invitación de: ${invitation.creatorUsername}", // Usar 'creatorUsername' y añadir etiqueta
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                // Puedes añadir botones de Aceptar/Rechazar invitación aquí si quieres.
                // Lógica de botones de Aceptar/Rechazar invitación (si el currentUserId es el invitado)
                // Esto es solo un placeholder, la lógica real sería más compleja y debería
                // modificar el estado de la invitación en Firestore (e.g., status: "accepted").
                if (currentUserId != null && invitation.collaborators.contains(currentUserId)) { // Asumiendo que 'collaborators' guarda el UID del invitado
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(onClick = { /* TODO: Lógica para Aceptar invitación */ }) {
                            Text("Aceptar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { /* TODO: Lógica para Rechazar invitación */ }) {
                            Text("Rechazar")
                        }
                    }
                }
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

// =================================================================================================
// HomeScreenPreview
// =================================================================================================
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    StackitTheme {
        // Simula la lista de colecciones que se mostrarían en el preview
        val previewCollectionsList = listOf(
            Collection("1", "Mi Colección Favorita (Preview)", "Descripción de colección 1.", emptyList(), "uid123", "CreadorPreview1", emptyList(), listOf("Collab1", "Collab2")),
            Collection("2", "Proyectos de Trabajo (Preview)", "Descripción de colección 2.", emptyList(), "uid456", "CreadorPreview2", emptyList(), listOf("CollabA")),
            Collection("3", "Colección Vacía (Preview)", "Sin elementos.", emptyList(), "uid123", "CreadorPreview1", emptyList(), emptyList())
        )
        val previewInvitationsList = listOf(
            Invitation("inv1", "Invitación a la Fiesta (Preview)", "¡Te esperamos!", emptyList(), "hostUid", "Organizador", emptyList(), listOf("someGuestUid")), // Simular un invitado
            Invitation("inv2", "Invitación a Reunión (Preview)", "Tema: Project X.", emptyList(), "bossUid", "Jefe", emptyList(), emptyList())
        )


        Column {
            Text("Preview de HomeScreen: los datos de Firestore NO son en tiempo real.", Modifier.padding(16.dp))
            Text("Las colecciones e invitaciones mostradas son datos simulados.", Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp))

            // Puedes previsualizar LazyColumn directamente con los datos simulados
            // Para ver cómo se renderiza la lista
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .height(300.dp) // Limitar altura para el preview
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Muestra colecciones simuladas en el preview
                items(previewCollectionsList) { collection ->
                    CollectionCard(
                        collection = collection,
                        currentUserId = "uid123", // Simula que el usuario actual es el creador de algunas
                        firestore = null // Pasa null a Firestore en el preview
                    )
                }
                // Si quieres previsualizar las invitaciones en el mismo LazyColumn de preview
                item { Spacer(Modifier.height(16.dp)) } // Espacio entre colecciones e invitaciones
                items(previewInvitationsList) { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        currentUserId = "someGuestUid", // Simula que el usuario actual es el invitado
                        firestore = null
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Llama a HomeScreen pasando null para las instancias de Firebase
            HomeScreen(
                onLogoutClicked = {},
                onCreateCollectionClicked = {},
                auth = null,
                firestore = null
            )
        }
    }
}