package com.example.stackit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stackit.R
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.stackit.ui.theme.StackitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(auth: FirebaseAuth, collectionId: String, onReturnClicked: () -> Unit) {
    var contentEditable by remember { mutableStateOf(false) }
    Scaffold (
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = { onReturnClicked() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
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
            )
        },
        // todo
        // floatting button només s'ha de mostrar en el cas que l'usuari sigui el administrador de la col·lecció
        // per a fer-ho s'a de recuperar el uID del current user (auth.currentUser?.uId) i recuperar de la BBDD
        // el username. El codi el podem trobar a les línies 30-47 de (buscar el LaunchedEffect):
        // https://github.com/danielhoyosceldran/StackIt_app/blob/main/app/src/main/java/com/example/stackit/ui/screens/CreateCollectionScreen.kt
        floatingActionButton = {
            FloatingActionButton(onClick = { /* todo: add button */ }) {
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
            ) {
                val t: Collection? = getPlaceholderCollections.find { it.id == collectionId }
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = t?.name ?: "Collection not found",
                        fontSize = 30.sp
                    )

                    Row (
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // todo
                        // Mostrar quan es pugui determinar si el currentUser és l'administrador de la col·lecció
                        IconButton(
                            onClick = { contentEditable = !contentEditable }
                        ) {
                            if (contentEditable) Icon(
                                Icons.Filled.Check,
                                contentDescription = "Check")
                            else Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit")
                        }
                        IconButton(
                            onClick = { /* todo: share button */ }
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t?.description ?: "Description not found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Text(
                    text = ("Administrator: " + (t?.admin ?: "Not found")),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 16.dp)
                )
                Row (
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { /* todo: Ranking button */ },
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(text = "Ranking")
                    }

                    Button(
                        onClick = { /* todo: Graphics button */ },
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(text = "Graphics")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if(getPlaceholderItems.isEmpty()) {
                    Text(text = "No collections found")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(getPlaceholderItems) { collectionItem ->
                            ItemCard(collectionItem, contentEditable)
                        }
                        item { Spacer(modifier = Modifier.height(92.dp)) }
                    }
                }
            }

        }
    }
}

data class Item(val id: String, val name: String, val description: String)

val getPlaceholderItems = mutableListOf(
    Item("1", "Item 1", "Description 1"),
    Item("2", "Item 2", "Description 2"),
    Item("3", "Item 3", "Description 3"),
    Item("4", "Item 4", "Description 4"),
    Item("5", "Item 5", "Description 5"),
    Item("6", "Item 6", "Description 6"),
    Item("7", "Item 7", "Description 7"),
    Item("8", "Item 8", "Description 8"),
    Item("9", "Item 9", "Description 9"),
    Item("10", "Item 10", "Description 10")
)

@Composable
fun ItemCard(collectionItem: Item, contentEditable: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = { /* todo: handle click */ }
    ) {
        Row (
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (contentEditable)
                    IconButton(onClick = { /* todo: delete item */ }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                Text(
                    text = "Titol",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "0",
                fontSize = 22.sp
            )
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease")
                }
                IconButton(
                    onClick = {},

                    ) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionScreenPreview() {
    StackitTheme {
        CollectionScreen(
            auth = FirebaseAuth.getInstance(),
            collectionId = "-1",
            onReturnClicked = {}
        )
    }
}