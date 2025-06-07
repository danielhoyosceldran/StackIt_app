package com.example.stackit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.example.stackit.ui.screens.HomeScreen
import com.example.stackit.ui.screens.AuthScreen
import com.example.stackit.ui.screens.CreateProfileScreen
import com.example.stackit.ui.screens.CreateCollectionScreen

import com.example.stackit.ui.theme.StackitTheme

class MainActivity : ComponentActivity() {

    // Initialize Firebase Auth instance
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth // Get the Firebase Auth instance

        setContent {
            StackitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the auth instance to your NavGraph
                    MyNavigationGraph(auth = auth)
                }
            }
        }
    }
}

@Composable
fun MyNavigationGraph(auth: FirebaseAuth) { // Now accepts FirebaseAuth as a parameter
    val navController = rememberNavController()

    val ROUTE_AUTH = "auth_route"
    val ROUTE_HOME = "home_route"
    val ROUTE_CREATE_PROFILE = "create_profile_route"
    val ROUTE_CREATE_COLLECTION = "create_collection_route"

    // State to track if the user is logged in, initially unknown
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }


    // Use LaunchedEffect to observe Firebase authentication state
    // This runs once when the Composable enters the composition
    LaunchedEffect(key1 = Unit) {
        // Add an Auth State Listener to observe changes in user login status
        auth.addAuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }
    }

    // While authentication state is unknown (null), display nothing or a loading indicator
    if (isLoggedIn == null) {
        // You can display a CircularProgressIndicator or a loading screen here
        // For simplicity, we'll just return for now
        return
    }

    // Once the state is known, determine the starting destination
    val startDestination = if (isLoggedIn == true) ROUTE_HOME else ROUTE_AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(ROUTE_AUTH) {
            AuthScreen(
                onLoginSuccess = { // Navigate to home on successful login/registration
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_AUTH) { inclusive = true } // Clear back stack
                    }
                },
                onRegisterSuccess = {
                    // Después de registro, ir a CreateProfileScreen
                    navController.navigate(ROUTE_CREATE_PROFILE) {
                        popUpTo(ROUTE_AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_CREATE_PROFILE) {
            CreateProfileScreen(
                auth = auth,
                onProfileCreated = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_CREATE_PROFILE) { inclusive = true }
                        popUpTo(ROUTE_AUTH) { inclusive = true }
                    }
                }
            )
        }
        composable(ROUTE_HOME) {
            HomeScreen(
                auth = auth,
                onLogoutClicked = { // Navigate to auth on successful logout
                    navController.navigate(ROUTE_AUTH) {
                        popUpTo(ROUTE_HOME) { inclusive = true } // Clear back stack
                    }
                },
                onCreateCollectionClicked = {
                    navController.navigate(ROUTE_CREATE_COLLECTION)
                }
            )
        }
        composable(ROUTE_CREATE_COLLECTION) {
            CreateCollectionScreen(
                auth = auth,
                onCollectionCreated = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_CREATE_COLLECTION) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StackitTheme {
        // For preview, you need a FirebaseAuth instance. You can mock it,
        // or simply preview individual screens (AuthScreen/HomeScreen) directly
        // as a real Firebase Auth instance cannot be initialized in a preview.
        // AuthScreen(onLoginSuccess = {}) // Example of previewing a single screen
    }
}