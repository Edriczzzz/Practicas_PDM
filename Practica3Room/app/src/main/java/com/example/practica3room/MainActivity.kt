package com.example.practica3room

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practica3room.di.AppContainer
import com.example.practica3room.repository.TaskApiRepository
import com.example.practica3room.ui.screens.Navigator
import com.example.practica3room.ui.theme.Practica3RoomTheme
import com.example.practica3room.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TaskApiRepository (para login/logout)
        val apiRepository = TaskApiRepository()

        // TaskRepository del AppContainer (para operaciones de tareas con Room + API)
        val taskRepository = AppContainer.taskRepository

        // Crear el ViewModel con ambos repositorios
        viewModel = TaskViewModel(
            repository = taskRepository,
            apiRepository = apiRepository
        )

        setContent {
            Practica3RoomTheme {
                Navigator(viewModel)
            }
        }
    }
}