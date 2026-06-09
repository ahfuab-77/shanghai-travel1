package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.db.AppDatabase
import com.example.data.repository.TravelRepository
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TravelViewModel
import com.example.viewmodel.TravelViewModelFactory

class MainActivity : ComponentActivity() {

    private val travelViewModel: TravelViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TravelRepository(database.travelDao())
        TravelViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = travelViewModel)
            }
        }
    }
}
