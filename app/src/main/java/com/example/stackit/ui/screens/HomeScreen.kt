package com.example.stackit.ui.screens

import android.R.attr.fontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogoutClicked: () -> Unit,
               onCreateCollectionClicked: () -> Unit
) {
    val collections = getPlaceholderCollections;
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
                if(collections.isEmpty()) {
                    Text(text = "No collections found")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(collections) { collection ->
                            CollectionCard(collection)
                        }
                    }
                }
            }
        }
    }
}

// Placeholder data
// Colección: Id Titulo Descripción Usuarios [...] Administrador <- usernames Items [item...]
data class Collection(
    val id: String,
    val name: String,
    val description: String,
    val users: List<String>,
    val admin: String,
    val items: List<Item> // Assuming Item is another data class you'll define
)

data class Item(val id: String, val name: String, val description: String) // Example Item data class

@Composable
fun CollectionCard(collection: Collection) {
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
                    text = collection.name,
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
        HomeScreen(onLogoutClicked = {}, onCreateCollectionClicked = {})
    }
}

val getPlaceholderCollections = mutableListOf(
    Collection("1", "Collection 1", "Description 1", emptyList(), "admin", emptyList()),
    Collection("2", "Collection 2", "Description 2", emptyList(), "admin", emptyList()),
    Collection("3", "Collection 3", "Description 3", emptyList(), "admin", emptyList()),
    Collection("4", "Collection 4", "Description 4", emptyList(), "admin", emptyList()),
    Collection("5", "Collection 5", "Description 5", emptyList(), "admin", emptyList()),
    Collection("6", "Collection 6", "Description 6", emptyList(), "admin", emptyList()),
    Collection("7", "Collection 7", "Description 7", emptyList(), "admin", emptyList()),
    Collection("8", "Collection 8", "Description 8", emptyList(), "admin", emptyList()),
    Collection("9", "Collection 9", "Description 9", emptyList(), "admin", emptyList()),
    Collection("10", "Collection 10", "Description 10", emptyList(), "admin", emptyList())
)