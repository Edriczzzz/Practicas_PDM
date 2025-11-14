package com.example.practica3room

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practica3room.local.TokenManager
import com.example.practica3room.repository.TaskApiRepository
import com.example.practica3room.ui.screens.Navigator
import com.example.practica3room.ui.theme.Practica3RoomTheme
import com.example.practica3room.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar TokenManager y Repository
        val tokenManager = TokenManager(applicationContext)
        val repository = TaskApiRepository()

        // Crear el ViewModel
        viewModel = TaskViewModel(repository)

        setContent {
            Practica3RoomTheme {
                Navigator(viewModel)
            }
        }
    }
}