package com.example.practica3room.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.practica3room.viewmodel.TaskViewModel

@Composable
fun Navigator(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") { MenuScreen(navController) }
        composable("task_list") { TaskListScreen(navController, viewModel) }
        composable("add_task") { AddTaskScreen(navController, viewModel) }
        composable("manage_status") { StatusScreen(navController, viewModel) }
        composable("edit_task") { EditScreen(navController, viewModel) }
        composable("delete_task") { DeleteTasksScreen(navController, viewModel) }
    }
}