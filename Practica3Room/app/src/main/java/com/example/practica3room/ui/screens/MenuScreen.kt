@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.practica3room.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.practica3room.ui.theme.BackgroundCream
import com.example.practica3room.ui.theme.PrimaryBlue
import com.example.practica3room.viewmodel.TaskViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MenuScreen(navController: NavHostController, viewModel: TaskViewModel) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Gestor de Tareas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = BackgroundCream
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = BackgroundCream
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono principal
            Text(
                text = "📝",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Botón: Ver todas las tareas
            MenuButton(
                text = "Ver Todas las Tareas",
                icon = Icons.Default.List,
                onClick = { navController.navigate("task_list") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón: Agregar nueva tarea
            MenuButton(
                text = "Agregar Nueva Tarea",
                icon = Icons.Default.Add,
                onClick = { navController.navigate("add_task") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón: Editar tareas
            MenuButton(
                text = "Editar Tareas",
                icon = Icons.Default.Edit,
                onClick = { navController.navigate("edit_task") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón: Gestionar estado de tareas
            MenuButton(
                text = "Gestionar Estado",
                icon = Icons.Default.CheckCircle,
                onClick = { navController.navigate("manage_status") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón: Eliminar tareas
            MenuButton(
                text = "Eliminar Tareas",
                icon = Icons.Default.Delete,
                onClick = { navController.navigate("delete_task") }
            )
        }
    }

    // Diálogo de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
            },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = PrimaryBlue)
                }
            }
        )
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}