package com.example.practica3room.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practica3room.model.DateConverter
import com.example.practica3room.model.TaskApi
import com.example.practica3room.repository.TaskApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados de la UI
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class TaskViewModel(private val repository: TaskApiRepository) : ViewModel() {

    // Estado de las tareas
    private val _tasksState = MutableStateFlow<UiState<List<TaskApi>>>(UiState.Idle)
    val tasksState: StateFlow<UiState<List<TaskApi>>> = _tasksState.asStateFlow()

    // Estado de operaciones individuales (crear, actualizar, eliminar)
    private val _operationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val operationState: StateFlow<UiState<String>> = _operationState.asStateFlow()

    // Estado de autenticación
    private val _authState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val authState: StateFlow<UiState<String>> = _authState.asStateFlow()

    // ============ AUTENTICACIÓN ============

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = repository.login(username, password)

            _authState.value = if (result.isSuccess) {
                UiState.Success("Login exitoso")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _tasksState.value = UiState.Idle
            _authState.value = UiState.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = UiState.Idle
    }

    // ============ OPERACIONES DE TAREAS ============

    fun loadTasks() {
        viewModelScope.launch {
            _tasksState.value = UiState.Loading

            val result = repository.getAllTasks()

            _tasksState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull() ?: emptyList())
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al cargar tareas")
            }
        }
    }

    fun createTask(name: String, deadline: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            val result = repository.createTask(name, deadline, false)

            _operationState.value = if (result.isSuccess) {
                loadTasks() // Recargar lista
                UiState.Success("Tarea creada exitosamente")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al crear tarea")
            }
        }
    }

    fun updateTask(id: Int, name: String, deadline: String, status: Boolean) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            Log.d("TaskViewModel", "📝 Actualizando tarea $id") // ← AGREGAR

            val result = repository.updateTask(id, name, deadline, status)

            _operationState.value = if (result.isSuccess) {
                Log.d("TaskViewModel", "✅ Tarea actualizada, recargando lista") // ← AGREGAR
                loadTasks() // ← Asegúrate que esto se llame
                UiState.Success("Tarea actualizada exitosamente")
            } else {
                Log.e("TaskViewModel", "❌ Error: ${result.exceptionOrNull()?.message}") // ← AGREGAR
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al actualizar")
            }
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            val result = repository.deleteTask(id)

            _operationState.value = if (result.isSuccess) {
                loadTasks() // Recargar lista
                UiState.Success("Tarea eliminada exitosamente")
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }

    fun updateTaskStatus(id: Int, newStatus: Boolean) {
        viewModelScope.launch {
            val result = repository.updateTaskStatus(id, newStatus)

            if (result.isSuccess) {
                loadTasks() // Recargar lista
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = UiState.Idle
    }

    // ============ HELPER ============

    // Obtener una tarea específica del estado actual
    fun getTaskById(id: Int): TaskApi? {
        return when (val state = _tasksState.value) {
            is UiState.Success -> state.data.find { it.id == id }
            else -> null
        }
    }
}