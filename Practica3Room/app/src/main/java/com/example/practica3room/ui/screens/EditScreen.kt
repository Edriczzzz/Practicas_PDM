package com.example.practica3room.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.practica3room.model.DateConverter
import com.example.practica3room.model.TaskApi
import com.example.practica3room.ui.theme.BackgroundCream
import com.example.practica3room.ui.theme.PrimaryBlue
import com.example.practica3room.viewmodel.TaskViewModel
import com.example.practica3room.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    val tasksState by viewModel.tasksState.collectAsState()

    // Cargar tareas al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Editar Tareas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = BackgroundCream
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadTasks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
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
                .fillMaxSize()
                .background(BackgroundCream)
                .padding(paddingValues)
        ) {
            when (tasksState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = (tasksState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadTasks() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val tasks = (tasksState as UiState.Success<List<TaskApi>>).data

                    if (tasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay tareas para editar",
                                fontSize = 18.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tasks, key = { it.id ?: 0 }) { task ->
                                EditTaskCard(
                                    task = task,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cargando...")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskCard(
    task: TaskApi,
    viewModel: TaskViewModel
) {
    var taskName by remember { mutableStateOf(task.name) }
    var taskDate by remember { mutableStateOf(DateConverter.toDisplayFormat(task.deadline)) }
    var taskStatus by remember { mutableStateOf(task.status) }
    var isEditing by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val operationState by viewModel.operationState.collectAsState()

    // Observar el resultado de la operación
    LaunchedEffect(operationState) {
        when (operationState) {
            is UiState.Success -> {
                isEditing = false
                viewModel.resetOperationState()
            }
            else -> { /* No hacer nada */ }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isEditing) {
                // Modo edición
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Nombre de la tarea") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    ),
                    singleLine = true,
                    enabled = operationState !is UiState.Loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = taskDate,
                    onValueChange = { },
                    label = { Text("Fecha (dd/mm/yyyy)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (operationState !is UiState.Loading) {
                                showDatePicker = true
                            }
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = PrimaryBlue,
                        disabledLabelColor = PrimaryBlue,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    readOnly = true,
                    enabled = false,
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (taskStatus) "Completada" else "Pendiente",
                            fontSize = 14.sp,
                            color = if (taskStatus) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Switch(
                            checked = taskStatus,
                            onCheckedChange = { taskStatus = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            ),
                            enabled = operationState !is UiState.Loading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón cancelar
                    OutlinedButton(
                        onClick = {
                            taskName = task.name
                            taskDate = DateConverter.toDisplayFormat(task.deadline)
                            taskStatus = task.status
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        ),
                        enabled = operationState !is UiState.Loading
                    ) {
                        Text("Cancelar")
                    }

                    // Botón guardar
                    Button(
                        onClick = {
                            viewModel.updateTask(
                                id = task.id!!,
                                name = taskName,
                                deadline = taskDate,
                                status = taskStatus
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        ),
                        enabled = taskName.isNotBlank() && taskDate.isNotBlank() && operationState !is UiState.Loading
                    ) {
                        if (operationState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar")
                        }
                    }
                }

            } else {
                // Modo visualización
                Text(
                    text = task.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fecha: ${DateConverter.toDisplayFormat(task.deadline)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (task.status) "✓ Completada" else "○ Pendiente",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.status) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Editar")
                }
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
                        taskDate = formatter.format(date)
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