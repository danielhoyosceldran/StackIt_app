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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.stackit.ui.theme.StackitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(auth: FirebaseAuth, collectionId: String, onReturnClicked: () -> Unit) {
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
                verticalArrangement = Arrangement.Start as Arrangement.Vertical,
                horizontalAlignment = Alignment.Start
            ) {
                val t: Collection? = getPlaceholderCollections.find { it.id == collectionId }
                Row {
                    Text(
                        text = t?.name ?: "Collection not found",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = { /* todo: share button */ }
                    ) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share"
                        )
                    }
                }
                Text(
                    text = t?.description ?: "Description not found",
                    style = MaterialTheme.typography.bodyLarge
                )
                Row {
                    Button(
                        onClick = { /* todo: Ranking button */ }
                    ) {
                        Text(text = "Ranking")
                    }

                    Button(
                        onClick = { /* todo: Graphics button */ }
                    ) {
                        Text(text = "Graphics")
                    }
                }
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
                            ItemCard(collectionItem)
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
    Item("5", "Item 5", "Description 5")
)

@Composable
fun ItemCard(collectionItem: Item) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = { /* todo: handle click */ }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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