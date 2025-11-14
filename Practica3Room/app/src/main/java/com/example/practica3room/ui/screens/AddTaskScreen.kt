@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.practica3room.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.practica3room.ui.theme.BackgroundCream
import com.example.practica3room.ui.theme.PrimaryBlue
import com.example.practica3room.viewmodel.TaskViewModel
import com.example.practica3room.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTaskScreen(navController: NavHostController, viewModel: TaskViewModel) {
    var taskName by remember { mutableStateOf("") }
    var plannedDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val operationState by viewModel.operationState.collectAsState()

    // Observar el estado de la operación
    LaunchedEffect(operationState) {
        when (operationState) {
            is UiState.Success -> {
                viewModel.resetOperationState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                errorMessage = (operationState as UiState.Error).message
            }
            else -> { /* No hacer nada */ }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Agregar Nueva Tarea",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = BackgroundCream
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = BackgroundCream,
                    navigationIconContentColor = BackgroundCream
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Nombre de la tarea") },
                placeholder = { Text("Ej: Estudiar Kotlin") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    focusedLabelColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                ),
                enabled = operationState !is UiState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = plannedDate,
                onValueChange = { },
                label = { Text("Fecha planeada") },
                placeholder = { Text("Selecciona una fecha") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (operationState !is UiState.Loading) {
                            showDatePicker = true
                        }
                    },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = PrimaryBlue,
                    disabledLabelColor = PrimaryBlue,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha",
                        tint = PrimaryBlue,
                        modifier = Modifier.clickable {
                            if (operationState !is UiState.Loading) {
                                showDatePicker = true
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    errorMessage = ""
                    when {
                        taskName.isBlank() -> {
                            errorMessage = "El nombre de la tarea no puede estar vacío"
                        }
                        plannedDate.isBlank() -> {
                            errorMessage = "Debe seleccionar una fecha"
                        }
                        else -> {
                            viewModel.createTask(taskName.trim(), plannedDate.trim())
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = operationState !is UiState.Loading
            ) {
                if (operationState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BackgroundCream
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar Tarea",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue
                ),
                enabled = operationState !is UiState.Loading
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Mostrar error si existe
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        plannedDate = formatter.format(date)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = PrimaryBlue)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryBlue,
                    todayContentColor = PrimaryBlue,
                    todayDateBorderColor = PrimaryBlue
                )
            )
        }
    }
}