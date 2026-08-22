package com.example.ui.swfexplorer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.AppState
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    onOpenSwf: () -> Unit,
    onSaveSwf: () -> Unit
) {
    val navController = rememberNavController()
    val appState by viewModel.appState.collectAsState()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                appState = appState,
                onOpenSwf = onOpenSwf,
                onViewExplorer = { navController.navigate("explorer") }
            )
        }
        composable("explorer") {
            ExplorerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditTag = { index -> navController.navigate("editor/$index") },
                onSave = onSaveSwf
            )
        }
        composable("editor/{tagIndex}") { backStackEntry ->
            val tagIndex = backStackEntry.arguments?.getString("tagIndex")?.toIntOrNull() ?: return@composable
            EditorScreen(
                viewModel = viewModel,
                tagIndex = tagIndex,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appState: AppState,
    onOpenSwf: () -> Unit,
    onViewExplorer: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SWF Editor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenSwf) {
                Icon(Icons.Default.FolderOpen, contentDescription = "Open SWF")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (appState) {
                is AppState.Idle -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tap the folder icon to open an SWF file")
                    }
                }
                is AppState.Loading -> {
                    CircularProgressIndicator()
                }
                is AppState.Loaded -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loaded: ${appState.file.name}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Size: ${appState.file.length() / 1024} KB")
                        Text("Version: ${appState.swf.header.version}")
                        Text("Signature: ${appState.swf.header.signature}")
                        Text("Tags: ${appState.swf.tags.size}")
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onViewExplorer) {
                            Text("Open Explorer")
                        }
                    }
                }
                is AppState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(appState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
