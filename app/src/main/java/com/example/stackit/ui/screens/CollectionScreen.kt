package com.example.stackit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
                    ) { }
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