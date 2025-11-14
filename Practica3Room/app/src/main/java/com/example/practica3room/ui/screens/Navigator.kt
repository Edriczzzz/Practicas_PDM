package com.example.practica3room.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.practica3room.viewmodel.TaskViewModel

@Composable
fun Navigator(viewModel: TaskViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"  // Iniciar en login
    ) {
        // Pantalla de Login
        composable("login") {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        // Menú principal (después del login)
        composable("menu") {
            MenuScreen(navController = navController, viewModel = viewModel)
        }

        // Lista de todas las tareas
        composable("task_list") {
            TaskListScreen(navController = navController, viewModel = viewModel)
        }

        // Agregar tarea
        composable("add_task") {
            AddTaskScreen(navController = navController, viewModel = viewModel)
        }

        // Gestionar estado
        composable("manage_status") {
            StatusScreen(navController = navController, viewModel = viewModel)
        }

        // Editar tareas
        composable("edit_task") {
            EditScreen(navController = navController, viewModel = viewModel)
        }

        // Eliminar tareas
        composable("delete_task") {
            DeleteTasksScreen(navController = navController, viewModel = viewModel)
        }

        // Editar tarea específica (para cuando hagan clic en una tarea)
        /*composable(
            route = "edit_single_task/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            EditScreen(
                navController = navController,
                viewModel = viewModel,
                taskId = taskId
            )
        }*/
    }
}