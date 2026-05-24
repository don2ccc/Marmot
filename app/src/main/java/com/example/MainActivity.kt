package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.DataRepository
import com.example.ui.components.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PhraseBuddyViewModel
import com.example.ui.viewmodel.PhraseBuddyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup local database, DAO and repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DataRepository(database.passageDao())
        
        // Instantiate ViewModel
        val factory = PhraseBuddyViewModelFactory(repository, applicationContext)
        val viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[PhraseBuddyViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Wrap our navigation router passing state viewModel, applying layout padding correctly
                    AppNavigation(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Support modifier binding with AppNavigation overload parameter cleanly
@androidx.compose.runtime.Composable
fun AppNavigation(
    viewModel: PhraseBuddyViewModel,
    modifier: Modifier
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        com.example.ui.components.AppNavigation(viewModel = viewModel)
    }
}
