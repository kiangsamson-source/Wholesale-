package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.WholesaleRepository
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.IssuesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WholesaleTab
import com.example.viewmodel.WholesaleViewModel
import com.example.viewmodel.WholesaleViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Room Local Database and Repository
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = WholesaleRepository(database)

        // 2. Set up the factory and obtain the ViewModel
        val factory = WholesaleViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[WholesaleViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainContentContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContentContainer(viewModel: WholesaleViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == WholesaleTab.CATALOG,
                    onClick = { viewModel.selectTab(WholesaleTab.CATALOG) },
                    icon = { Icon(Icons.Default.LocalMall, contentDescription = "Wholesale Catalog") },
                    label = { Text("Wholesale Hub", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = currentTab == WholesaleTab.CHAT,
                    onClick = { viewModel.selectTab(WholesaleTab.CHAT) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Direct Vendor Chat") },
                    label = { Text("Direct Connect", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = currentTab == WholesaleTab.ISSUES,
                    onClick = { viewModel.selectTab(WholesaleTab.ISSUES) },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Business Advisor") },
                    label = { Text("AI Consultant", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars) // Avoid camera notch cutting titles
        ) {
            when (currentTab) {
                WholesaleTab.CATALOG -> CatalogScreen(viewModel)
                WholesaleTab.CHAT -> ChatScreen(viewModel)
                WholesaleTab.ISSUES -> IssuesScreen(viewModel)
            }
        }
    }
}

// Custom Font weight reference to avoid build compiler issues
private object FontWeight {
    val SemiBold = androidx.compose.ui.text.font.FontWeight.SemiBold
}
